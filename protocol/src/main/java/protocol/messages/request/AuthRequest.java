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

import protocol.Encoder;
import protocol.P9Protocol;
import protocol.messages.Message;
import protocol.messages.TransactionMessage;

public class AuthRequest implements TransactionMessage {

  private String userName;
  private String userAuth;
  private int AuthFileID;
  private int tagValue;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserAuth() {
    return userAuth;
  }

  public void setUserAuth(String userAuth) {
    this.userAuth = userAuth;
  }

  public int getAuthFileID() {
    return AuthFileID;
  }

  public void setAuthFileID(int authFileID) {
    AuthFileID = authFileID;
  }

  @Override
  public int getTag() {
    return tagValue;
  }

  @Override
  public void setTag(int tagValue) {
    this.tagValue = tagValue;
  }

  @Override
  public byte messageType() {
    return P9Protocol.TAUTH;
  }

  @Override
  public void setTransactionId(int transactionId) {
    setTag(transactionId);
  }

  @Override
  public Message toMessage() {
    return Encoder.encodeAuthRequest(this).toMessage();
  }
}
