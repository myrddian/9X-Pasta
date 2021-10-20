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

import ciotola.actor.SinkAgent;
import ciotola.actor.SourceRecord;
import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import gelato.client.GelatoMessage;
import gelato.client.GelatoMessaging;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Encoder;
import protocol.P9Protocol;
import protocol.messages.MessageRaw;

public class ClientSideOutTcpWrite implements SinkAgent<GelatoMessage> {

  private final Logger logger = LoggerFactory.getLogger(ClientSideOutTcpWrite.class);
  private OutputStream socketOutputStream;
  private boolean shutdown = false;
  private int currentTagClient = 0;
  private GelatoMessaging messaging;

  public ClientSideOutTcpWrite(OutputStream networkStream, GelatoMessaging messaging) {
    logger.debug("Client side output steamer starting");
    socketOutputStream = networkStream;
    this.messaging = messaging;
  }

  public  int generateTag() {
    if (currentTagClient > 65000) {
      currentTagClient = 1;
    } else {
      ++currentTagClient;
    }
    return currentTagClient;
  }

  public boolean isShutdown() {
    return shutdown;
  }
  public void shutdown() {
    shutdown = true;
  }

  @Override
  public void onRecord(SourceRecord<GelatoMessage> record) {
    int msgTag = generateTag();
    GelatoMessage outbound = record.getValue();
    if (outbound.messageType() == P9Protocol.TVERSION) {
      outbound.setTag(P9Protocol.NO_TAG);
      currentTagClient = 0;
    } else {
      outbound.setTag(msgTag);
    }
    if (outbound.getTag() < 0) {
      outbound.setTag(0);
    }
    messaging.addFuture(outbound);
    MessageRaw raw = outbound.toMessage().toRaw();
    byte[] outBytes = Encoder.messageToBytes(raw);
    try {
      socketOutputStream.write(outBytes);
    } catch (IOException e) {
      logger.error("Error",e);
    }
  }

}
