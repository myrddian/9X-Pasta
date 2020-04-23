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

package gelato.server.manager;

import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import gelato.Gelato;
import gelato.GelatoConnection;
import gelato.GelatoDescriptorManager;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.manager.v2.V2Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.P9Protocol;
import protocol.messages.Message;
import protocol.messages.VersionRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GelatoDescriptorHandler {

  final Logger logger = LoggerFactory.getLogger(GelatoDescriptorHandler.class);

  private GelatoConnection serverConnection;
  private Map<GelatoFileDescriptor, GelatoSession> descriptorGelatoSessionMap = new HashMap<>();
  private BlockingQueue<V2Message> readMessageQueue = new LinkedBlockingQueue<>();
  private GelatoSessionHandler sessionHandler;
  private Gelato library;
  private boolean shutdown = false;

  public GelatoDescriptorHandler(
      Gelato library, GelatoConnection server, GelatoSessionHandler sessionHandler1) {
    serverConnection = server;
    this.library = library;
    sessionHandler = sessionHandler1;
  }

  @CiotolaServiceRun
  public void process() {
    while (!isShutdown()) {
      processMessages();
    }
  }

  public void processMessages() {
    try {
      V2Message message = readMessageQueue.take();
      Message msg = message.getMessage();
      GelatoConnection clientConnection = message.getClientConnection();
      GelatoSession session = descriptorGelatoSessionMap.get(message.getDescriptor());

      if (session == null) {
        createDescriptorSession(message.getDescriptor(), msg, clientConnection);
      } else {
        sessionHandler.processRequest(clientConnection, message.getDescriptor(), session, msg);
      }
    } catch (InterruptedException e) {
      logger.error("Service Interrupted",e);
    }
  }

  public void addMessage(V2Message processMessage) {
    try {
      readMessageQueue.put(processMessage);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void createDescriptorSession(
      GelatoFileDescriptor descriptor, Message msg, GelatoConnection connection) {
    if (msg.messageType != P9Protocol.TVERSION) {
      logger.error(
          "Descriptor has not started a valid session "
              + Long.toString(descriptor.getDescriptorId())
              + " "
              + Byte.toString(msg.messageType));
      return;
    }
    library.getTagManager().createTagHandler(descriptor);
    GelatoServerSession newSession = new GelatoServerSession();
    newSession.setManager(new GelatoDescriptorManager());
    newSession.setConnection(connection);
    newSession.setTags(library.getTagManager().getManager(descriptor));
    VersionRequest response = new VersionRequest();
    Message rspVersion = response.toMessage();
    rspVersion.messageType = P9Protocol.RVERSION;
    rspVersion.tag = msg.tag;
    newSession.getTags().registerTag(msg.tag);
    connection.sendMessage(descriptor, rspVersion);
    descriptorGelatoSessionMap.put(descriptor, newSession);
    logger.debug("Started Session for Descriptor " + Long.toString(descriptor.getDescriptorId()));
  }

  @CiotolaServiceStop
  public synchronized void shutdown() {
    shutdown = true;
  }

  @CiotolaServiceStart
  public synchronized void start() {
    shutdown = false;
  }

  public synchronized boolean isShutdown() {
    return shutdown;
  }
}
