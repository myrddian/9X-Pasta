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

package protocol;

import protocol.messages.*;
import protocol.messages.request.*;
import protocol.messages.response.*;

public class Encoder {


    public static byte [] messageToBytes(MessageRaw serialise) {
        int totalSize = ByteEncoder.decodeInt(serialise.size,0);
        byte [] retVal = new byte[totalSize];
        int dataOffset = P9Protocol.MSG_INT_SIZE + 1 + P9Protocol.MSG_TAG_SIZE;
        int dataEnd = totalSize - dataOffset;
        ByteEncoder.copyBytesTo(serialise.size, retVal, 0, P9Protocol.MSG_INT_SIZE);
        retVal[P9Protocol.MSG_INT_SIZE] = serialise.type;
        ByteEncoder.copyBytesTo(serialise.tag, retVal, P9Protocol.MSG_INT_SIZE +1, P9Protocol.MSG_TAG_SIZE );
        ByteEncoder.copyBytesTo(serialise.data, retVal, dataOffset, dataEnd);
        return retVal;
    }


    public static MessageRaw encodeFlushRequest(FlushRequest request) {
        MessageRaw retval = new MessageRaw();
        retval.type = P9Protocol.TFLUSH;
        int size = MessageRaw.minSize + P9Protocol.MSG_TAG_SIZE;
        retval.data = new byte[size];
        ByteEncoder.encodeShort((short)request.getTag(), retval.tag,0);
        ByteEncoder.encodeShort((short)request.getOldtag(), retval.data, 0);
        ByteEncoder.encodeInt(size,retval.size, 0);
        return retval;
    }

    public static MessageRaw encodeFlushResponse(FlushResponse response) {
        MessageRaw retval = new MessageRaw();
        int size = MessageRaw.minSize;
        ByteEncoder.encodeInt(size, retval.size, 0);
        ByteEncoder.encodeShort(response.getTag(),retval.tag, 0);
        retval.type = P9Protocol.RFLUSH;
        return retval;
    }



    public static MessageRaw encodeVersionRequest(VersionRequest request){
        MessageRaw rawMessage = new MessageRaw();
        rawMessage.type = P9Protocol.TVERSION;
        int dataSegmentSize = P9Protocol.MSG_FID_SIZE + ByteEncoder.stringLength(request.getVersion());
        rawMessage.data = new byte[dataSegmentSize];
        ByteEncoder.encodeInt(request.getMaxMsgSize(),rawMessage.data,0);
        ByteEncoder.encodeString(rawMessage.data, P9Protocol.MSG_FID_SIZE, request.getVersion());
        int totalSize = MessageRaw.minSize + dataSegmentSize;
        ByteEncoder.encodeInt(totalSize,rawMessage.size, 0);
        ByteEncoder.encodeShort(request.getMessageTag(), rawMessage.tag, 0);
        return rawMessage;
    }

    public static MessageRaw encodeAuthResponse(AuthRequestResponse response) {
        MessageRaw raw = new MessageRaw();
        raw.type = P9Protocol.RAUTH;
        int size = MessageRaw.minSize + P9Protocol.MSG_QID_SIZE;
        raw.data = new byte[P9Protocol.MSG_QID_SIZE];
        ByteEncoder.encodeInt(size, raw.size, 0);
        ByteEncoder.encodeShort(response.getTag(), raw.tag, 0);
        ByteEncoder.encodeQID(response.getQid(), raw.data, 0);
        return raw;
    }


    public static MessageRaw encodeAuthRequest(AuthRequest request) {
        MessageRaw messageRaw = new MessageRaw();
        messageRaw.type = P9Protocol.TAUTH;
        byte [] userName = ByteEncoder.encodeStringToBuffer(request.getUserName());
        byte [] userAuth = ByteEncoder.encodeStringToBuffer(request.getUserAuth());
        ByteEncoder.encodeShort(request.getTag(), messageRaw.tag, 0);
        int dataSize = P9Protocol.MSG_FID_SIZE + userAuth.length + userName.length;
        messageRaw.data = new byte[dataSize];
        int dataPosition = 0;
        ByteEncoder.encodeInt(request.getAuthFileID(), messageRaw.data, dataPosition);
        dataPosition += P9Protocol.MSG_FID_SIZE;
        ByteEncoder.copyBytesTo(userName, messageRaw.data, dataPosition, userName.length);
        dataPosition += userName.length;
        ByteEncoder.copyBytesTo(userAuth, messageRaw.data, dataPosition, userAuth.length);
        int totalSize = MessageRaw.minSize + dataSize;
        ByteEncoder.encodeInt(totalSize, messageRaw.size, 0);
        return messageRaw;
    }

    public static MessageRaw encodeError(ErrorMessage msg) {
        MessageRaw messageRaw = new MessageRaw();
        messageRaw.type = P9Protocol.RERROR;
        int errorLen = ByteEncoder.stringLength(msg.getErrorMessage());
        int size = MessageRaw.minSize + errorLen;
        messageRaw.data = new byte[size];
        ByteEncoder.encodeInt(size, messageRaw.size, 0);
        ByteEncoder.encodeShort(msg.getTag(), messageRaw.tag, 0);
        ByteEncoder.encodeString(messageRaw.data, 0 , msg.getErrorMessage());
        return messageRaw;
    }


}
