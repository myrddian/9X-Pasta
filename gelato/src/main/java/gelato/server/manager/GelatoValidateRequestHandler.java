
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
import gelato.server.manager.implementation.*;
import gelato.server.manager.requests.*;
import org.slf4j.*;
import protocol.*;
import protocol.messages.*;
import protocol.messages.request.*;

public class GelatoValidateRequestHandler implements GenericRequestHandler
{

    private final Logger logger = LoggerFactory.getLogger(GelatoValidateRequestHandler.class);

    private GenericRequestHandler unknownHandler;
    private GenericRequestHandler nextHandler;

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message request) {

        //Determine is message is known
        //if not pass it to the unknown handler

        //If version reset the entire session

        //Decode the message type
        //Get requiredFIDs

        //Only Auth and Attach require two FIDS

        GelatoFileDescriptor requestedResource = new GelatoFileDescriptor();
        if(request.messageType == P9Protocol.TOPEN) {
            requestedResource.setRawFileDescriptor(Decoder.decodeOpenRequest(request).getFileDescriptor());
        } else if (request.messageType == P9Protocol.TWALK) {
            requestedResource.setRawFileDescriptor(Decoder.decodeWalkRequest(request).getBaseDescriptor());
        } else if (request.messageType == P9Protocol.TFLUSH) {
            return IgnoreFlushRequests.sendFlushResponse(connection, descriptor, session, Decoder.decodeFlushRequest(request));
        } else if (request.messageType == P9Protocol.TREMOVE) {
            requestedResource.setRawFileDescriptor(Decoder.decodeRemoveRequest(request).getFileDescriptor());
        } else if (request.messageType == P9Protocol.TWSTAT) {
            requestedResource.setRawFileDescriptor(Decoder.decodeStatWriteRequest(request).getFileDescriptor());
        } else if (request.messageType == P9Protocol.TWRITE) {
            requestedResource.setRawFileDescriptor(Decoder.decodeWriteRequest(request).getFileDescriptor());
        } else if (request.messageType == P9Protocol.TCLOSE) {
            requestedResource.setRawFileDescriptor(Decoder.decodeCloseRequest(request).getFileID());
        } else if (request.messageType == P9Protocol.TREAD) {
            requestedResource.setRawFileDescriptor(Decoder.decodeReadRequest(request).getFileDescriptor());
        } else if(request.messageType == P9Protocol.TSTAT) {
            requestedResource.setRawFileDescriptor(Decoder.decodeStatRequest(request).getFileDescriptor());
        } else if (request.messageType == P9Protocol.TCREATE) {
            requestedResource.setRawFileDescriptor(Decoder.decodeCreateRequest(request).getFileDescriptor());
        } else  {
            logger.trace("Messge Type unknown - Passing to Registered Unknown Handler");
            if(unknownHandler != null) {
                return unknownHandler.processRequest(connection, descriptor, session, request);
            }
            else {
                logger.error("Unable to process message");
                return false;
            }
        }
        if(!session.getManager().validDescriptor(requestedResource)) {
            logger.error("Invalid Descriptor request in Message");
            return false;
        }

        return nextHandler.processRequest(connection,descriptor,session,request);
    }



    public GenericRequestHandler getUnknownHandler() {
        return unknownHandler;
    }

    public void setUnknownHandler(GenericRequestHandler unknownHandler) {
        this.unknownHandler = unknownHandler;
    }

    public GenericRequestHandler getNextHandler() {
        return nextHandler;
    }

    public void setNextHandler(GenericRequestHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
