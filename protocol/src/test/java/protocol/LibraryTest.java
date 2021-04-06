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

package protocol;

import org.junit.jupiter.api.Test;
import protocol.messages.Message;
import protocol.messages.MessageRaw;
import protocol.messages.VersionRequest;
import protocol.messages.request.AuthRequest;
import protocol.messages.response.AuthResponse;

class LibraryTest {
  @Test
  void testSomeLibraryMethod() {
    VersionRequest versionRequest = new VersionRequest();
    MessageRaw messageRaw = Encoder.encodeVersionRequest(versionRequest);
    System.out.println(messageRaw.type);
    Message decodedMessage = Decoder.decodeToMessage(messageRaw);
    System.out.println(decodedMessage.messageSize);
    VersionRequest ver = Decoder.decodeVersionRequest(decodedMessage);
    System.out.println(ver.getMaxMsgSize());
    System.out.println(ver.getVersion());

    AuthRequest authRequest = new AuthRequest();
    authRequest.setUserAuth("test-auth");
    authRequest.setUserName("test-user");
    authRequest.setAuthFileID(12345);
    authRequest.setTag(1);

    MessageRaw rawAuth = Encoder.encodeAuthRequest(authRequest);
    byte[] rawByte = Encoder.messageToBytes(rawAuth);
    MessageRaw bytesRaw = Decoder.decodeBytesToRaw(rawByte);
    decodedMessage = Decoder.decodeToMessage(bytesRaw);
    AuthRequest recodedAuth = Decoder.decodeAuthRequest(decodedMessage);
    System.out.println(recodedAuth.getUserAuth());

    AuthResponse authResponse = new AuthResponse();
    authResponse.setTag(1);
    QID testQid = new QID();
    testQid.setLongFileId(12345678);
    authResponse.setQid(testQid);
    rawAuth = Encoder.encodeAuthResponse(authResponse);
    rawByte = Encoder.messageToBytes(rawAuth);
    bytesRaw = Decoder.decodeBytesToRaw(rawByte);
    decodedMessage = Decoder.decodeToMessage(bytesRaw);
    AuthResponse decodedAuthRsp = Decoder.decodeAuthResponse(decodedMessage);
    System.out.println(decodedAuthRsp.getTag());
  }
}
