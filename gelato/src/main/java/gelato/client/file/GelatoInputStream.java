/*
 *   Copyright (c) 2020. Enzo Reyes
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gelato.client.file;

import gelato.Gelato;
import gelato.GelatoFileDescriptor;
import gelato.client.GelatoMessage;
import gelato.client.GelatoMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.ByteEncoder;
import protocol.P9Protocol;
import protocol.messages.request.CloseRequest;
import protocol.messages.request.ReadRequest;
import protocol.messages.response.CloseResponse;
import protocol.messages.response.ReadResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class GelatoInputStream extends InputStream {

  private GelatoMessaging messaging;
  private GelatoFileDescriptor fileDescriptor;

  private int currentLocation = 0;
  private long fileLocation = 0;
  private byte[] buffer = new byte[Gelato.DEFAULT_NET_IO_MEM_BUFFER];
  private long fileSize = 0;
  private long ioNetworkSize = 0;
  private final Logger logger = LoggerFactory.getLogger(GelatoInputStream.class);


  private void initialise() throws IOException {
    currentLocation = 0;
    fileLocation = 0;
    strategySelector();
  }

  private void strategySelector() throws IOException {
    currentLocation = 0;
    long effectiveSize = fileSize - fileLocation;
    if(effectiveSize <= Gelato.DEFAULT_NET_IO_MEM_BUFFER) {
      fitFileToBufferStrategy();
    } else {
      fetchBytesToBufferStrategy();
    }
  }


  private void fetchBytesToBufferStrategy() throws IOException {
    GelatoMessage<ReadRequest, ReadResponse> readRequest = messaging.createReadTransaction();
    readRequest.getMessage().setFileDescriptor(fileDescriptor.getRawFileDescriptor());
    readRequest.getMessage().setBytesToRead(P9Protocol.MAX_MSG_CONTENT_SIZE);
    readRequest.getMessage().setFileOffset(fileLocation);
    messaging.submitMessage(readRequest);

    if(readRequest.getResponse() == null ) {
      throw new IOException("Doble boom");
    }

    Iterator<ReadResponse> iterator = readRequest.iterator();
    int location = 0;
    while(iterator.hasNext()) {
      ReadResponse readResponse = iterator.next();
      ByteEncoder.copyBytesTo(
              readResponse.getData(), buffer, location, readResponse.getData().length - 1);
      location += readResponse.getData().length;
    }
    if(location != fileSize) {
      logger.error("READ MISMATCH");
      throw new IOException("BLO");
    }
    messaging.close(readRequest);
  }

  //Copy the stream to the memory buffer
  private void fitFileToBufferStrategy() throws IOException {
    GelatoMessage<ReadRequest, ReadResponse> readRequest = messaging.createReadTransaction();
    readRequest.getMessage().setFileDescriptor(fileDescriptor.getRawFileDescriptor());
    readRequest.getMessage().setBytesToRead((int)fileSize);
    readRequest.getMessage().setFileOffset(fileLocation);
    messaging.submitMessage(readRequest);

    if(readRequest.getResponse() == null ) {
      throw new IOException("Doble boom");
    }

    if(fileSize > ioNetworkSize) {
      Iterator<ReadResponse> iterator = readRequest.iterator();
      int location = 0;
      while(iterator.hasNext()) {
        ReadResponse readResponse = iterator.next();
        ByteEncoder.copyBytesTo(
                readResponse.getData(), buffer, location, readResponse.getData().length - 1);
        location += readResponse.getData().length;
      }
      if(location != fileSize) {
        logger.error("READ MISMATCH");
        throw new RuntimeException("BLO");
      }
    } else {
      ByteEncoder.copyBytesTo(readRequest.getResponse().getData(), buffer, 0, (int) fileSize);
    }
    messaging.close(readRequest);
  }

  public GelatoInputStream( GelatoMessaging messaging,
    GelatoFileDescriptor descriptor, long ioSize, long fileSize) {

    this.fileSize = fileSize;
    this.messaging = messaging;
    this.fileDescriptor = descriptor;
    this.ioNetworkSize = ioSize;
    try {
      initialise();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  @Override
  public void close() {
    GelatoMessage<CloseRequest, CloseResponse> closeRequest = messaging.createCloseTransaction();
    closeRequest.getMessage().setFileID(fileDescriptor.getRawFileDescriptor());
    messaging.submitMessage(closeRequest);
    if(closeRequest.getResponse() == null ) {
      logger.error("Error closing stream");
    }
    messaging.close(closeRequest);
  }

  @Override
  public int available() {
    if(fileSize < Gelato.DEFAULT_NET_IO_MEM_BUFFER  ) {
      return (int)fileSize;
    }
    return (Gelato.DEFAULT_NET_IO_MEM_BUFFER - currentLocation);
  }

  @Override
  public synchronized int read() throws IOException {
    int byteval = 0;
    if(currentLocation >= Gelato.DEFAULT_NET_IO_MEM_BUFFER) {
      strategySelector();
    } else if( fileLocation >= fileSize) {
      return -1;
    }
    byteval = buffer[currentLocation] & 0xFF;
    currentLocation++;
    fileLocation++;
    return byteval;
  }


}
