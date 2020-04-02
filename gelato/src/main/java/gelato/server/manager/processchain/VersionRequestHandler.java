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

import gelato.Gelato;
import gelato.GelatoConnection;
import gelato.GelatoDescriptorManager;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.manager.GelatoAbstractGenericRequestHandler;
import gelato.server.manager.GelatoServerSession;
import gelato.server.manager.requests.RequestVersionHandler;
import gelato.server.manager.response.VersionResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.messages.Message;
import protocol.messages.VersionRequest;

public class VersionRequestHandler extends GelatoAbstractGenericRequestHandler
    implements RequestVersionHandler, VersionResponseHandler {

  private final Logger logger = LoggerFactory.getLogger(VersionRequestHandler.class);
  private GelatoAbstractGenericRequestHandler next;
  private RequestVersionHandler versionRequestHandler = this;
  private VersionResponseHandler versionResponseHandler = this;
  private GelatoSession clientSession = null;
  private Gelato library;

  public Gelato getLibrary() {
    return library;
  }

  public void setLibrary(Gelato library) {
    this.library = library;
  }

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
  public boolean processRequest(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      Message request) {
    if (request.messageType != P9Protocol.TVERSION && clientSession == null) {
      logger.error(
          "Descriptor has not started a valid session "
              + Long.toString(descriptor.getDescriptorId())
              + " "
              + Byte.toString(request.messageType));
      sendErrorMessage(connection, descriptor, request.tag, "");
      return false;
    } else if (request.messageType == P9Protocol.TVERSION) {
      return versionRequestHandler.processRequest(
          connection, descriptor, Decoder.decodeVersionRequest(request));
    }
    return next.processRequest(connection, descriptor, clientSession, request);
  }

  @Override
  public boolean processRequest(
      GelatoConnection connection, GelatoFileDescriptor descriptor, VersionRequest request) {
    library.getTagManager().createTagHandler(descriptor);
    GelatoServerSession newSession = new GelatoServerSession();
    newSession.setManager(new GelatoDescriptorManager());
    newSession.setConnection(connection);
    newSession.setTags(library.getTagManager().getManager(descriptor));
    VersionRequest response = new VersionRequest();
    response.setTag(request.getMessageTag());
    newSession.getTags().registerTag(request.getMessageTag());
    clientSession = newSession;
    logger.info("Started Session for Descriptor " + Long.toString(descriptor.getDescriptorId()));
    return versionResponseHandler.writeResponse(connection, descriptor, response);
  }

  @Override
  public boolean writeResponse(
      GelatoConnection connection, GelatoFileDescriptor fileDescriptor, VersionRequest response) {
    Message rspVersion = response.toMessage();
    rspVersion.messageType = P9Protocol.RVERSION;
    connection.sendMessage(fileDescriptor, rspVersion);
    return true;
  }
}
