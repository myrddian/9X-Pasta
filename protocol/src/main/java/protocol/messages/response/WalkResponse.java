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

package protocol.messages.response;

import protocol.ByteEncoder;
import protocol.P9Protocol;
import protocol.QID;
import protocol.messages.Message;
import protocol.messages.MessageRaw;
import protocol.messages.TransactionMessage;

public class WalkResponse implements TransactionMessage {

  private int tag;
  private int numOfQid = 1;
  private QID newQID;

  @Override
  public void setTransactionId(int transactionId) {
    setTag(transactionId);
  }

  @Override
  public int getTag() {
    return tag;
  }

  @Override
  public void setTag(int newTag) {
    tag = newTag;
  }

  @Override
  public Message toMessage() {
    Message retMessage = new Message();
    retMessage.messageType = P9Protocol.RWALK;
    retMessage.tag = tag;
    int size = P9Protocol.MSG_TAG_SIZE + P9Protocol.MSG_QID_SIZE;
    retMessage.messageContent = new byte[size];
    retMessage.messageSize = MessageRaw.minSize + size;
    ByteEncoder.encodeShort(numOfQid, retMessage.messageContent, 0);
    ByteEncoder.encodeQID(newQID, retMessage.messageContent, P9Protocol.MSG_TAG_SIZE);
    return retMessage;
  }

  public int getNumOfQid() {
    return numOfQid;
  }

  public QID getQID() {
    return newQID;
  }

  public void setQID(QID newQID) {
    this.newQID = newQID;
  }
}
