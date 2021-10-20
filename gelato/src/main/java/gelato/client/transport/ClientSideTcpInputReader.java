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

package gelato.client.transport;

import ciotola.actor.AgentPort;
import ciotola.actor.SourceProducer;
import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import gelato.client.GelatoMessaging;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Decoder;
import protocol.messages.Message;
import protocol.messages.MessageRaw;

public class ClientSideTcpInputReader implements SourceProducer<Message> {

  public static final String INCORRECT_HEADER = "Incorrect header";
  public static final String INVALID_BYTE_COUNT =
      "Number of bytes read from stream does not match required amount";
  private final Logger logger = LoggerFactory.getLogger(ClientSideTcpInputReader.class);
  private InputStream netWorkInputStream;
  private byte[] minHeaderBuffer = new byte[MessageRaw.minSize];
  private boolean shutdown = false;
  private boolean isReady = true;
  private GelatoMessaging messaging;

  public ClientSideTcpInputReader(InputStream inputStream, GelatoMessaging broker) {
    netWorkInputStream = inputStream;
    messaging = broker;
  }

  public Message getMessage() throws IOException {
    isReady = false;
    for (int byteCount = 0; byteCount < MessageRaw.minSize; ++byteCount) {
      int val = netWorkInputStream.read();
      if (val != -1) {
        minHeaderBuffer[byteCount] = (byte) (val & 0xFF);
      } else {
        logger.error(INCORRECT_HEADER);
        shutdown();
        throw new IOException(INCORRECT_HEADER);
      }
    }
    MessageRaw minMessage = Decoder.decodeRawHeader(minHeaderBuffer);
    Message msg = minMessage.toMessage();
    int bytesToRead = msg.getContentSize();
    byte[] content = new byte[bytesToRead];
    int rsize = netWorkInputStream.read(content);
    if (rsize != bytesToRead) {
      logger.error(INVALID_BYTE_COUNT);
      shutdown();
      throw new IOException(INVALID_BYTE_COUNT);
    }
    msg.messageContent = content;
    isReady = true;
    return msg;
  }

  @Override
  public void execute(AgentPort<Message> target) {
    try {
      target.write(getMessage());
    } catch (IOException e) {
      logger.error("Client side input error", e);
      shutdown();
    }
  }

  @Override
  public boolean isReady() {
    return !shutdown;
  }

  public void shutdown() {
    shutdown = true;
  }

  public boolean isShutdown() {
    return shutdown;
  }


}
