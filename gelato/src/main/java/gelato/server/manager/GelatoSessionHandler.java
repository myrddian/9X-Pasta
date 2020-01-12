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
import protocol.messages.response.*;

public class GelatoSessionHandler {

    private final Logger logger = LoggerFactory.getLogger(GelatoSessionHandler.class);
    private GelatoQIDManager qidManager;
    private GenericRequestHandler handler;
    private GelatoAuthorisationManager authorisationManager;
    private GelatoResourceHandler rootAttach;

    public GelatoSessionHandler(GelatoQIDManager gelatoQIDManager,
                                GenericRequestHandler handler) {
        qidManager = gelatoQIDManager;
        this.handler = handler;
        authorisationManager = new NullAuthorisation();

    }


    public GelatoSessionHandler(GelatoQIDManager gelatoQIDManager,
                                GenericRequestHandler handler,
                                GelatoResourceHandler root) {
        qidManager = gelatoQIDManager;
        this.handler = handler;
        authorisationManager = new NullAuthorisation();
        rootAttach = root;
    }

    public GelatoSessionHandler(GelatoQIDManager gelatoQIDManager,
                                GenericRequestHandler nextHandler,
                                GelatoAuthorisationManager authorisationManager,
                                GelatoResourceHandler root)
    {
        qidManager = gelatoQIDManager;
        handler = nextHandler;
        this.authorisationManager = authorisationManager;
        rootAttach = root;
    }

    public void handleSession(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message message) {
       GelatoFileDescriptor auth = session.getAuthorisationDescriptor();

       //validate tag
       if (session.getTags().isRecycled(message.tag)  != false) {
           logger.error("Invalid Tags, Expected: " + Integer.toString(session.getTags().getTagCount()) + " Got: " + Integer.toString(message.tag));
       }
       session.getTags().registerTag(message.tag);
       if(auth == null) {
           requestAuth(connection, descriptor, session, message);
       } else {
           if (handler == null) {
               logger.error("Request Handler currently not setup ");
           }
           if (handler.processRequest(connection, descriptor, session, message) == false) {
               logger.error("Unable to process request");
           }
       }
    }

    private void requestAuth(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message message) {
        if(message.messageType == P9Protocol.TAUTH || message.messageType == P9Protocol.TATTACH) {
            if(message.messageType == P9Protocol.TATTACH) {
                handleAttachRequest(connection, descriptor, session, message);
            }
            else {
                handleAuthRequest(connection, descriptor, session, message);
            }
        }
        else {
            logger.error("Invalid Session Creation - Expected AUTH/Attach Message");
        }
    }

    private void handleAttachRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message message) {
        logger.info("Handling Attach Request for Descriptor " + Long.toString(descriptor.getDescriptorId()));
        AttachRequest request  = Decoder.decodeAttachRequest(message);
        GelatoDescriptorManager sessionDescriptor  = new GelatoDescriptorManager();
        GelatoFileDescriptor authDescriptor;
        GelatoFileDescriptor resourceDescriptor = new GelatoFileDescriptor();
        if(request.getAfid() == P9Protocol.NO_FID && authorisationManager.requireAuth() == false) {
            //Start an attach procedure
            session.setUserName(request.getUsername());
            authDescriptor = new GelatoFileDescriptor();
            session.setManager(sessionDescriptor);
            authDescriptor.setRawFileDescriptor(P9Protocol.NO_FID);
            sessionDescriptor.registerDescriptor(authDescriptor);


        } else if(request.getAfid() != P9Protocol.NO_FID && authorisationManager.requireAuth() == true) {
            //Get the descriptor agreed by the auth exchange
            authDescriptor = authorisationManager.getAuthorisedDescriptor(connection, descriptor);
            if(authDescriptor.getDescriptorId() != ByteEncoder.getUnsigned(request.getAfid())) {
                logger.error("Client sent invalid AFID ID expected: " + Long.toString(authDescriptor.getDescriptorId()) +
                        "Got: " + Long.toString(ByteEncoder.getUnsigned(request.getAfid())));
                return;
            }
            session.setManager(sessionDescriptor);
            authDescriptor.setRawFileDescriptor(request.getAfid());
            sessionDescriptor.registerDescriptor(authDescriptor);
        }
        else {
            logger.error("Expected an Authorised FID - Authorisation Manager Requests client to be authenticated");
            return;
        }
        resourceDescriptor.setRawFileDescriptor(request.getFid());
        session.getManager().mapQID(resourceDescriptor, rootAttach.getFileDescriptor());
        //Send Response
        AttachResponse response = new AttachResponse();
        response.setTag(message.tag);
        response.setServerID(rootAttach.getQID());
        connection.sendMessage(descriptor, response.toMessage());
        logger.info("User: " + request.getUsername() + " Mapped ROOT against FID: " + Long.toString(ByteEncoder.getUnsigned(request.getFid()))
                + " Connection: " + Long.toString(descriptor.getDescriptorId()));
    }

    private void handleAuthRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message message) {
        AuthRequest authRequest = Decoder.decodeAuthRequest(message);
        if(authorisationManager.requireAuth()) {
            if(authorisationManager.processAuthRequest(connection, descriptor, session, authRequest)) {
                QID authQID = authorisationManager.authoriseQID(descriptor);
                session.setUserName(authRequest.getUserName());
                //setup session stuff
                GelatoDescriptorManager newDescriptorSessionManager = new GelatoDescriptorManager();
                GelatoFileDescriptor newAuth = new GelatoFileDescriptor();
                session.setManager(newDescriptorSessionManager);
                newAuth.setQid(authQID);
                newAuth.setRawFileDescriptor(authRequest.getAuthFileID());
                newDescriptorSessionManager.registerDescriptor(newAuth);

                //send response
                AuthResponse response = new AuthResponse();
                response.setTag(message.tag);
                response.setQid(authQID);
                connection.sendMessage(descriptor,response.toMessage());

                logger.info("User: " + authRequest.getUserName() + " authenticated against FID: " + Integer.toString(authRequest.getAuthFileID())
                        + " Connection: " + Long.toString(descriptor.getDescriptorId()));
            } else {
                logger.error("Unable to Authenticate user "+ authRequest.getUserName());
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorMessage("authentication failed");
                errorMessage.setTag(message.tag);
                connection.sendMessage(descriptor, errorMessage.toMessage());
            }
        } else {
            logger.error("Authorisation Manager does not support AUTH requests - Sending error");
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setTag(message.tag);
            errorMessage.setErrorMessage("No Authorisation Supported");
            connection.sendMessage(descriptor, errorMessage.toMessage());
        }

    }


    public GelatoQIDManager getQidManager() {
        return qidManager;
    }

    public void setQidManager(GelatoQIDManager qidManager) {
        this.qidManager = qidManager;
    }

    public GenericRequestHandler getHandler() {
        return handler;
    }

    public void setHandler(GenericRequestHandler handler) {
        this.handler = handler;
    }

    public GelatoAuthorisationManager getAuthorisationManager() {
        return authorisationManager;
    }

    public void setAuthorisationManager(GelatoAuthorisationManager authorisationManager) {
        this.authorisationManager = authorisationManager;
    }

    public GelatoResourceHandler getRootAttach() {
        return rootAttach;
    }

    public void setRootAttach(GelatoResourceHandler rootAttach) {
        this.rootAttach = rootAttach;
    }
}
