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

import gelato.GelatoConnection;
import gelato.GelatoDescriptorManager;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.manager.implementation.NullAuthorisation;
import gelato.server.manager.requests.GenericRequestHandler;
import gelato.server.manager.requests.RequestAttachHandler;
import gelato.server.manager.response.ResponseAttachHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.ByteEncoder;
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.QID;
import protocol.messages.Message;
import protocol.messages.request.AttachRequest;
import protocol.messages.request.AuthRequest;
import protocol.messages.response.AttachResponse;
import protocol.messages.response.AuthResponse;

public class GelatoSessionHandler extends GelatoAbstractGenericRequestHandler
    implements RequestAttachHandler, ResponseAttachHandler {

  private final Logger logger = LoggerFactory.getLogger(GelatoSessionHandler.class);
  private GelatoQIDManager qidManager;
  private GenericRequestHandler handler;
  private GelatoAuthorisationManager authorisationManager;
  private GelatoGelatoAbstractResourcetHandler rootAttach;
  private ResponseAttachHandler responseAttachHandler = this;

  public GelatoSessionHandler(GelatoQIDManager gelatoQIDManager, GenericRequestHandler handler) {
    qidManager = gelatoQIDManager;
    this.handler = handler;
    authorisationManager = new NullAuthorisation();
  }

  public GelatoSessionHandler(
      GelatoQIDManager gelatoQIDManager,
      GenericRequestHandler handler,
      GelatoGelatoAbstractResourcetHandler root) {
    qidManager = gelatoQIDManager;
    this.handler = handler;
    authorisationManager = new NullAuthorisation();
    rootAttach = root;
  }

  public GelatoSessionHandler(
      GelatoQIDManager gelatoQIDManager,
      GenericRequestHandler nextHandler,
      GelatoAuthorisationManager authorisationManager,
      GelatoGelatoAbstractResourcetHandler root) {
    qidManager = gelatoQIDManager;
    handler = nextHandler;
    this.authorisationManager = authorisationManager;
    rootAttach = root;
  }

  public ResponseAttachHandler getResponseAttachHandler() {
    return responseAttachHandler;
  }

  public void setResponseAttachHandler(ResponseAttachHandler responseAttachHandler) {
    this.responseAttachHandler = responseAttachHandler;
  }

  @Override
  public boolean processRequest(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      Message request) {
    GelatoFileDescriptor auth = session.getAuthorisationDescriptor();

    // validate tag
    if (session.getTags().isRecycled(request.tag) != false) {
      logger.error(
          "Invalid Tags, Expected: "
              + Integer.toString(session.getTags().getTagCount())
              + " Got: "
              + Integer.toString(request.tag));
    }
    session.getTags().registerTag(request.tag);
    if (auth == null) {
      return requestAuth(connection, descriptor, session, request);
    } else {
      if (handler == null) {
        logger.error("Request Handler currently not setup ");
        return false;
      } else {
        return handler.processRequest(connection, descriptor, session, request);
      }
    }
  }

  @Override
  public boolean processRequest(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      AttachRequest request) {
    logger.info(
        "Handling Attach Request for Descriptor " + Long.toString(descriptor.getDescriptorId()));
    GelatoDescriptorManager sessionDescriptor = new GelatoDescriptorManager();
    GelatoFileDescriptor authDescriptor;
    GelatoFileDescriptor resourceDescriptor = new GelatoFileDescriptor();
    if (request.getAfid() == P9Protocol.NO_FID && authorisationManager.requireAuth() == false) {
      // Start an attach procedure
      session.setUserName(request.getUsername());
      authDescriptor = new GelatoFileDescriptor();
      session.setManager(sessionDescriptor);
      authDescriptor.setRawFileDescriptor(P9Protocol.NO_FID);
      sessionDescriptor.registerDescriptor(authDescriptor);

    } else if (request.getAfid() != P9Protocol.NO_FID
        && authorisationManager.requireAuth() == true) {
      // Get the descriptor agreed by the auth exchange
      authDescriptor = authorisationManager.getAuthorisedDescriptor(connection, descriptor);
      if (authDescriptor.getDescriptorId() != ByteEncoder.getUnsigned(request.getAfid())) {
        logger.error(
            "Client sent invalid AFID ID expected: "
                + Long.toString(authDescriptor.getDescriptorId())
                + "Got: "
                + Long.toString(ByteEncoder.getUnsigned(request.getAfid())));
        return false;
      }
      session.setManager(sessionDescriptor);
      authDescriptor.setRawFileDescriptor(request.getAfid());
      sessionDescriptor.registerDescriptor(authDescriptor);
    } else {
      logger.error(
          "Expected an Authorised FID - Authorisation Manager Requests client to be authenticated");
      return false;
    }
    resourceDescriptor.setRawFileDescriptor(request.getFid());
    session.setAuthorisationDescriptor(authDescriptor);
    session.getManager().mapQID(resourceDescriptor, rootAttach.getFileDescriptor());
    return true;
  }

  private boolean requestAuth(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      Message message) {
    if (message.messageType == P9Protocol.TAUTH || message.messageType == P9Protocol.TATTACH) {
      if (message.messageType == P9Protocol.TATTACH) {
        AttachRequest attachRequest = Decoder.decodeAttachRequest(message);
        if (processRequest(connection, descriptor, session, attachRequest)) {
          // Send Response
          AttachResponse response = new AttachResponse();
          response.setTag(message.tag);
          response.setServerID(rootAttach.getQID());
          responseAttachHandler.writeResponse(connection, descriptor, response);
          logger.info(
              "User: "
                  + attachRequest.getUsername()
                  + " Mapped ROOT against FID: "
                  + Long.toString(ByteEncoder.getUnsigned(attachRequest.getFid()))
                  + " Connection: "
                  + Long.toString(descriptor.getDescriptorId()));
          return true;
        } else {
          sendErrorMessage(connection, descriptor, message.tag, "Unable to Attach service");
          return false;
        }
      } else {
        return handleAuthRequest(connection, descriptor, session, message);
      }
    } else {
      logger.error("Invalid Session Creation - Expected AUTH/Attach Message");
    }
    return false;
  }

  private boolean handleAuthRequest(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      Message message) {
    AuthRequest authRequest = Decoder.decodeAuthRequest(message);
    if (authorisationManager.requireAuth()) {
      if (authorisationManager.processAuthRequest(connection, descriptor, session, authRequest)) {
        QID authQID = authorisationManager.authoriseQID(descriptor);
        session.setUserName(authRequest.getUserName());
        // setup session stuff
        GelatoDescriptorManager newDescriptorSessionManager = new GelatoDescriptorManager();
        GelatoFileDescriptor newAuth = new GelatoFileDescriptor();
        session.setManager(newDescriptorSessionManager);
        newAuth.setQid(authQID);
        newAuth.setRawFileDescriptor(authRequest.getAuthFileID());
        newDescriptorSessionManager.registerDescriptor(newAuth);

        // send response
        AuthResponse response = new AuthResponse();
        response.setTag(message.tag);
        response.setQid(authQID);
        connection.sendMessage(descriptor, response.toMessage());

        logger.info(
            "User: "
                + authRequest.getUserName()
                + " authenticated against FID: "
                + Integer.toString(authRequest.getAuthFileID())
                + " Connection: "
                + Long.toString(descriptor.getDescriptorId()));
        return true;
      } else {
        logger.error("Unable to Authenticate user " + authRequest.getUserName());
        sendErrorMessage(connection, descriptor, message.tag, "authentication failed");
      }
    } else {
      logger.error("Authorisation Manager does not support AUTH requests - Sending error");
      sendErrorMessage(
          connection,
          descriptor,
          message.tag,
          "Authorisation Manager does not support AUTH requests");
    }
    return false;
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

  public GelatoGelatoAbstractResourcetHandler getRootAttach() {
    return rootAttach;
  }

  public void setRootAttach(GelatoGelatoAbstractResourcetHandler rootAttach) {
    this.rootAttach = rootAttach;
  }

  @Override
  public boolean writeResponse(
      GelatoConnection connection, GelatoFileDescriptor fileDescriptor, AttachResponse response) {
    connection.sendMessage(fileDescriptor, response.toMessage());
    return true;
  }
}
