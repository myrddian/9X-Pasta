
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
import protocol.*;
import protocol.messages.*;
import protocol.messages.request.*;

public class GelatoGenericRequestHandler implements GenericRequestHandler
{

    private RequestAttachHandler attachHandler;
    private RequestWriteHandler writeHandler;
    private RequestStatHandler statHandler;
    private RequestStatWriteHandler statWriteHandler;
    private RequestRemoveHandler requestRemoveHandler;
    private RequestReadHandler requestReadHandler;
    private RequestOpenHandler requestOpenHandler;
    private RequestCreateHandler requestCreateHandler;
    private RequestFlushHandler requestFlushHandler;
    private RequestWalkHandler requestWalkHandler;
    private RequestClunkHandler requestClunkHandler;
    private GenericRequestHandler unknownHandler;

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message request) {
        if (request.messageType == P9Protocol.TWALK) {
            return requestWalkHandler.processRequest(connection,
                    descriptor,
                    session,
                    Decoder.decodeWalkRequest(request));
        } else if (request.messageType == P9Protocol.TATTACH) {
            return attachHandler.processRequest(connection,
                    descriptor,
                    session,
                    Decoder.decodeAttachRequest(request));
        } else if (request.messageType == P9Protocol.TOPEN) {
            return requestOpenHandler.processRequest(connection,
                    descriptor,
                    session,
                    Decoder.decodeOpenRequest(request));
        } else if (request.messageType == P9Protocol.TREAD) {

        } else if (request.messageType == P9Protocol.TWRITE) {

        } else if (request.messageType == P9Protocol.TCREATE) {

        } else if (request.messageType == P9Protocol.TREMOVE) {

        } else if (request.messageType == P9Protocol.TSTAT) {

        } else if (request.messageType == P9Protocol.TWSTAT) {

        } else if (request.messageType == P9Protocol.TFLUSH) {

        } else if (request.messageType == P9Protocol.TCLUNK) {

        } else {
            if(unknownHandler != null) {
                return  unknownHandler.processRequest(connection,
                        descriptor,session, request);
            }
        }


        return false;
    }

    public RequestAttachHandler getAttachHandler() {
        return attachHandler;
    }

    public void setAttachHandler(RequestAttachHandler attachHandler) {
        this.attachHandler = attachHandler;
    }

    public RequestWriteHandler getWriteHandler() {
        return writeHandler;
    }

    public void setWriteHandler(RequestWriteHandler writeHandler) {
        this.writeHandler = writeHandler;
    }

    public RequestStatHandler getStatHandler() {
        return statHandler;
    }

    public void setStatHandler(RequestStatHandler statHandler) {
        this.statHandler = statHandler;
    }

    public RequestStatWriteHandler getStatWriteHandler() {
        return statWriteHandler;
    }

    public void setStatWriteHandler(RequestStatWriteHandler statWriteHandler) {
        this.statWriteHandler = statWriteHandler;
    }

    public RequestRemoveHandler getRequestRemoveHandler() {
        return requestRemoveHandler;
    }

    public void setRequestRemoveHandler(RequestRemoveHandler requestRemoveHandler) {
        this.requestRemoveHandler = requestRemoveHandler;
    }

    public RequestReadHandler getRequestReadHandler() {
        return requestReadHandler;
    }

    public void setRequestReadHandler(RequestReadHandler requestReadHandler) {
        this.requestReadHandler = requestReadHandler;
    }

    public RequestOpenHandler getRequestOpenHandler() {
        return requestOpenHandler;
    }

    public void setRequestOpenHandler(RequestOpenHandler requestOpenHandler) {
        this.requestOpenHandler = requestOpenHandler;
    }

    public RequestCreateHandler getRequestCreateHandler() {
        return requestCreateHandler;
    }

    public void setRequestCreateHandler(RequestCreateHandler requestCreateHandler) {
        this.requestCreateHandler = requestCreateHandler;
    }

    public RequestFlushHandler getRequestFlushHandler() {
        return requestFlushHandler;
    }

    public void setRequestFlushHandler(RequestFlushHandler requestFlushHandler) {
        this.requestFlushHandler = requestFlushHandler;
    }

    public RequestWalkHandler getRequestWalkHandler() {
        return requestWalkHandler;
    }

    public void setRequestWalkHandler(RequestWalkHandler requestWalkHandler) {
        this.requestWalkHandler = requestWalkHandler;
    }

    public RequestClunkHandler getRequestClunkHandler() {
        return requestClunkHandler;
    }

    public void setRequestClunkHandler(RequestClunkHandler requestClunkHandler) {
        this.requestClunkHandler = requestClunkHandler;
    }

    public GenericRequestHandler getUnknownHandler() {
        return unknownHandler;
    }

    public void setUnknownHandler(GenericRequestHandler unknownHandler) {
        this.unknownHandler = unknownHandler;
    }
}
