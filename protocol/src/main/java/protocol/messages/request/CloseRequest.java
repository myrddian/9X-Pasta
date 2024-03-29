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

package protocol.messages.request;

import protocol.ByteEncoder;
import protocol.P9Protocol;
import protocol.messages.Message;
import protocol.messages.MessageRaw;
import protocol.messages.TransactionMessage;

public class CloseRequest implements TransactionMessage {

  private int tag;
  private int fileID;

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
    return P9Protocol.TCLOSE;
  }

  @Override
  public Message toMessage() {
    Message rtr = new Message();
    rtr.messageType = P9Protocol.TCLOSE;
    rtr.tag = tag;
    rtr.messageSize = MessageRaw.minSize + P9Protocol.MSG_INT_SIZE;
    rtr.messageContent = new byte[P9Protocol.MSG_INT_SIZE];
    ByteEncoder.encodeInt(fileID, rtr.messageContent, 0);
    return rtr;
  }

  @Override
  public void setTransactionId(int transactionId) {
    setTag(transactionId);
  }

  public int getFileID() {
    return fileID;
  }

  public void setFileID(int fileID) {
    this.fileID = fileID;
  }
}
