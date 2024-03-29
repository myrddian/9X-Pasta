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

package protocol.messages.response;

import protocol.ByteEncoder;
import protocol.P9Protocol;
import protocol.QID;
import protocol.messages.Message;
import protocol.messages.TransactionMessage;

public class CreateResponse implements TransactionMessage {

  private int tag;
  private QID serverResource;
  private int ioSize = P9Protocol.MAX_MSG_CONTENT_SIZE;

  @Override
  public void setTransactionId(int transactionId) {
    setTag(transactionId);
  }

  @Override
  public int getTag() {
    return tag;
  }

  @Override
  public void setTag(int tag) {
    this.tag = tag;
  }

  @Override
  public byte messageType() {
    return P9Protocol.RCLOSE;
  }

  @Override
  public Message toMessage() {
    Message rtr = new Message();
    rtr.messageType = P9Protocol.RCREATE;
    rtr.tag = tag;
    int size = P9Protocol.MSG_INT_SIZE + P9Protocol.MSG_QID_SIZE;
    int ptr = 0;
    rtr.messageContent = new byte[size];
    ByteEncoder.encodeQID(serverResource, rtr.messageContent, 0);
    ByteEncoder.encodeInt(ioSize, rtr.messageContent, P9Protocol.MSG_QID_SIZE);
    rtr.messageSize = size + P9Protocol.MIN_MSG_SIZE;
    return rtr;
  }

  public QID getServerResource() {
    return serverResource;
  }

  public void setServerResource(QID serverResource) {
    this.serverResource = serverResource;
  }

  public int getIoSize() {
    return ioSize;
  }

  public void setIoSize(int ioSize) {
    this.ioSize = ioSize;
  }
}
