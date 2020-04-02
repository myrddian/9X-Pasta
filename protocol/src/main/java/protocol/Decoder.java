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

import protocol.messages.Message;
import protocol.messages.MessageRaw;
import protocol.messages.VersionRequest;
import protocol.messages.request.AttachRequest;
import protocol.messages.request.AuthRequest;
import protocol.messages.request.CloseRequest;
import protocol.messages.request.CreateRequest;
import protocol.messages.request.FlushRequest;
import protocol.messages.request.OpenRequest;
import protocol.messages.request.ReadRequest;
import protocol.messages.request.RemoveRequest;
import protocol.messages.request.StatRequest;
import protocol.messages.request.WalkRequest;
import protocol.messages.request.WriteRequest;
import protocol.messages.request.WriteStatRequest;
import protocol.messages.response.AttachResponse;
import protocol.messages.response.AuthResponse;
import protocol.messages.response.CloseResponse;
import protocol.messages.response.CreateResponse;
import protocol.messages.response.ErrorMessage;
import protocol.messages.response.FlushResponse;
import protocol.messages.response.OpenResponse;
import protocol.messages.response.ReadResponse;
import protocol.messages.response.RemoveResponse;
import protocol.messages.response.StatResponse;
import protocol.messages.response.WalkResponse;
import protocol.messages.response.WriteResponse;
import protocol.messages.response.WriteStatResponse;

import java.util.Arrays;

public class Decoder {

  public static MessageRaw decodeBytesToRaw(byte[] bytes) {
    MessageRaw raw = new MessageRaw();

    raw.size = Arrays.copyOfRange(bytes, 0, P9Protocol.MSG_SIZE_HEADER);
    raw.type = bytes[P9Protocol.MSG_INT_SIZE];
    raw.tag =
        Arrays.copyOfRange(
            bytes, MessageRaw.tagLocation, MessageRaw.tagLocation + P9Protocol.MSG_TAG_SIZE);
    raw.data = Arrays.copyOfRange(bytes, MessageRaw.minSize, bytes.length);
    return raw;
  }

  public static MessageRaw decodeRawHeader(byte[] bytes) {
    MessageRaw raw = new MessageRaw();
    raw.size = Arrays.copyOfRange(bytes, 0, P9Protocol.MSG_SIZE_HEADER);
    raw.type = bytes[P9Protocol.MSG_INT_SIZE];
    raw.tag =
        Arrays.copyOfRange(
            bytes, MessageRaw.tagLocation, MessageRaw.tagLocation + P9Protocol.MSG_TAG_SIZE);
    return raw;
  }

  public static Message decodeToMessage(MessageRaw rawMessage) {
    Message msg = new Message();
    msg.messageSize = ByteEncoder.decodeInt(rawMessage.size, 0);
    msg.tag = ByteEncoder.decodeShort(rawMessage.tag, 0);
    msg.messageType = rawMessage.type;
    msg.messageContent = rawMessage.data;
    return msg;
  }

  public static VersionRequest decodeVersionRequest(Message decodeMessage) {
    if (decodeMessage.messageType != P9Protocol.RVERSION
        && decodeMessage.messageType != P9Protocol.TVERSION) {
      throw new RuntimeException("NOT SUPPORTED");
    }

    VersionRequest request = new VersionRequest();
    request.setMessageTag(decodeMessage.tag);
    request.setMaxMsgSize(ByteEncoder.decodeInt(decodeMessage.messageContent, 0));
    request.setVersion(ByteEncoder.decodeString(decodeMessage.messageContent, 4));
    return request;
  }

