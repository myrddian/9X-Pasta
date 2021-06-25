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

package gelato.server.manager.v2;

import ciotola.CiotolaConnectionService;
import gelato.GelatoFileDescriptor;
import gelato.transport.GelatoTransport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Decoder;
import protocol.Encoder;
import protocol.messages.Message;
import protocol.messages.MessageRaw;

public class V2TCPTransport implements GelatoTransport, CiotolaConnectionService {

  final Logger logger = LoggerFactory.getLogger(V2TCPTransport.class);

  private BlockingQueue<Message> readMessageQueue = new LinkedBlockingQueue<>();
  private BlockingQueue<Message> writeMessageQueue = new LinkedBlockingQueue<>();
  private boolean closeConnction = false;
  private int headerSize = MessageRaw.minSize;
  private int readSize = 0;
  private byte[] minHeaderBuffer = new byte[headerSize];
  private V2TransportProxy proxy = new V2TransportProxy(this);
  private Socket clientSocket;
  private GelatoFileDescriptor descriptor;
  private long processTime = System.currentTimeMillis();
  private V2ClientDescriptorHandler nextHandler;
  private int svcProxy = CiotolaConnectionService.NO_PROXY_TERMINATION;

  public V2TCPTransport(
      Socket cliSocket,
      GelatoFileDescriptor connectionDescriptor,
      V2ClientDescriptorHandler clientDescriptorHandler) {
    descriptor = connectionDescriptor;
    clientSocket = cliSocket;
    nextHandler = clientDescriptorHandler;
  }

  public int getSourcePort() {
    return clientSocket.getPort();
  }

  public String getAddress() {
    return clientSocket.getInetAddress().toString();
  }

  @Override
  public synchronized void close() {
    logger.debug("Closing Connection");
    closeConnction = true;
    nextHandler.shutdown();
    nextHandler = null;
    closeStream();
  }

  @Override
  public synchronized boolean isOpen() {
    return !closeConnction;
  }

  @Override
  public boolean writeMessage(Message messageRaw) {
    try {
      writeMessageQueue.put(messageRaw);
      return true;
    } catch (InterruptedException e) {
      logger.error("Interrupted - Nothing Written to transport", e);
    }
    return false;
  }

  @Override
  public Message readMessage() {
    try {
      return readMessageQueue.take();
    } catch (InterruptedException e) {
      logger.error("Interrupted - Returning Null", e);
      return null;
    }
  }

  @Override
  public int size() {
    return readMessageQueue.size();
  }

  private void processOutbound(OutputStream os) throws InterruptedException, IOException {

    Message outbound = writeMessageQueue.take();
    MessageRaw raw = outbound.toRaw();
    byte[] outBytes = Encoder.messageToBytes(raw);
    os.write(outBytes);
  }

  private void processInbound(InputStream is) throws InterruptedException, IOException {
    // peek if there are bytes available otherwise just skip
    readSize = is.available();
    if (readSize == 0) {
      return;
    }

    MessageRaw minMessage;
    int rsize = 0;
    // Read the header of the incoming message
    if (readSize >= headerSize) {
      rsize = is.read(minHeaderBuffer);
      if (rsize == -1 || rsize != headerSize) {
        logger.error("Invalid size detected for header");
        throw new IOException("??-WTF");
      }
    } else {
      for (int byteCount = 0; byteCount < headerSize; ++byteCount) {
        int val = is.read();
        if (val != -1) {
          minHeaderBuffer[byteCount] = (byte) (val & 0xFF);
        } else {
          logger.error("Unable to read header");
          throw new IOException("WTF");
        }
      }
    }
    minMessage = Decoder.decodeRawHeader(minHeaderBuffer);
    Message msg = minMessage.toMessage();
    int bytesToRead = msg.getContentSize();
    byte[] content = new byte[bytesToRead];
    rsize = is.read(content);
    msg.messageContent = content;
    V2Message newMessage = new V2Message();
    newMessage.setMessage(msg);
    newMessage.setDescriptor(descriptor);
    newMessage.setClientConnection(proxy);
    nextHandler.addMessage(newMessage);

    if (nextHandler.isShutdown()) {
      notifyClose();
    }
  }

  public InputStream getSocketInputStream() {
    try {
      return clientSocket.getInputStream();
    } catch (IOException e) {
      logger.error("Unable to open Input stream", e);
    }
    return null;
  }

  public OutputStream getSocketOutputStream() {
    try {
      return clientSocket.getOutputStream();
    } catch (IOException e) {
      logger.error("Unable to open Output stream", e);
    }
    return null;
  }

  public void closeStream() {
    try {
      clientSocket.close();
    } catch (IOException e) {
      logger.error("Unable to close socket", e);
    }
  }

  @Override
  public int bytesToProcessInbound() throws IOException {
    return getSocketInputStream().available();
  }

  @Override
  public int messagesToProcessOutbound() {
    return writeMessageQueue.size();
  }

  @Override
  public long getConnectionId() {
    return descriptor.getDescriptorId();
  }

  @Override
  public void processInbound() throws IOException, InterruptedException {
    InputStream is = getSocketInputStream();
    processInbound(is);
  }

  @Override
  public void processOutbound() throws IOException, InterruptedException {
    OutputStream os = getSocketOutputStream();
    processOutbound(os);
  }

  @Override
  public void notifyClose() {
    close();
  }

  @Override
  public boolean isRunning() {
    return !closeConnction;
  }

  @Override
  public long getProcessedTime() {
    return processTime;
  }

  @Override
  public void setProcessedTime(long time) {
    processTime = time;
  }

  @Override
  public boolean isClosed() {
    return closeConnction;
  }

  @Override
  public int getProxyId() {
    return svcProxy;
  }

  public int getSvcProxy() {
    return svcProxy;
  }

  public void setSvcProxy(int svcProxy) {
    this.svcProxy = svcProxy;
  }
}
