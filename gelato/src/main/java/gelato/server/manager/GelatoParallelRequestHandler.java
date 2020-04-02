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
import gelato.Gelato;
import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.manager.implementation.IgnoreFlushRequests;
import gelato.server.manager.implementation.ParallelHandler;
import gelato.server.manager.implementation.ParallelRequest;
import gelato.server.manager.requests.GenericRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.messages.Message;

import java.util.ArrayList;
import java.util.List;

public class GelatoParallelRequestHandler implements GenericRequestHandler {

  private final Logger logger = LoggerFactory.getLogger(GelatoParallelRequestHandler.class);
  private Gelato library;
  private GelatoQIDManager resources;
  private List<ParallelHandler> workers = new ArrayList<>();

  public GelatoParallelRequestHandler(Gelato library, GelatoQIDManager qidManager) {
    resources = qidManager;
    this.library = library;;

    for(int i=0; i < library.threadCapacity(); ++i) {
      ParallelHandler handler = new ParallelHandler();
      workers.add(handler);
      Ciotola.getInstance().injectService(handler);
    }

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
      return IgnoreFlushRequests.sendFlushResponse(
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
    GelatoGelatoAbstractResourcetHandler handler = resources.getHandler(serverResource);

    int scheduleGroup =
        Math.abs((int) (serverResource.getQid().getLongFileId() % library.threadCapacity()));

    ParallelRequest parallelRequest = new ParallelRequest();
    parallelRequest.setConnection(connection);
    parallelRequest.setHandler(handler);
    parallelRequest.setDescriptor(descriptor);
    parallelRequest.setSession(session);
    parallelRequest.setMessage(request);
    workers.get(scheduleGroup).addMessage(parallelRequest);
    return true;
  }
}
