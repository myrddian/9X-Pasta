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

public class WalkRequest implements TransactionMessage {

    private int messageTag = 0;
    private String targetFile;
    private int pathSize = 1;
    private int baseDescriptor = 0;
    private int newDescriptor = 0;

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
        Message returnMessage = new Message();
        returnMessage.tag = messageTag;
        returnMessage.messageType  = P9Protocol.TWALK;
        byte []stringPath = ByteEncoder.encodeStringToBuffer(targetFile);
        int contentSize = stringPath.length + (2 * P9Protocol.MSG_FID_SIZE) + P9Protocol.MSG_TAG_SIZE;
        returnMessage.messageContent = new byte[contentSize];
        ByteEncoder.encodeInt(baseDescriptor, returnMessage.messageContent, 0);
        ByteEncoder.encodeInt(newDescriptor, returnMessage.messageContent, P9Protocol.MSG_FID_SIZE);
        ByteEncoder.encodeShort(pathSize, returnMessage.messageContent, P9Protocol.MSG_FID_SIZE * 2);
        ByteEncoder.copyBytesTo(stringPath, returnMessage.messageContent,(P9Protocol.MSG_FID_SIZE * 2)+ P9Protocol.MSG_TAG_SIZE, stringPath.length );
        returnMessage.messageSize = MessageRaw.minSize + contentSize;
        return returnMessage;
    }

    public int getMessageTag() {
        return messageTag;
    }

    public void setMessageTag(int messageTag) {
        this.messageTag = messageTag;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public int getPathSize() {
        return pathSize;
    }

    public void setPathSize(int pathSize) {
        this.pathSize = pathSize;
    }

    public int getBaseDescriptor() {
        return baseDescriptor;
    }

    public void setBaseDescriptor(int baseDescriptor) {
        this.baseDescriptor = baseDescriptor;
    }

    public int getNewDecriptor() {
        return newDescriptor;
    }

    public void setNewDecriptor(int newDescriptor) {
        this.newDescriptor = newDescriptor;
    }
}
