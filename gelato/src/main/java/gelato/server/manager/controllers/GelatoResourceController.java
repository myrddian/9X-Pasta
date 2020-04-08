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

package gelato.server.manager.controllers;

import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.manager.GelatoQIDManager;
import gelato.server.manager.GenericRequestHandler;
import gelato.server.manager.RequestConnection;
import gelato.server.manager.implementation.requests.RequestFlushHandler;
import gelato.server.manager.processchain.CloseRequestHandler;
import gelato.server.manager.processchain.CreateRequestHandler;
import gelato.server.manager.processchain.OpenRequestHandler;
import gelato.server.manager.processchain.ReadRequestHandler;
import gelato.server.manager.processchain.RemoveRequestHandler;
import gelato.server.manager.processchain.StatRequestHandler;
import gelato.server.manager.processchain.WalkRequestHandler;
import gelato.server.manager.processchain.WriteRequestHandler;
import gelato.server.manager.processchain.WriteStatRequestHandler;
import protocol.QID;
import protocol.StatStruct;

public interface GelatoResourceController extends GenericRequestHandler {
  QID getQID();

  void setQID(QID value);

  String resourceName();

  StatStruct getStat();

  void setStat(StatStruct newStat);

  GelatoFileDescriptor getFileDescriptor();

  void setFileDescriptor(GelatoFileDescriptor descriptor);

  void setResourceName(String newName);

  void setQidManager(GelatoQIDManager newManager);

  GelatoQIDManager getResourceManager();

  void sendErrorMessage(RequestConnection connection, String message);

  void sendErrorMessage(
      GelatoConnection connection, GelatoFileDescriptor descriptor, int tag, String message);

  RequestConnection createConnection(
      GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, int tag);

  CloseRequestHandler getCloseRequestHandler();

  void setCloseRequestHandler(CloseRequestHandler closeRequestHandler);

  CreateRequestHandler getCreateRequestHandler();

  void setCreateRequestHandler(CreateRequestHandler createRequestHandler);

  OpenRequestHandler getOpenRequestHandler();

  void setOpenRequestHandler(OpenRequestHandler openRequestHandler);

  RemoveRequestHandler getRemoveRequestHandler();

  void setRemoveRequestHandler(RemoveRequestHandler removeRequestHandler);

  StatRequestHandler getStatRequestHandler();

  void setStatRequestHandler(StatRequestHandler statRequestHandler);

  WalkRequestHandler getWalkRequestHandler();

  void setWalkRequestHandler(WalkRequestHandler walkRequestHandler);

  WriteRequestHandler getWriteRequestHandler();

  void setWriteRequestHandler(WriteRequestHandler writeRequestHandler);

  WriteStatRequestHandler getWriteStatRequestHandler();

  void setWriteStatRequestHandler(WriteStatRequestHandler writeStatRequestHandler);

  GelatoFileDescriptor generateDescriptor(QID qid, int descriptor);

  RequestFlushHandler getFlushHandler();

  void setFlushHandler(RequestFlushHandler flushHandler);

  ReadRequestHandler getReadRequestHandler();

  void setReadRequestHandler(ReadRequestHandler readRequestHandler);
}
