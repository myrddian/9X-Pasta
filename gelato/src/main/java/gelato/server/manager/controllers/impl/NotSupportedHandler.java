/*
 * Copyright (c) 2021.  Enzo Reyes Licensed under the Apache License, Version 2.0 (the "License");   you may
 * not use this file except in compliance with the License.   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package gelato.server.manager.controllers.impl;

import gelato.GelatoFileDescriptor;
import gelato.server.manager.RequestConnection;
import gelato.server.manager.processchain.CloseRequestHandler;
import gelato.server.manager.processchain.CreateRequestHandler;
import gelato.server.manager.processchain.OpenRequestHandler;
import gelato.server.manager.processchain.ReadRequestHandler;
import gelato.server.manager.processchain.RemoveRequestHandler;
import gelato.server.manager.processchain.StatRequestHandler;
import gelato.server.manager.processchain.WalkRequestHandler;
import gelato.server.manager.processchain.WriteRequestHandler;
import gelato.server.manager.processchain.WriteStatRequestHandler;
import protocol.StatStruct;

public class NotSupportedHandler
    implements CloseRequestHandler,
        CreateRequestHandler,
        OpenRequestHandler,
        ReadRequestHandler,
        RemoveRequestHandler,
        StatRequestHandler,
        WalkRequestHandler,
        WriteRequestHandler,
        WriteStatRequestHandler {

  private void sendError(RequestConnection connection) {
    connection
        .getResourceController()
        .sendErrorMessage(connection, "This operation is not supported");
  }

  @Override
  public boolean closeRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
    sendError(connection);
    return true;
  }

  @Override
  public boolean createRequest(
      RequestConnection connection, String fileName, int permission, byte mode) {
    sendError(connection);
    return true;
  }

  @Override
  public boolean openRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, byte mode) {
    sendError(connection);
    return true;
  }

  @Override
  public boolean readRequest(
      RequestConnection connection,
      GelatoFileDescriptor clientFileDescriptor,
      long offset,
      int numberOfBytes) {
    sendError(connection);
    return true;
  }

  @Override
  public boolean removeRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
    sendError(connection);
    return true;
  }

  @Override
  public boolean statRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
    sendError(connection);
    return true;
  }

  @Override
  public boolean walkRequest(
      RequestConnection connection, String fileName, GelatoFileDescriptor newDescriptor) {
    sendError(connection);
    return true;
  }

  @Override
  public boolean writeRequest(
      RequestConnection connection,
      GelatoFileDescriptor clientFileDescriptor,
      long offset,
      byte[] data) {
    sendError(connection);
    return true;
  }

  @Override
  public boolean writeStatRequest(
      RequestConnection connection,
      GelatoFileDescriptor clientFileDescriptor,
      StatStruct newStruct) {
    sendError(connection);
    return true;
  }
}
