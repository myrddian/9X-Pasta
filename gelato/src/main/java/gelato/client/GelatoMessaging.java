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

package gelato.client;

import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import gelato.GelatoConnection;
import gelato.GelatoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.messages.Message;
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
import protocol.messages.response.FlushResponse;
import protocol.messages.response.OpenResponse;
import protocol.messages.response.ReadResponse;
import protocol.messages.response.RemoveResponse;
import protocol.messages.response.StatResponse;
import protocol.messages.response.WalkResponse;
import protocol.messages.response.WriteResponse;
import protocol.messages.response.WriteStatResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GelatoMessaging {

    private GelatoSession sessionHandler;
    private boolean shutdown = false;
    private GelatoConnection connection;
    private Map<Integer, GelatoMessage> messageMap = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(GelatoMessaging.class);

    private void processMessages() {
        Message fetchedMessage = connection.getMessage();
        int correlation = fetchedMessage.tag;
        if(messageMap.containsKey(correlation))  {
            GelatoMessage future = messageMap.get(correlation);
            if(fetchedMessage.messageType == P9Protocol.RERROR) {
                String error = Decoder.decodeError(fetchedMessage).getErrorMessage();
                future.setError();
                future.setErrorMessage(error);
                future.setCompleted();
                logger.error("Error in response " + error );

            } else {
                switch (future.messageType()) {
                    case P9Protocol.TATTACH:
                        future.setResponseMessage(Decoder.decodeAttachResponse(fetchedMessage));
                        break;
                    case P9Protocol.TAUTH:
                        future.setResponseMessage(Decoder.decodeAuthResponse(fetchedMessage));
                        break;
                    case P9Protocol.TCLOSE:
                        future.setResponseMessage(Decoder.decodeCloseResponse(fetchedMessage));
                        break;
                    case P9Protocol.TCREATE:
                        future.setResponseMessage(Decoder.decodeCreateResponse(fetchedMessage));
                        break;
                    case P9Protocol.TFLUSH:
                        future.setResponseMessage(Decoder.decodeFlushResponse(fetchedMessage));
                        break;
                    case P9Protocol.TOPEN:
                        future.setResponseMessage(Decoder.decodeOpenResponse(fetchedMessage));
                        break;
                    case P9Protocol.TREAD:
                        future.setResponseMessage(Decoder.decodeReadResponse(fetchedMessage));
                        break;
                    case P9Protocol.TREMOVE:
                        future.setResponseMessage(Decoder.decodeRemoveResponse(fetchedMessage));
                        break;
                    case P9Protocol.TSTAT:
                        future.setResponseMessage(Decoder.decodeStatResponse(fetchedMessage));
                        break;
                    case P9Protocol.TWALK:
                        future.setResponseMessage(Decoder.decodeWalkResponse(fetchedMessage));
                        break;
                    case P9Protocol.TWRITE:
                        future.setResponseMessage(Decoder.decodeWriteResponse(fetchedMessage));
                        break;
                    case P9Protocol.TWSTAT:
                        future.setResponseMessage(Decoder.decodeStatWriteResponse(fetchedMessage));
                        break;
                }
                future.setCompleted();
            }
        }
    }

    public GelatoMessaging(GelatoSession session,
                           GelatoConnection connection) {
        sessionHandler = session;
        this.connection = connection;
    }

    public void close(GelatoMessage message) {
        sessionHandler.getTags().closeTag(message.getTag());
        if(messageMap.containsKey(message.getTag())) {
            messageMap.remove(message.getTag());
        }
    }

    public void submitMessage(GelatoMessage message) {
        message.setTag(sessionHandler.getTags().generateTag());
        if(messageMap.containsKey(message.getTag())) {
            logger.error("Clashing tags - removing old");
            messageMap.remove(message.getTag());
        }
        messageMap.put(message.getTag(), message);
        connection.sendMessage(message.toMessage());
    }

    public GelatoMessage<AttachRequest, AttachResponse> createAttachTransaction() {
            return new GelatoMessage<>( new AttachRequest());
    }

    public GelatoMessage<AuthRequest, AuthResponse> createAuthTransaction() {
        return new GelatoMessage<>(new AuthRequest());
    }

    public GelatoMessage<CloseRequest, CloseResponse> createCloseTransaction() {
        return new GelatoMessage<>(new CloseRequest());
    }


    public GelatoMessage<FlushRequest, FlushResponse> createFlushTransaction() {
        return new GelatoMessage<>(new FlushRequest());
    }

    public GelatoMessage<OpenRequest, OpenResponse> createOpenTransaction() {
        return new GelatoMessage<>(new OpenRequest());
    }

    public GelatoMessage<CreateRequest, CreateResponse> createCreateTransaction() {
        return new GelatoMessage<>(new CreateRequest());
    }

    public GelatoMessage<ReadRequest, ReadResponse> createReadTransaction() {
        return new GelatoMessage<>(new ReadRequest());
    }

    public GelatoMessage<StatRequest, StatResponse> createStatTransaction() {
        return new GelatoMessage<>(new StatRequest());
    }

    public GelatoMessage<RemoveRequest, RemoveResponse> createRemoveTransaction() {
        return new GelatoMessage<>(new RemoveRequest());
    }

    public GelatoMessage<WalkRequest, WalkResponse> createWalkTransaction() {
        return new GelatoMessage<>(new WalkRequest());
    }

    public GelatoMessage<WriteRequest, WriteResponse> createWriteTransaction() {
        return new GelatoMessage<>(new WriteRequest());
    }

    public GelatoMessage<WriteStatRequest, WriteStatResponse> createWriteStatRequest() {
        return new GelatoMessage<>(new WriteStatRequest());
    }

    @CiotolaServiceStop
    public synchronized void shutdown() {
        shutdown = true;
    }

    @CiotolaServiceStart
    public synchronized void start() {
        shutdown = false;
    }

    @CiotolaServiceRun
    public void process() {
        while (!isShutdown()) {
            processMessages();
        }
    }

    public synchronized boolean isShutdown() {
        return shutdown;
    }

}
