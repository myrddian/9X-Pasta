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

package gelato.server.manager.implementation;

import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.manager.controllers.GelatoResourceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.messages.Message;
import protocol.messages.response.ErrorMessage;

public class ParallelRequest implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(ParallelRequest.class);
  private Message message;
  private GelatoResourceController handler;
  private GelatoSession session;
  private GelatoConnection connection;
  private GelatoFileDescriptor descriptor;

  public GelatoConnection getConnection() {
    return connection;
  }

  public void setConnection(GelatoConnection connection) {
    this.connection = connection;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  public GelatoResourceController getHandler() {
    return handler;
  }

  public void setHandler(GelatoResourceController handler) {
    this.handler = handler;
  }

  public GelatoSession getSession() {
    return session;
  }

  public void setSession(GelatoSession session) {
    this.session = session;
  }

  public GelatoFileDescriptor getDescriptor() {
    return descriptor;
  }

  public void setDescriptor(GelatoFileDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  @Override
  public void run() {
    if (!handler.processRequest(connection, descriptor, session, message)) {
      ErrorMessage msg = new ErrorMessage();
      msg.setTag(message.tag);
      msg.setErrorMessage("General Failure - Handling Operation ");
      connection.sendMessage(descriptor, msg.toMessage());
      logger.error(
          "Unable to process Message : "
              + Integer.toString(message.messageType)
              + " Resource "
              + handler.getStat().getName());
    }
  }
}
