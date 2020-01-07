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

import protocol.*;

public class VersionRequest  implements TransactionMessage{

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

    private int maxMsgSize = P9Protocol.DEFAULT_MSG_SIZE;
    private String version = P9Protocol.protocolVersion;
    private int messageTag = 0;

    public int getMessageTag() {
        return messageTag;
    }

    public void setMessageTag(int messageTag) {
        this.messageTag = messageTag;
    }

    @Override
    public void setTag(int newTag) {
        messageTag = newTag;
    }

    @Override
    public int getTag() {
        return messageTag;
    }

    @Override
    public Message toMessage() {
        return Encoder.encodeVersionRequest(this).toMessage();
    }
}
