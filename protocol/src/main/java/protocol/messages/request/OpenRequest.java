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

package protocol.messages.request;

import protocol.*;
import protocol.messages.*;

public class OpenRequest  implements TransactionMessage {
    private int tag;
    private int fileDescriptor;
    private byte mode;

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

    public int getFileDescriptor() {
        return fileDescriptor;
    }

    public void setFileDescriptor(int fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    public byte getMode() {
        return mode;
    }

    public void setMode(byte mode) {
        this.mode = mode;
    }

    @Override
    public Message toMessage() {
        Message retVal = new Message();
        retVal.messageType = P9Protocol.TOPEN;
        retVal.tag = tag;
        retVal.messageContent = new byte[P9Protocol.MSG_FID_SIZE + P9Protocol.MSG_TYPE_SIZE];
        ByteEncoder.encodeInt(getFileDescriptor(), retVal.messageContent, 0);
        retVal.messageContent[P9Protocol.MSG_FID_SIZE] = mode;
        retVal.messageSize = MessageRaw.minSize + P9Protocol.MSG_FID_SIZE + P9Protocol.MSG_TYPE_SIZE;
        return retVal;
    }
}
