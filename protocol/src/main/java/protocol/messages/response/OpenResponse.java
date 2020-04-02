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

public class OpenResponse implements TransactionMessage {
  private int tag;
  private QID fileQID;
  private int sizeIO = P9Protocol.MAX_MSG_CONTENT_SIZE;

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
  public Message toMessage() {
    Message retMessage = new Message();
    int contentSize = P9Protocol.MSG_QID_SIZE + P9Protocol.MSG_INT_SIZE;
    retMessage.tag = tag;
    retMessage.messageType = P9Protocol.ROPEN;
    retMessage.messageSize = contentSize + MessageRaw.minSize;
    retMessage.messageContent = new byte[contentSize];
    int ptr = 0;
    ByteEncoder.encodeQID(fileQID, retMessage.messageContent, ptr);
    ptr += P9Protocol.MSG_QID_SIZE;
    ByteEncoder.encodeInt(sizeIO, retMessage.messageContent, ptr);
    return retMessage;
  }

  public QID getFileQID() {
    return fileQID;
  }

  public void setFileQID(QID fileQID) {
    this.fileQID = fileQID;
  }

  public int getSizeIO() {
    return sizeIO;
  }

  public void setSizeIO(int sizeIO) {
    this.sizeIO = sizeIO;
  }
}
