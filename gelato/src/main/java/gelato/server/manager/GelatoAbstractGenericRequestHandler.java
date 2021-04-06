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

package gelato.server.manager;

import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import protocol.messages.response.ErrorMessage;

public abstract class GelatoAbstractGenericRequestHandler implements GenericRequestHandler {

  public void sendErrorMessage(RequestConnection connection, String message) {
    ErrorMessage msg = new ErrorMessage();
    msg.setTag(connection.getTransactionId());
    msg.setErrorMessage(message);
    connection.reply(msg);
  }

  public void sendErrorMessage(
      GelatoConnection connection, GelatoFileDescriptor descriptor, int tag, String message) {
    ErrorMessage msg = new ErrorMessage();
    msg.setTag(tag);
    msg.setErrorMessage(message);
    connection.sendMessage(descriptor, msg.toMessage());
  }

  public RequestConnection createConnection(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      int tag) {
    RequestConnection con = new RequestConnection();
    con.setConnection(connection);
    con.setDescriptor(descriptor);
    con.setSession(session);
    con.setTransactionId(tag);
    return con;
  }
}
