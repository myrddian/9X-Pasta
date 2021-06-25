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
import protocol.messages.MessageRaw;
import protocol.messages.TransactionMessage;

public class AttachResponse implements TransactionMessage {

  private QID serverID;
  private int tag;

  public QID getServerID() {
    return serverID;
  }

  public void setServerID(QID serverID) {
    this.serverID = serverID;
  }

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
    return P9Protocol.RATTACH;
  }

  @Override
  public Message toMessage() {
    Message raw = new Message();
    raw.tag = this.tag;
    raw.messageType = P9Protocol.RATTACH;
    int size = MessageRaw.minSize + P9Protocol.MSG_QID_SIZE;
    raw.messageSize = size;
    raw.messageContent = new byte[P9Protocol.MSG_QID_SIZE];
    ByteEncoder.encodeQID(serverID, raw.messageContent, 0);
    return raw;
  }
}
