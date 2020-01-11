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

public class WriteRequest implements TransactionMessage {
    private int tag;
    private int fileDescriptor;
    private long fileOffset;
    private int byteCount;
    private byte [] writeData;

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
        if(writeData == null) {
            return null;
        }
        byteCount = writeData.length;
        Message rtr = new Message();
        rtr.messageType = P9Protocol.TWRITE;
        rtr.tag = tag;
        int contentSize = writeData.length + P9Protocol.MSG_INT_SIZE;
        contentSize += P9Protocol.MSG_LONG_SIZE;
        contentSize += P9Protocol.MSG_INT_SIZE;
        rtr.messageContent = new byte[contentSize];
        int ptr = 0;
        ByteEncoder.encodeInt(fileDescriptor, rtr.messageContent, ptr);
        ptr += P9Protocol.MSG_INT_SIZE;
        ByteEncoder.encodeLong(fileOffset, rtr.messageContent, ptr);
        ptr += P9Protocol.MSG_LONG_SIZE;
        ByteEncoder.encodeInt(byteCount, rtr.messageContent, ptr);
        ptr += P9Protocol.MSG_INT_SIZE;
        ByteEncoder.copyBytesTo(writeData, rtr.messageContent, ptr, byteCount);
        rtr.messageSize = P9Protocol.MIN_MSG_SIZE + contentSize;
        return rtr;
    }

    public int getFileDescriptor() {
        return fileDescriptor;
    }

    public void setFileDescriptor(int fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public void setFileOffset(long fileOffset) {
        this.fileOffset = fileOffset;
    }

    public int getByteCount() {
        return byteCount;
    }

    public void setByteCount(int byteCount) {
        this.byteCount = byteCount;
    }

    public byte[] getWriteData() {
        return writeData;
    }

    public void setWriteData(byte[] writeData) {
        this.writeData = writeData;
    }
}
