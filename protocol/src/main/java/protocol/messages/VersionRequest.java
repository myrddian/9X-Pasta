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

package protocol.messages;

import protocol.Encoder;
import protocol.P9Protocol;

public class VersionRequest implements TransactionMessage {

  private int maxMsgSize = P9Protocol.DEFAULT_MSG_SIZE;
  private String version = P9Protocol.protocolVersion;
  private int messageTag = 0;

  public int getMaxMsgSize() {
    return maxMsgSize;
  }

  public void setMaxMsgSize(int maxMsgSize) {
    this.maxMsgSize = maxMsgSize;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getMessageTag() {
    return messageTag;
  }

  public void setMessageTag(int messageTag) {
    this.messageTag = messageTag;
  }

  @Override
  public void setTransactionId(int transactionId) {
    setTag(transactionId);
  }

  @Override
  public int getTag() {
    return messageTag;
  }

  @Override
  public void setTag(int newTag) {
    messageTag = newTag;
  }

  @Override
  public byte messageType() {
    return P9Protocol.TVERSION;
  }

  @Override
  public Message toMessage() {
    return Encoder.encodeVersionRequest(this).toMessage();
  }
}
