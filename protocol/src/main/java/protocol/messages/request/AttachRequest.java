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

public class AttachRequest implements TransactionMessage {
    private int fid;
    private int afid;
    private String username;
    private int tag;
    private String namespace;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public int getAfid() {
        return afid;
    }

    public void setAfid(int afid) {
        this.afid = afid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public void setTransactionId(int transactionId) {
        setTag(transactionId);
    }

    @Override
    public void setTag(int tag) {
        this.tag = tag;
    }

    @Override
    public Message toMessage() {
        Message retVal = new Message();
        retVal.messageType = P9Protocol.TATTACH;
        retVal.tag = tag;
        byte [] userName = ByteEncoder.encodeStringToBuffer(username);
        byte [] userAuth = ByteEncoder.encodeStringToBuffer(namespace);
        int dataSize = (P9Protocol.MSG_FID_SIZE * 2) + userAuth.length + userName.length;
        retVal.messageContent = new byte[dataSize];

        int dataPosition = 0;
        ByteEncoder.encodeInt(fid, retVal.messageContent, dataPosition);
        dataPosition += P9Protocol.MSG_FID_SIZE;
        ByteEncoder.encodeInt(afid, retVal.messageContent, dataPosition);
        dataPosition += P9Protocol.MSG_FID_SIZE;
        ByteEncoder.copyBytesTo(userName, retVal.messageContent, dataPosition, userName.length);
        dataPosition += userName.length;
        ByteEncoder.copyBytesTo(userAuth, retVal.messageContent, dataPosition, userAuth.length);
        int totalSize = MessageRaw.minSize + dataSize;
        retVal.messageSize = totalSize;

        return retVal;
    }


}
