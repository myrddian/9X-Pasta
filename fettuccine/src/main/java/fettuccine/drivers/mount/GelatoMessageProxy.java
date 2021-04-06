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

package fettuccine.drivers.mount;

import gelato.GelatoConnection;
import gelato.client.GelatoMessage;
import gelato.server.manager.RequestConnection;

public class GelatoMessageProxy {
  private GelatoMessage forwardedMessage;
  private long timeSent = 0;
  private int queueSize = 0;
  private int originalTransactionId = 0;
  private long connectionDescriptor = 0;

  private RequestConnection requestConnection = new RequestConnection();
  private GelatoConnection originatingSource;

  public GelatoConnection getOriginatingSource() {
    return originatingSource;
  }

  public void setOriginatingSource(GelatoConnection originatingSource) {
    this.originatingSource = originatingSource;
  }

  public GelatoMessage getForwardedMessage() {
    return forwardedMessage;
  }

  public void setForwardedMessage(GelatoMessage forwardedMessage) {
    this.forwardedMessage = forwardedMessage;
  }

  public long getTimeSent() {
    return timeSent;
  }

  public void setTimeSent(long timeSent) {
    this.timeSent = timeSent;
  }

  public int getQueueSize() {
    return queueSize;
  }

  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

  public int getOriginalTransactionId() {
    return originalTransactionId;
  }

  public void setOriginalTransactionId(int originalTransactionId) {
    this.originalTransactionId = originalTransactionId;
  }

  public long getConnectionDescriptor() {
    return connectionDescriptor;
  }

  public void setConnectionDescriptor(long connectionDescriptor) {
    this.connectionDescriptor = connectionDescriptor;
  }

  public String messageId() {
    return MountPoint.generateId(this);
  }

  public RequestConnection getRequestConnection() {
    return requestConnection;
  }

  public void setRequestConnection(RequestConnection requestConnection) {
    this.requestConnection = requestConnection;
  }
}