  public static AuthRequest decodeAuthRequest(Message decodeMessage) {
    if (decodeMessage.messageType != P9Protocol.TAUTH) {
      throw new RuntimeException("NOT SUPPORTED");
    }
    AuthRequest request = new AuthRequest();
    request.setTag(decodeMessage.tag);
    request.setAuthFileID(ByteEncoder.decodeInt(decodeMessage.messageContent, 0));
    request.setUserName(
        ByteEncoder.decodeString(decodeMessage.messageContent, P9Protocol.MSG_FID_SIZE));
    int nextPosition = ByteEncoder.stringLength(request.getUserName()) + P9Protocol.MSG_FID_SIZE;
    request.setUserAuth(ByteEncoder.decodeString(decodeMessage.messageContent, nextPosition));
    return request;
  }

  public static AuthResponse decodeAuthResponse(Message decodeMessage) {
    if (decodeMessage.messageType != P9Protocol.RAUTH) {
      throw new RuntimeException("NOT SUPPORTED");
    }
    AuthResponse response = new AuthResponse();
    response.setTag(decodeMessage.tag);
    response.setQid(ByteEncoder.decodeQID(decodeMessage.messageContent, 0));
    return response;
  }

  public static ErrorMessage decodeError(Message msg) {
    if (msg.messageType != P9Protocol.RERROR) {
      throw new RuntimeException("Not supported");
    }
    ErrorMessage retVal = new ErrorMessage();
    retVal.setTag(msg.tag);
    retVal.setErrorMessage(ByteEncoder.decodeString(msg.messageContent, 0));
    return retVal;
  }

  public static FlushRequest decodeFlushRequest(Message msg) {
    if (msg.messageType != P9Protocol.TFLUSH) {
      throw new RuntimeException("Not supported");
    }
    FlushRequest retVal = new FlushRequest();
    retVal.setTag(msg.tag);
    retVal.setOldtag(ByteEncoder.decodeInt(msg.messageContent, 0));
    return retVal;
  }

  public static FlushResponse decodeFlushResponse(Message msg) {
    if (msg.messageType != P9Protocol.RFLUSH) {
      throw new RuntimeException("Not supported");
    }
    FlushResponse retVal = new FlushResponse();
    retVal.setTag(msg.tag);
    return retVal;
  }

