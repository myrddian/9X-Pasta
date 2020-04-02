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

import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import protocol.messages.TransactionMessage;

public class RequestConnection {
  private GelatoSession session;
  private GelatoConnection connection;
  private GelatoFileDescriptor descriptor;
  private int transactionId;

  public void reply(TransactionMessage msg) {
    msg.setTag(getTransactionId());
    connection.sendMessage(descriptor, msg.toMessage());
  }

  public GelatoSession getSession() {
    return session;
  }

  public void setSession(GelatoSession session) {
    this.session = session;
  }

  public GelatoConnection getConnection() {
    return connection;
  }

  public void setConnection(GelatoConnection connection) {
    this.connection = connection;
  }

  public GelatoFileDescriptor getDescriptor() {
    return descriptor;
  }

  public void setDescriptor(GelatoFileDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  public int getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(int transactionId) {
    this.transactionId = transactionId;
  }
}
