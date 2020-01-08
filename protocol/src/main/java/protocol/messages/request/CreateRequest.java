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

public class CreateRequest implements TransactionMessage {
    private int tag;
    private int permission;
    private byte mode;
    private String fileName;
    private int fileDescriptor;

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
        Message rtr = new Message();

        rtr.tag = tag;
        rtr.messageType = P9Protocol.TCREATE;
        byte []encodedName = ByteEncoder.encodeStringToBuffer(fileName);
        int totalSize = encodedName.length + ( P9Protocol.MSG_INT_SIZE * 2 ) + 1;
        rtr.messageContent = new byte[totalSize];
        int ptr = 0;
        ByteEncoder.encodeInt(fileDescriptor, rtr.messageContent, 0);
        ptr += P9Protocol.MSG_INT_SIZE;
        ByteEncoder.copyBytesTo(encodedName,rtr.messageContent, ptr, encodedName.length);
        ptr += encodedName.length;
        ByteEncoder.encodeInt(permission, rtr.messageContent,ptr);
        ptr += P9Protocol.MSG_INT_SIZE;
        rtr.messageContent[ptr] = mode;
        rtr.messageSize = MessageRaw.minSize + totalSize;
        return rtr;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public byte getMode() {
        return mode;
    }

    public void setMode(byte mode) {
        this.mode = mode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileDescriptor() {
        return fileDescriptor;
    }

    public void setFileDescriptor(int fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }
}
