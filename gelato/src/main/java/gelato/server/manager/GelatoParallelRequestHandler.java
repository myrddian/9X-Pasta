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

import ciotola.Ciotola;
import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.manager.controllers.GelatoResourceController;
import gelato.server.manager.controllers.impl.DefaultFlushHandler;
import gelato.server.manager.implementation.ParallelRequest;
import gelato.server.manager.implementation.requests.RequestFlushHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.messages.Message;

public class GelatoParallelRequestHandler implements GenericRequestHandler {

  private final Logger logger = LoggerFactory.getLogger(GelatoParallelRequestHandler.class);
  private GelatoQIDManager resources;
  private RequestFlushHandler flushResponseHandler = new DefaultFlushHandler();

  public GelatoParallelRequestHandler(GelatoQIDManager qidManager) {
    resources = qidManager;
  }

  public RequestFlushHandler getFlushResponseHandler() {
    return flushResponseHandler;
  }

  public void setFlushResponseHandler(RequestFlushHandler flushResponseHandler) {
    this.flushResponseHandler = flushResponseHandler;
  }

  @Override
  public boolean processRequest(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      Message request) {
    GelatoFileDescriptor requestedResource = new GelatoFileDescriptor();
    if (request.messageType == P9Protocol.TOPEN) {
      requestedResource.setRawFileDescriptor(
          Decoder.decodeOpenRequest(request).getFileDescriptor());
    } else if (request.messageType == P9Protocol.TWALK) {
      requestedResource.setRawFileDescriptor(
          Decoder.decodeWalkRequest(request).getBaseDescriptor());
    } else if (request.messageType == P9Protocol.TFLUSH) {
      return flushResponseHandler.processRequest(
          connection, descriptor, session, Decoder.decodeFlushRequest(request));
    } else if (request.messageType == P9Protocol.TREMOVE) {
      requestedResource.setRawFileDescriptor(
          Decoder.decodeRemoveRequest(request).getFileDescriptor());
    } else if (request.messageType == P9Protocol.TWSTAT) {
      requestedResource.setRawFileDescriptor(
          Decoder.decodeStatWriteRequest(request).getFileDescriptor());
    } else if (request.messageType == P9Protocol.TWRITE) {
      requestedResource.setRawFileDescriptor(
          Decoder.decodeWriteRequest(request).getFileDescriptor());
    } else if (request.messageType == P9Protocol.TCLOSE) {
      requestedResource.setRawFileDescriptor(Decoder.decodeCloseRequest(request).getFileID());
    } else if (request.messageType == P9Protocol.TREAD) {
      requestedResource.setRawFileDescriptor(
          Decoder.decodeReadRequest(request).getFileDescriptor());
    } else if (request.messageType == P9Protocol.TSTAT) {
      requestedResource.setRawFileDescriptor(
          Decoder.decodeStatRequest(request).getFileDescriptor());
    } else if (request.messageType == P9Protocol.TCREATE) {
      requestedResource.setRawFileDescriptor(
          Decoder.decodeCreateRequest(request).getFileDescriptor());
    }
    GelatoFileDescriptor serverResource =
        session.getManager().getServerDescriptor(requestedResource);
    GelatoResourceController handler = resources.getHandler(serverResource);

    ParallelRequest parallelRequest = new ParallelRequest();
    parallelRequest.setConnection(connection);
    parallelRequest.setHandler(handler);
    parallelRequest.setDescriptor(descriptor);
    parallelRequest.setSession(session);
    parallelRequest.setMessage(request);
    Ciotola.getInstance().execute(parallelRequest,serverResource.getQid().getLongFileId());
    return true;
  }
}
