/*
 * Copyright (c) 2021.  Enzo Reyes Licensed under the Apache License, Version 2.0 (the "License");   you may
 * not use this file except in compliance with the License.   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package gelato.client.file;

import gelato.Gelato;
import gelato.GelatoFileDescriptor;
import gelato.client.GelatoMessage;
import gelato.client.GelatoMessaging;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.messages.request.CloseRequest;
import protocol.messages.request.WriteRequest;
import protocol.messages.response.CloseResponse;
import protocol.messages.response.WriteResponse;

public class GelatoOutputStream extends OutputStream {

  private final Logger logger = LoggerFactory.getLogger(GelatoOutputStream.class);
  private byte[] buffer = new byte[Gelato.DEFAULT_NET_IO_MEM_BUFFER];
  private int bufferPtr = 0;
  private long ioNetworkSize = 0;
  private GelatoMessaging messaging;
  private GelatoFileDescriptor fileDescriptor;
  private boolean closed = false;

  public GelatoOutputStream(
      GelatoMessaging messaging, GelatoFileDescriptor descriptor, long ioSize) {

    this.messaging = messaging;
    this.fileDescriptor = descriptor;
    this.ioNetworkSize = ioSize;
  }

  private void sendMessage(int from, int size) throws IOException {

    GelatoMessage<WriteRequest, WriteResponse> writeMessage = messaging.createWriteTransaction();
    writeMessage.getMessage().setFileDescriptor(fileDescriptor.getRawFileDescriptor());
    writeMessage.getMessage().setByteCount(bufferPtr);
    writeMessage.getMessage().setWriteData(Arrays.copyOfRange(buffer, from, size));
    messaging.submitAndClose(writeMessage);
  }

  private void multiBufferFlush() throws IOException {
    int flushCounter = 0;
    while (flushCounter != buffer.length) {
      int totalBytes = buffer.length - flushCounter;
      if (totalBytes > ioNetworkSize) {
        totalBytes = (int) ioNetworkSize;
      }
      sendMessage(flushCounter, totalBytes);
      flushCounter += totalBytes;
    }
  }

  private void singleBufferFlush() throws IOException {
    sendMessage(0, bufferPtr);
  }

  @Override
  public void flush() throws IOException {
    if (closed) {
      throw new IOException("Stream CLOSED");
    }
    if (bufferPtr > ioNetworkSize) {
      multiBufferFlush();
    } else {
      singleBufferFlush();
    }
    bufferPtr = 0;
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      flush();
      closed = true;
      GelatoMessage<CloseRequest, CloseResponse> closeRequest = messaging.createCloseTransaction();
      closeRequest.getMessage().setFileID(fileDescriptor.getRawFileDescriptor());
      messaging.submitAndClose(closeRequest);
    }
  }

  @Override
  public void write(int b) throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
    if (bufferPtr >= buffer.length) {
      flush();
    }
    buffer[bufferPtr] = (byte) b;
    bufferPtr++;
  }
}
