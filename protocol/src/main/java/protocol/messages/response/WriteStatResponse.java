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

import protocol.P9Protocol;
import protocol.messages.Message;
import protocol.messages.TransactionMessage;

public class WriteStatResponse implements TransactionMessage {

  private int tag;

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
    return P9Protocol.RWSTAT;
  }

  @Override
  public Message toMessage() {
    Message rtr = new Message();
    rtr.tag = tag;
    rtr.messageType = P9Protocol.RWSTAT;
    rtr.messageSize = P9Protocol.MIN_MSG_SIZE;
    rtr.messageContent = null;
    return rtr;
  }
}
