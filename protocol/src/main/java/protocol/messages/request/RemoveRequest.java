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

public class RemoveRequest implements TransactionMessage {

    private int tag;
    private int fileDescriptor;


    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public Message toMessage() {
        Message rtr =  new Message();
        rtr.tag = tag;
        rtr.messageType = P9Protocol.TREMOVE;
        rtr.messageSize = P9Protocol.MIN_MSG_SIZE + P9Protocol.MSG_INT_SIZE;
        rtr.messageContent = new byte [P9Protocol.MSG_INT_SIZE];
        ByteEncoder.encodeInt(fileDescriptor, rtr.messageContent, 0);
        return rtr;
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
}
