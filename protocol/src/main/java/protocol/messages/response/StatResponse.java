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

package protocol.messages.response;

import protocol.*;
import protocol.messages.*;

public class StatResponse implements TransactionMessage {
    private int tag;
    private StatStruct statStruct;

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public Message toMessage() {
        Message rtr = new Message();
        rtr.tag  = tag;
        rtr.messageType = P9Protocol.RSTAT;
        rtr.messageContent = statStruct.EncodeStat();
        rtr.messageSize = P9Protocol.MIN_MSG_SIZE + rtr.messageContent.length;
        return rtr;
    }

    @Override
    public void setTag(int tag) {
        this.tag = tag;
    }

    public StatStruct getStatStruct() {
        return statStruct;
    }

    public void setStatStruct(StatStruct statStruct) {
        this.statStruct = statStruct;
    }
}
