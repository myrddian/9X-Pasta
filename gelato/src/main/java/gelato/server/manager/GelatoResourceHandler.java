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

package gelato.server.manager;

import gelato.*;
import gelato.server.manager.requests.*;
import org.slf4j.*;
import protocol.*;
import protocol.messages.*;
import protocol.messages.request.*;
import protocol.messages.response.*;

public abstract class GelatoResourceHandler implements GenericRequestHandler,
        RequestCreateHandler,
        RequestFlushHandler,
        RequestWalkHandler,
        RequestCloseHandler,
        RequestOpenHandler,
        RequestReadHandler,
        RequestRemoveHandler,
        RequestStatWriteHandler,
        RequestStatHandler,
        RequestWriteHandler {

    private final Logger logger = LoggerFactory.getLogger(GelatoResourceHandler.class);

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message request) {
        if(request.messageType == P9Protocol.TOPEN) {
            return processRequest(connection,descriptor,session,Decoder.decodeOpenRequest(request));
        } else if (request.messageType == P9Protocol.TWALK) {
            return processRequest(connection,descriptor,session, Decoder.decodeWalkRequest(request));
        } else if (request.messageType == P9Protocol.TFLUSH) {
            return processRequest(connection, descriptor, session, Decoder.decodeFlushRequest(request));
        } else if (request.messageType == P9Protocol.TREMOVE) {
            return processRequest(connection, descriptor, session, Decoder.decodeRemoveRequest(request));
        } else if (request.messageType == P9Protocol.TWSTAT) {
            return processRequest(connection, descriptor, session, Decoder.decodeStatWriteRequest(request));
        } else if (request.messageType == P9Protocol.TWRITE) {
            return processRequest(connection, descriptor, session, Decoder.decodeWriteRequest(request));
        } else if (request.messageType == P9Protocol.TCLOSE) {
            return processRequest(connection, descriptor, session, Decoder.decodeCloseRequest(request));
        } else if (request.messageType == P9Protocol.TREAD) {
            return processRequest(connection, descriptor, session, Decoder.decodeReadRequest(request));
        } else if(request.messageType == P9Protocol.TSTAT) {
            return processRequest(connection, descriptor, session, Decoder.decodeStatRequest(request));
        } else if (request.messageType == P9Protocol.TCREATE) {
            return processRequest(connection, descriptor, session, Decoder.decodeCreateRequest(request));
        }
        logger.error("Unable to Process Request");
        return false;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, OpenRequest request) {
        RequestConnection con = createConnection(connection, descriptor,session, request.getTag());
        GelatoFileDescriptor clientDescriptor = new GelatoFileDescriptor();
        clientDescriptor.setQid(getQID());
        clientDescriptor.setRawFileDescriptor(request.getFileDescriptor());
        openRequest(con, clientDescriptor, request.getMode());
        return true;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, WalkRequest request) {
        RequestConnection con = createConnection(connection, descriptor,session, request.getTag());
        GelatoFileDescriptor clientDescriptor = new GelatoFileDescriptor();
        clientDescriptor.setQid(getQID());
        clientDescriptor.setRawFileDescriptor(request.getNewDecriptor());
        walkRequest(con, request.getTargetFile(), clientDescriptor);
        return true;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, CloseRequest request) {
        RequestConnection con = createConnection(connection, descriptor,session, request.getTag());
        GelatoFileDescriptor clientDescriptor = new GelatoFileDescriptor();
        clientDescriptor.setQid(getQID());
        clientDescriptor.setRawFileDescriptor(request.getFileID());
        closeRequest(con,clientDescriptor);
        return true;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, CreateRequest request) {
        RequestConnection con = createConnection(connection, descriptor,session, request.getTag());
        createRequest(con,request.getFileName(), request.getPermission(), request.getMode());
        return true;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, ReadRequest request) {
        RequestConnection con = createConnection(connection, descriptor,session, request.getTag());
        GelatoFileDescriptor clientDescriptor = new GelatoFileDescriptor();
        clientDescriptor.setQid(getQID());
        clientDescriptor.setRawFileDescriptor(request.getFileDescriptor());
        readRequest(con,clientDescriptor,request.getFileOffset(), request.getBytesToRead());
        return true;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, RemoveRequest request) {
        RequestConnection con = createConnection(connection, descriptor,session, request.getTag());
        GelatoFileDescriptor clientDescriptor = new GelatoFileDescriptor();
        clientDescriptor.setQid(getQID());
        clientDescriptor.setRawFileDescriptor(request.getFileDescriptor());
        removeRequest(con,clientDescriptor);
        return true;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, StatRequest request) {
        RequestConnection con = createConnection(connection, descriptor,session, request.getTag());
        GelatoFileDescriptor clientDescriptor = new GelatoFileDescriptor();
        clientDescriptor.setQid(getQID());
        clientDescriptor.setRawFileDescriptor(request.getFileDescriptor());
        statRequest(con,clientDescriptor);
        return true;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, WriteStatRequest request) {
        RequestConnection con = createConnection(connection, descriptor,session, request.getTag());
        GelatoFileDescriptor clientDescriptor = new GelatoFileDescriptor();
        clientDescriptor.setQid(getQID());
        clientDescriptor.setRawFileDescriptor(request.getFileDescriptor());
        writeStatRequest(con,clientDescriptor, request.getStatStruct());
        return true;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, WriteRequest request) {
        RequestConnection con = createConnection(connection, descriptor,session, request.getTag());
        GelatoFileDescriptor clientDescriptor = new GelatoFileDescriptor();
        clientDescriptor.setQid(getQID());
        clientDescriptor.setRawFileDescriptor(request.getFileDescriptor());
        writeRequest(con,clientDescriptor, request.getFileOffset(), request.getWriteData());
        return true;
    }

    private RequestConnection createConnection(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, int tag) {
        RequestConnection con = new RequestConnection();
        con.setConnection(connection);
        con.setDescriptor(descriptor);
        con.setSession(session);
        con.setTransactionId(tag);
        return con;
    }

    public void sendErrorMessage(RequestConnection connection, String message) {
        ErrorMessage msg = new ErrorMessage();
        msg.setTag(connection.getTransactionId());
        msg.setErrorMessage(message);
        connection.reply(msg);
    }

    public abstract void  openRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, byte mode);
    public abstract void  readRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, long offset, int numberOfBytes);
    public abstract void  walkRequest(RequestConnection connection, String fileName, GelatoFileDescriptor newDescriptor);
    public abstract void  createRequest(RequestConnection connection, String fileName, int permission, byte mode);
    public abstract void  removeRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor);
    public abstract void  closeRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor);
    public abstract void  statRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor);
    public abstract void  writeStatRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, StatStruct newStruct);
    public abstract void  writeRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, long offset, byte []data);


    public void setQID(QID value) {
        fileDescriptor.setQid(value);
    }
    public QID getQID() {
        return fileDescriptor.getQid();
    }
    public String resourceName() {
        return getStat().getName();
    }
    public StatStruct getStat() {
        return resourceStat;
    }
    public void setStat(StatStruct newStat) {
        resourceStat = newStat;
    }
    public GelatoFileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }
    public void setFileDescriptor(GelatoFileDescriptor descriptor) {
        fileDescriptor = descriptor;
    }
    public void setResourceName(String newName) { getStat().setName(newName); }
    public void setQidManager(GelatoQIDManager newManager) { resourceManager = newManager;}
    public GelatoQIDManager getResourceManager() { return resourceManager; }

    private GelatoFileDescriptor fileDescriptor = new GelatoFileDescriptor();
    private String directoryName = "";
    private StatStruct resourceStat = new StatStruct();
    private GelatoQIDManager resourceManager;


}
