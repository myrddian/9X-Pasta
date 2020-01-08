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
            return false;
        }

        logger.error("Unable to Process Request");
        return false;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, OpenRequest request) {
        return false;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, WalkRequest request) {
        return false;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, CloseRequest request) {
        return false;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, CreateRequest request) {
        return false;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, FlushRequest request) {
        return false;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, ReadRequest request) {
        return false;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, RemoveRequest request) {
        return false;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, StatRequest request) {
        return false;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, WriteStatRequest request) {
        return false;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, WriteRequest request) {
        return false;
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
        return directoryName;
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

    private GelatoFileDescriptor fileDescriptor;
    private String directoryName = "";
    private StatStruct resourceStat;


}
