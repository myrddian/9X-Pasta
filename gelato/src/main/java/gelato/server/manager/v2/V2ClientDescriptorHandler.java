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

package gelato.server.manager.v2;

import ciotola.Ciotola;
import ciotola.annotations.CiotolaAutowire;
import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import gelato.GelatoConnection;
import gelato.GelatoDescriptorManager;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.GelatoTags;
import gelato.server.GelatoServerManager;
import gelato.server.manager.GelatoAbstractGenericRequestHandler;
import gelato.server.manager.GelatoServerSession;
import gelato.server.manager.GenericRequestHandler;
import gelato.server.manager.RequestConnection;
import gelato.server.manager.controllers.GelatoDirectoryController;
import gelato.server.manager.processchain.AttachRequestHandler;
import gelato.server.manager.processchain.AuthRequestHandler;
import gelato.server.manager.processchain.UnknownRequestHandler;
import gelato.server.manager.processchain.VersionRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.ByteEncoder;
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.messages.Message;
import protocol.messages.VersionRequest;
import protocol.messages.request.AttachRequest;
import protocol.messages.request.AuthRequest;
import protocol.messages.response.AttachResponse;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class V2ClientDescriptorHandler extends GelatoAbstractGenericRequestHandler implements VersionRequestHandler,
        AttachRequestHandler, AuthRequestHandler, UnknownRequestHandler {


    public static final String INVALID_FID = "Only NO_FID supported";
    public static final String NO_SESSION = "No Session started - TVERSION Expected not found";
    public static final String NOT_SUPPORTED = "Not supported";


    final Logger logger = LoggerFactory.getLogger(V2ClientDescriptorHandler.class);
    private GenericRequestHandler genericRequestHandler = this;
    private VersionRequestHandler versionRequestHandler = this;
    private AttachRequestHandler attachRequestHandler = this;
    private AuthRequestHandler authRequestHandler = this;
    private UnknownRequestHandler unknownRequestHandler = this;
    private GenericRequestHandler nextHandlerChain;
    private BlockingQueue<V2Message> readMessageQueue = new LinkedBlockingQueue<>();
    private GelatoDirectoryController rootAttach;
    private boolean shutdown = false;

    @CiotolaAutowire private GelatoServerManager manager;

    //Client connection state
    private GelatoSession clientSession = null;
    private GelatoDescriptorManager clientDescriptors = null;
    private GelatoFileDescriptor clientFileDescriptor = null;
    private GelatoTags clientTagHandler = null;

    public V2ClientDescriptorHandler(GelatoFileDescriptor connectionName) {
        clientFileDescriptor = connectionName;
    }


    private void processMessages() {
        boolean completedOk = true;
        try {
            V2Message message = readMessageQueue.poll(Ciotola.getInstance().getConnectionTimeOut() , TimeUnit.SECONDS);
            Message msg = message.getMessage();
            GelatoConnection clientConnection = message.getClientConnection();
            completedOk = genericRequestHandler.processRequest(clientConnection,clientFileDescriptor,clientSession,msg);
        } catch (InterruptedException | NullPointerException e) {
            logger.debug("Connection interrupted - Possible time out - Stopping");
            shutdown();
            return;
        }

        if(!completedOk) {
            shutdown();
            logger.error("Shutting client connection down - Process Chain Error");
        }
    }

    public void addMessage(V2Message processMessage) {
        try {
            readMessageQueue.put(processMessage);
        } catch (InterruptedException e) {
            logger.error("Interrupted", e);
        }
    }

    public boolean sessionValid() {
        if(clientSession == null) {
            return false;
        } else if( clientDescriptors == null) {
            return false;
        } else if(clientFileDescriptor == null) {
            return false;
        } else if(clientTagHandler == null) {
            return false;
        }
        return true;
    }

    public boolean validateSession(GelatoFileDescriptor descriptor, Message msg) {
        if(!sessionValid()) {
            logger.error(
                    "Descriptor has not started a valid session "
                            + Long.toString(descriptor.getDescriptorId())
                            + " "
                            + Byte.toString(msg.messageType));
            return false;
        }
        return true;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message request) {

        RequestConnection requestConnection = createConnection(connection,descriptor,session,request.tag);
        requestConnection.setOriginalMessage(request);

        if(request.messageType == P9Protocol.TVERSION) {
            VersionRequest versionRequest = Decoder.decodeVersionRequest(request);
            return versionRequestHandler.versionRequest(requestConnection,versionRequest);
        }

        if (clientTagHandler.isRecycled(request.tag) != false) {
            logger.error(
                    "Invalid Tags, Expected: "
                            + Integer.toString(clientTagHandler.getTagCount())
                            + " Got: "
                            + Integer.toString(request.tag));
            sendErrorMessage(requestConnection,"Invalid TAGS");
            return false;
        }

        if (request.messageType == P9Protocol.TATTACH) {
            AttachRequest attachRequest = Decoder.decodeAttachRequest(request);
            return attachRequestHandler.attachRequest(requestConnection, attachRequest);
        } else if (request.messageType == P9Protocol.TAUTH) {
            AuthRequest authRequest = Decoder.decodeAuthRequest(request);
            return authRequestHandler.processAuthRequest(requestConnection,authRequest);
        } else {
            return nextHandlerChain.processRequest(connection,descriptor,session,request);
        }

    }

    @Override
    public boolean versionRequest(RequestConnection connection, VersionRequest versionRequest) {

        clientTagHandler = new GelatoTags();
        clientSession = new GelatoServerSession();
        clientDescriptors = new GelatoDescriptorManager();
        clientSession.setManager(clientDescriptors);
        clientSession.setConnection(connection.getConnection());
        clientSession.setTags(clientTagHandler);

        //Send Reply
        VersionRequest response = new VersionRequest();
        Message rspVersion = response.toMessage();
        rspVersion.messageType = P9Protocol.RVERSION;
        rspVersion.tag = P9Protocol.NO_TAG;
        clientTagHandler.registerTag(P9Protocol.NO_TAG);
        connection.reply(rspVersion);

        //Log
        logger.debug("Started Session for Descriptor " + Long.toString(connection.getDescriptor().getDescriptorId()));

        return true;
    }

    @Override
    public boolean attachRequest(RequestConnection connection, AttachRequest request) {

        if(!validateSession(connection.getDescriptor(), connection.getOriginalMessage())) {
            sendErrorMessage(connection,NO_SESSION);
            return false;
        }

        //We only accept NO_FID here
        if(request.getAfid() != P9Protocol.NO_FID) {
            logger.error(INVALID_FID);
            sendErrorMessage(connection, INVALID_FID);
            return false;
        }

        clientSession.setUserName(request.getUsername());
        clientSession.setNameSpace(request.getNamespace());

        GelatoFileDescriptor authDescriptor = new GelatoFileDescriptor();
        GelatoFileDescriptor resourceDescriptor = new GelatoFileDescriptor();

        authDescriptor.setRawFileDescriptor(P9Protocol.NO_FID);
        clientDescriptors.registerDescriptor(authDescriptor);
        resourceDescriptor.setRawFileDescriptor(request.getFid());
        clientSession.setAuthorisationDescriptor(authDescriptor);
        clientSession.getManager().mapQID(resourceDescriptor, rootAttach.getFileDescriptor());

        //Send Response
        AttachResponse response = new AttachResponse();
        response.setTag(connection.getOriginalMessage().tag);
        response.setServerID(rootAttach.getQID());

        connection.reply(response);
        logger.debug(
                "User: "
                        + request.getUsername()
                        + " Mapped ROOT against FID: "
                        + Long.toString(ByteEncoder.getUnsigned(request.getFid()))
                        + " Connection: "
                        + Long.toString(connection.getDescriptor().getDescriptorId()));

        return true;
    }



    @Override
    public boolean processAuthRequest(RequestConnection request, AuthRequest authRequest) {
        sendErrorMessage(request,NOT_SUPPORTED);
        return false;
    }

    @Override
    public boolean processUnknown(RequestConnection request) {
        sendErrorMessage(request,NOT_SUPPORTED);
        return false;
    }

    @CiotolaServiceRun
    public void process() {
        nextHandlerChain = manager.getParallelRequestHandler();
        rootAttach = manager.getRoot();
        while (!isShutdown()) {
            processMessages();
        }
    }

    @CiotolaServiceStop
    public synchronized void shutdown() {
        shutdown = true;
    }

    @CiotolaServiceStart
    public synchronized void start() {
        shutdown = false;
    }

    public synchronized boolean isShutdown() {
        return shutdown;
    }



}