  public static AttachRequest decodeAttachRequest(Message msg) {
    if (msg.messageType != P9Protocol.TATTACH) {
      throw new RuntimeException("Not supported");
    }
    AttachRequest ret = new AttachRequest();
    int ptr = 0;
    ret.setFid(ByteEncoder.decodeInt(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_INT_SIZE;
    ret.setAfid(ByteEncoder.decodeInt(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_FID_SIZE;
    ret.setUsername(ByteEncoder.decodeString(msg.messageContent, ptr));
    ptr += ByteEncoder.stringLength(ret.getUsername());
    ret.setNamespace(ByteEncoder.decodeString(msg.messageContent, ptr));
    ret.setTag(msg.tag);
    return ret;
  }

  public static AttachResponse decodeAttachResponse(Message msg) {
    if (msg.messageType != P9Protocol.RATTACH) {
      throw new RuntimeException("Not supported");
    }
    AttachResponse response = new AttachResponse();
    response.setTag(msg.tag);
    response.setServerID(ByteEncoder.decodeQID(msg.messageContent, 0));
    return response;
  }

  public static WalkRequest decodeWalkRequest(Message msg) {
    if (msg.messageType != P9Protocol.TWALK) {
      throw new RuntimeException("Not supported");
    }
    int ptr = 0;
    WalkRequest retVal = new WalkRequest();
    retVal.setTag(msg.tag);
    retVal.setBaseDescriptor(ByteEncoder.decodeInt(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_FID_SIZE;
    retVal.setNewDecriptor(ByteEncoder.decodeInt(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_FID_SIZE;
    retVal.setPathSize(ByteEncoder.decodeShort(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_SHORT_SIZE;
    retVal.setTargetFile(ByteEncoder.decodeString(msg.messageContent, ptr));
    return retVal;
  }

  public static WalkResponse decodeWalkResponse(Message msg) {
    if (msg.messageType != P9Protocol.RWALK) {
      throw new RuntimeException("Not supported");
    }
    WalkResponse response = new WalkResponse();
    response.setTag(msg.tag);
    response.setQID(ByteEncoder.decodeQID(msg.messageContent, P9Protocol.MSG_SHORT_SIZE));
    return response;
  }

  public static OpenRequest decodeOpenRequest(Message msg) {
    if (msg.messageType != P9Protocol.TOPEN) {
      throw new RuntimeException("Not supported");
    }
    OpenRequest retVal = new OpenRequest();
    retVal.setTag(msg.tag);
    retVal.setFileDescriptor(ByteEncoder.decodeInt(msg.messageContent, 0));
    retVal.setMode(msg.messageContent[P9Protocol.MSG_FID_SIZE]);

    return retVal;
  }

  public static OpenResponse decodeOpenResponse(Message msg) {
    if (msg.messageType != P9Protocol.ROPEN) {
      throw new RuntimeException("Not supported");
    }
    OpenResponse openResponse = new OpenResponse();
    openResponse.setFileQID(ByteEncoder.decodeQID(msg.messageContent, 0));
    openResponse.setSizeIO(ByteEncoder.decodeInt(msg.messageContent, P9Protocol.MSG_QID_SIZE));
    openResponse.setTag(msg.tag);
    return openResponse;
  }

  public static CreateRequest decodeCreateRequest(Message msg) {
    if (msg.messageType != P9Protocol.TCREATE) {
      throw new RuntimeException("Not supported");
    }
    CreateRequest createRequest = new CreateRequest();
    createRequest.setTag(msg.tag);
    int ptr = 0;
    createRequest.setFileDescriptor(ByteEncoder.decodeInt(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_INT_SIZE;
    createRequest.setFileName(ByteEncoder.decodeString(msg.messageContent, ptr));
    ptr += ByteEncoder.stringLength(createRequest.getFileName());
    createRequest.setPermission(ByteEncoder.decodeInt(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_INT_SIZE;
    createRequest.setMode(msg.messageContent[ptr]);
    return createRequest;
  }

  public static CreateResponse decodeCreateResponse(Message msg) {
    if (msg.messageType != P9Protocol.RCREATE) {
      throw new RuntimeException("Not supported");
    }
    CreateResponse response = new CreateResponse();
    response.setTag(msg.tag);
    response.setServerResource(ByteEncoder.decodeQID(msg.messageContent, 0));
    response.setIoSize(ByteEncoder.decodeInt(msg.messageContent, P9Protocol.MSG_QID_SIZE));
    return response;
  }

  public static ReadRequest decodeReadRequest(Message msg) {
    if (msg.messageType != P9Protocol.TREAD) {
      throw new RuntimeException("Not supported");
    }
    ReadRequest readRequest = new ReadRequest();
    int ptr = 0;
    readRequest.setFileDescriptor(ByteEncoder.decodeInt(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_INT_SIZE;
    readRequest.setFileOffset(ByteEncoder.decodeLong(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_LONG_SIZE;
    readRequest.setBytesToRead(ByteEncoder.decodeInt(msg.messageContent, ptr));
    readRequest.setTag(msg.tag);
    return readRequest;
  }

  public static ReadResponse decodeReadResponse(Message msg) {
    if (msg.messageType != P9Protocol.RREAD) {
      throw new RuntimeException("Not supported");
    }

    ReadResponse readResponse = new ReadResponse();
    readResponse.setTag(msg.tag);
    int dataSize = ByteEncoder.decodeInt(msg.messageContent, 0);
    if (dataSize != 0) {
      readResponse.setData(
          Arrays.copyOfRange(
              msg.messageContent, P9Protocol.MSG_INT_SIZE, msg.messageContent.length));
    }
    return readResponse;
  }

  public static WriteRequest decodeWriteRequest(Message msg) {
    if (msg.messageType != P9Protocol.TWRITE) {
      throw new RuntimeException("Not supported");
    }
    WriteRequest request = new WriteRequest();
    request.setTag(msg.tag);
    int ptr = 0;
    request.setFileDescriptor(ByteEncoder.decodeInt(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_INT_SIZE;
    request.setFileOffset(ByteEncoder.decodeLong(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_LONG_SIZE;
    request.setByteCount(ByteEncoder.decodeInt(msg.messageContent, ptr));
    ptr += P9Protocol.MSG_INT_SIZE;
    request.setWriteData(Arrays.copyOfRange(msg.messageContent, ptr, request.getByteCount()));

    return request;
  }

  public static WriteResponse decodeWriteResponse(Message msg) {
    if (msg.messageType != P9Protocol.RWRITE) {
      throw new RuntimeException("Not supported");
    }
    WriteResponse response = new WriteResponse();
    response.setTag(msg.tag);
    response.setBytesWritten(ByteEncoder.decodeInt(msg.messageContent, 0));
    return response;
  }

  public static CloseRequest decodeCloseRequest(Message msg) {
    if (msg.messageType != P9Protocol.TCLOSE) {
      throw new RuntimeException("Not supported");
    }
    CloseRequest closeRequest = new CloseRequest();
    closeRequest.setTag(msg.tag);
    closeRequest.setFileID(ByteEncoder.decodeInt(msg.messageContent, 0));
    return closeRequest;
  }

  public static CloseResponse decodeCloseResponse(Message msg) {
    if (msg.messageType != P9Protocol.RCLOSE) {
      throw new RuntimeException("Not supported");
    }
    CloseResponse closeResponse = new CloseResponse();
    closeResponse.setTag(msg.tag);
    return closeResponse;
  }

  public static RemoveRequest decodeRemoveRequest(Message msg) {
    if (msg.messageType != P9Protocol.TREMOVE) {
      throw new RuntimeException("Not supported");
    }
    RemoveRequest removeRequest = new RemoveRequest();
    removeRequest.setTag(msg.tag);
    removeRequest.setFileDescriptor(ByteEncoder.decodeInt(msg.messageContent, 0));
    return removeRequest;
  }

  public static RemoveResponse decodeRemoveResponse(Message msg) {
    if (msg.messageType != P9Protocol.RREMOVE) {
      throw new RuntimeException("Not supported");
    }
    RemoveResponse response = new RemoveResponse();
    response.setTag(msg.tag);
    return response;
  }

  public static StatRequest decodeStatRequest(Message msg) {
    if (msg.messageType != P9Protocol.TSTAT) {
      throw new RuntimeException("Not supported");
    }
    StatRequest request = new StatRequest();
    request.setTag(msg.tag);
    request.setFileDescriptor(ByteEncoder.decodeInt(msg.messageContent, 0));
    return request;
  }

  public static StatResponse decodeStatResponse(Message msg) {
    if (msg.messageType != P9Protocol.RSTAT) {
      throw new RuntimeException("Not supported");
    }
    StatResponse response = new StatResponse();
    response.setTag(msg.tag);
    response.setStatStruct(new StatStruct().DecodeStat(msg.messageContent, 0));
    return response;
  }

  public static WriteStatRequest decodeStatWriteRequest(Message msg) {
    if (msg.messageType != P9Protocol.TWSTAT) {
      throw new RuntimeException("Not supported");
    }
    WriteStatRequest request = new WriteStatRequest();
    request.setTag(msg.tag);
    request.setFileDescriptor(ByteEncoder.decodeInt(msg.messageContent, 0));
    request.setStatStruct(new StatStruct().DecodeStat(msg.messageContent, P9Protocol.MSG_FID_SIZE));
    return request;
  }

  public static WriteStatResponse decodeStatWriteResponse(Message msg) {
    if (msg.messageType != P9Protocol.RWSTAT) {
      throw new RuntimeException("Not supported");
    }
    WriteStatResponse response = new WriteStatResponse();
    response.setTag(msg.tag);
    return response;
  }
}
