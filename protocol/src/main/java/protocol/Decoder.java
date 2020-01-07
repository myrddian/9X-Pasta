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

import java.util.*;

public class Decoder {

    public static MessageRaw decodeBytesToRaw(byte [] bytes) {
        MessageRaw raw = new MessageRaw();

        raw.size = Arrays.copyOfRange(bytes, 0, P9Protocol.MSG_SIZE_HEADER);
        raw.type = bytes[P9Protocol.MSG_INT_SIZE];
        raw.tag = Arrays.copyOfRange(bytes,MessageRaw.tagLocation, MessageRaw.tagLocation + P9Protocol.MSG_TAG_SIZE);
        raw.data = Arrays.copyOfRange(bytes, MessageRaw.minSize, bytes.length);
        return raw;
    }

    public static MessageRaw decodeRawHeader(byte [] bytes) {
        MessageRaw raw = new MessageRaw();
        raw.size = Arrays.copyOfRange(bytes, 0, P9Protocol.MSG_SIZE_HEADER);
        raw.type = bytes[P9Protocol.MSG_INT_SIZE];
        raw.tag = Arrays.copyOfRange(bytes,MessageRaw.tagLocation, MessageRaw.tagLocation + P9Protocol.MSG_TAG_SIZE);
        return raw;
    }

    public static Message decodeToMessage(MessageRaw rawMessage) {
            Message msg = new Message();
            msg.messageSize = ByteEncoder.decodeInt(rawMessage.size,0);
            msg.tag = ByteEncoder.decodeShort(rawMessage.tag, 0);
            msg.messageType = rawMessage.type;
            msg.messageContent = rawMessage.data;
            return msg;
    }

    public static VersionRequest decodeVersionRequest(Message decodeMessage) {
        if(decodeMessage.messageType != P9Protocol.RVERSION &&
                decodeMessage.messageType != P9Protocol.TVERSION) {
            throw new RuntimeException("NOT SUPPORTED");
        }

        VersionRequest request = new VersionRequest();
        request.setMessageTag(decodeMessage.tag);
        request.setMaxMsgSize(ByteEncoder.decodeInt(decodeMessage.messageContent, 0));
        request.setVersion(ByteEncoder.decodeString(decodeMessage.messageContent,4));
        return request;

    }

    public static AuthRequest decodeAuthRequest(Message decodeMessage) {
        if(decodeMessage.messageType != P9Protocol.TAUTH) {
            throw new RuntimeException("NOT SUPPORTED");
        }
        AuthRequest request = new AuthRequest();
        request.setTag(decodeMessage.tag);
        request.setAuthFileID(ByteEncoder.decodeInt(decodeMessage.messageContent, 0));
        request.setUserName(ByteEncoder.decodeString(decodeMessage.messageContent, P9Protocol.MSG_FID_SIZE));
        int nextPosition = ByteEncoder.stringLength(request.getUserName()) + P9Protocol.MSG_FID_SIZE;
        request.setUserAuth(ByteEncoder.decodeString(decodeMessage.messageContent, nextPosition));
        return request;
    }

    public static AuthRequestResponse decodeAuthResponse(Message decodeMessage) {
        if(decodeMessage.messageType != P9Protocol.RAUTH) {
            throw new RuntimeException("NOT SUPPORTED");
        }
        AuthRequestResponse response = new AuthRequestResponse();
        response.setTag(decodeMessage.tag);
        response.setQid(ByteEncoder.decodeQID(decodeMessage.messageContent, 0));
        return response;
    }

    public static ErrorMessage decodeError(Message msg) {
        if(msg.messageType != P9Protocol.RERROR) {
            throw new RuntimeException("Not supported");
        }
        ErrorMessage retVal = new ErrorMessage();
        retVal.setTag(msg.tag);
        retVal.setErrorMessage(ByteEncoder.decodeString(msg.messageContent,0));
        return retVal;
    }

    public static OpenResponse decodeOpenResponse(Message msg) {
        if(msg.messageType != P9Protocol.ROPEN) {
            throw new RuntimeException("Not supported");
        }
        OpenResponse openResponse = new OpenResponse();
        openResponse.setFileQID(ByteEncoder.decodeQID(msg.messageContent, 0));
        openResponse.setSizeIO(ByteEncoder.decodeInt(msg.messageContent, P9Protocol.MSG_QID_SIZE));
        openResponse.setTag(msg.tag);
        return openResponse;
    }

    public static OpenRequest decodeOpenRequest(Message msg) {
        if(msg.messageType != P9Protocol.TOPEN) {
            throw new RuntimeException("Not supported");
        }
        OpenRequest retVal = new OpenRequest();
        retVal.setTag(msg.tag);
        retVal.setFileDescriptor(ByteEncoder.decodeInt(msg.messageContent, 0));
        retVal.setMode(msg.messageContent[P9Protocol.MSG_FID_SIZE]);

        return retVal;
    }

    public static WalkRequest decodeWalkRequest(Message msg) {
        if(msg.messageType != P9Protocol.TWALK) {
            throw new RuntimeException("Not supported");
        }
        WalkRequest retVal = new WalkRequest();
        retVal.setTag(msg.tag);
        retVal.setBaseDescriptor(ByteEncoder.decodeInt(msg.messageContent, 0));
        retVal.setNewDecriptor(ByteEncoder.decodeInt(msg.messageContent, P9Protocol.MSG_FID_SIZE));
        retVal.setTargetFile(ByteEncoder.decodeString(msg.messageContent, P9Protocol.MSG_FID_SIZE * 2));
        return retVal;
    }

    public static AttachRequest decodeAttachRequest(Message msg) {
        AttachRequest ret = new AttachRequest();
        ret.setFid(ByteEncoder.decodeInt(msg.messageContent, 0));
        ret.setAfid(ByteEncoder.decodeInt(msg.messageContent, P9Protocol.MSG_FID_SIZE));
        ret.setNamespace(ByteEncoder.decodeString(msg.messageContent, (P9Protocol.MSG_FID_SIZE * 2)));
        return ret;
    }
}
