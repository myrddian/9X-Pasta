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

package gelato.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Decoder;
import protocol.Encoder;
import protocol.messages.Message;
import protocol.messages.MessageRaw;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class TCPTransport implements GelatoTransport, Runnable {

  final Logger logger = LoggerFactory.getLogger(TCPTransport.class);
  private BlockingQueue<Message> readMessageQueue = new LinkedBlockingQueue<>();
  private BlockingQueue<Message> writeMessageQueue = new LinkedBlockingQueue<>();
  private boolean closeConnction = false;
  private int headerSize = MessageRaw.minSize;
  private int readSize = 0;
  private byte[] minHeaderBuffer = new byte[headerSize];
  private long sleepCycle = 10;
  private long dutyCycle = 0;
  private long lastProcessed = 0;


  @Override
  public synchronized void close() {
    logger.info("Closing Connection");
    closeConnction = true;
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

  @Override
  public void run() {
    try {
      processMessages();
    } catch (IOException e) {
      logger.error("IOException has occurred in the thread -", e);
    } catch (InterruptedException e) {
      logger.error("Interrupted", e);
    }
  }

  public abstract InputStream getSocketInputStream();

  public abstract OutputStream getSocketOutputStream();

  public abstract void closeStream();

  private void processOutbound(OutputStream os) throws InterruptedException, IOException {
    int messages = writeMessageQueue.size();
    if(messages == 0 )  {
      dutyCycle = System.currentTimeMillis()  - lastProcessed;
      if(dutyCycle >= sleepCycle) {
        dutyCycle = sleepCycle;
      }
      Thread.sleep(dutyCycle);
    }
    for (int counter = 0; counter < messages; ++counter) {
      Message outbound = writeMessageQueue.take();
      MessageRaw raw = outbound.toRaw();
      byte[] outBytes = Encoder.messageToBytes(raw);
      os.write(outBytes);
    }
    lastProcessed = System.currentTimeMillis();
  }

  private void processInbound(InputStream is) throws InterruptedException, IOException {
    // peek if there are bytes available otherwise just skip
    readSize = is.available();
    if (readSize == 0) return;

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
    readMessageQueue.add(msg);
  }

  private void processMessages() throws IOException, InterruptedException {
    InputStream is = getSocketInputStream();
    OutputStream os = getSocketOutputStream();
    while (isOpen()) {
      processOutbound(os);
      processInbound(is);
    }
    closeStream();
  }
}
