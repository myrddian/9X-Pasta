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

public class Message  implements TransactionMessage{

    public int messageSize;
    public byte messageType;
    public byte[] messageContent;
    public int tag;

    public int getContentSize() {
        return messageSize - MessageRaw.minSize;
    }

    public MessageRaw toRaw() {
        MessageRaw newRaw = new MessageRaw();
        newRaw.type = this.messageType;
        ByteEncoder.encodeShort(this.tag, newRaw.tag, 0);
        ByteEncoder.encodeInt(this.messageSize, newRaw.size, 0);
        newRaw.data = this.messageContent;
        return newRaw;
    }

    @Override
    public void setTransactionId(int transactionId) {
        setTag(transactionId);
    }

    @Override
    public void setTag(int newTag) {
        tag = newTag;
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public Message toMessage() {
        return this;
    }
}
