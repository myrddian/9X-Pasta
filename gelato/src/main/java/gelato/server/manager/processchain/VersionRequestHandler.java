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

package gelato.server.manager.processchain;

import gelato.*;
import gelato.server.manager.*;
import gelato.server.manager.requests.*;
import gelato.server.manager.response.*;
import org.slf4j.*;
import protocol.*;
import protocol.messages.*;

public class VersionRequestHandler extends GelatoAbstractGenericRequestHandler implements RequestVersionHandler,
        VersionResponseHandler {

    private final Logger logger = LoggerFactory.getLogger(VersionRequestHandler.class);
    private GelatoAbstractGenericRequestHandler next;
    private RequestVersionHandler versionRequestHandler = this;
    private VersionResponseHandler versionResponseHandler = this;
    private GelatoSession clientSession = null;

    public Gelato getLibrary() {
        return library;
    }

    public void setLibrary(Gelato library) {
        this.library = library;
    }

    private Gelato library;

    public GelatoAbstractGenericRequestHandler getNext() {
        return next;
    }

    public void setNext(GelatoAbstractGenericRequestHandler next) {
        this.next = next;
    }

    public RequestVersionHandler getVersionRequestHandler() {
        return versionRequestHandler;
    }

    public void setVersionRequestHandler(RequestVersionHandler versionRequestHandler) {
        this.versionRequestHandler = versionRequestHandler;
    }

    public VersionResponseHandler getVersionResponseHandler() {
        return versionResponseHandler;
    }

    public void setVersionResponseHandler(VersionResponseHandler versionResponseHandler) {
        this.versionResponseHandler = versionResponseHandler;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message request) {
        if(request.messageType != P9Protocol.TVERSION && clientSession==null) {
            logger.error("Descriptor has not started a valid session " + Long.toString(descriptor.getDescriptorId()) + " " + Byte.toString(request.messageType));
            sendErrorMessage(connection,descriptor,request.tag,"");
            return false;
        } else if(request.messageType == P9Protocol.TVERSION) {
            return versionRequestHandler.processRequest(connection, descriptor, Decoder.decodeVersionRequest(request));
        }
        return next.processRequest(connection,descriptor,clientSession,request);
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, VersionRequest request) {
        library.getTagManager().createTagHandler(descriptor);
        GelatoServerSession newSession = new GelatoServerSession();
        newSession.setManager(new GelatoDescriptorManager());
        newSession.setConnection(connection);
        newSession.setTags(library.getTagManager().getManager(descriptor));
        VersionRequest response = new VersionRequest();
        response.setTag(request.getMessageTag());
        newSession.getTags().registerTag(request.getMessageTag());
        clientSession =  newSession;
        logger.info("Started Session for Descriptor " + Long.toString(descriptor.getDescriptorId()));
        return versionResponseHandler.writeResponse(connection,descriptor,response);
    }

    @Override
    public boolean writeResponse(GelatoConnection connection, GelatoFileDescriptor fileDescriptor, VersionRequest response) {
        Message rspVersion = response.toMessage();
        rspVersion.messageType = P9Protocol.RVERSION;
        connection.sendMessage(fileDescriptor, rspVersion);
        return true;
    }
}
