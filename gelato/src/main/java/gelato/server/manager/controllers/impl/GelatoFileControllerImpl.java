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

package gelato.server.manager.controllers.impl;

import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.manager.GelatoQIDManager;
import gelato.server.manager.RequestConnection;
import gelato.server.manager.controllers.GelatoFileController;
import gelato.server.manager.controllers.GelatoResourceController;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;
import protocol.messages.Message;
import protocol.messages.response.OpenResponse;
import protocol.messages.response.ReadResponse;
import protocol.messages.response.StatResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class GelatoFileControllerImpl
    implements GelatoFileController, ReadRequestHandler, StatRequestHandler, OpenRequestHandler {

  private final Logger logger = LoggerFactory.getLogger(GelatoFileControllerImpl.class);
  private GelatoResourceController resourceController = new GelatoResourceControllerImpl();
  private InputStream fileInputStream;

  public GelatoFileControllerImpl(
      String fileName,
      InputStream inputStream,
      long resourceSize,
      GelatoFileDescriptor descriptor) {
    resourceController.setReadRequestHandler(this);
    resourceController.setOpenRequestHandler(this);
    resourceController.setStatRequestHandler(this);
    fileInputStream = inputStream;
    resourceController.getStat().setName(fileName);
    resourceController.getStat().setLength(resourceSize);
    resourceController.getStat().getQid().setType(P9Protocol.QID_FILE);
    resourceController.setFileDescriptor(descriptor);
    resourceController.getStat().updateSize();
  }

  public InputStream getFileInputStream() {
    return fileInputStream;
  }

  public void setFileInputStream(InputStream fileInputStream) {
    this.fileInputStream = fileInputStream;
  }

  // Defaults

  @Override
  public boolean openRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, byte mode) {
    if (mode == P9Protocol.OPEN_MODE_OREAD) {
      OpenResponse response = new OpenResponse();
      response.setFileQID(getQID());
      connection.reply(response);
      return true;
    }
    sendErrorMessage(connection, "Only READ mode is allowed");
    return false;
  }

  @Override
  public boolean readRequest(
      RequestConnection connection,
      GelatoFileDescriptor clientFileDescriptor,
      long offset,
      int numberOfBytes) {
    // Reset stream
    try {
      int ptr = 0;
      fileInputStream.reset();
      fileInputStream.skip(offset);
      byte[] buff = new byte[P9Protocol.MAX_MSG_CONTENT_SIZE];

      while (ptr < numberOfBytes) {
        int copyByte = numberOfBytes - ptr;
        if (copyByte > P9Protocol.MAX_MSG_CONTENT_SIZE) {
          copyByte = (int) P9Protocol.MAX_MSG_CONTENT_SIZE;
        }
        // Copy stream to buffer
        for (int i = 0; i < copyByte; ++i) {
          buff[i] = (byte) fileInputStream.read();
        }
        ReadResponse readResponse = new ReadResponse();
        readResponse.setData(Arrays.copyOf(buff, copyByte));
        connection.reply(readResponse);
        ptr += copyByte;
      }

      return true;
    } catch (IOException e) {
      logger.error("Error in reading Input stream", e);
      return false;
    }
  }

  @Override
  public boolean statRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
    StatStruct selfStat = getStat();
    StatResponse response = new StatResponse();
    response.setStatStruct(selfStat);
    connection.reply(response);
    return true;
  }

  // ResourceController Proxy

  @Override
  public QID getQID() {
    return resourceController.getQID();
  }

  @Override
  public void setQID(QID value) {
    resourceController.setQID(value);
  }

  @Override
  public String resourceName() {
    return resourceController.resourceName();
  }

  @Override
  public StatStruct getStat() {
    return resourceController.getStat();
  }

  @Override
  public void setStat(StatStruct newStat) {
    resourceController.setStat(newStat);
  }

  @Override
  public GelatoFileDescriptor getFileDescriptor() {
    return resourceController.getFileDescriptor();
  }

  @Override
  public void setFileDescriptor(GelatoFileDescriptor descriptor) {
    resourceController.setFileDescriptor(descriptor);
  }

  @Override
  public void setResourceName(String newName) {
    resourceController.setResourceName(newName);
  }

  @Override
  public void setQidManager(GelatoQIDManager newManager) {
    resourceController.setQidManager(newManager);
  }

  @Override
  public GelatoQIDManager getResourceManager() {
    return resourceController.getResourceManager();
  }

  @Override
  public void sendErrorMessage(RequestConnection connection, String message) {
    resourceController.sendErrorMessage(connection, message);
  }

  @Override
  public void sendErrorMessage(
      GelatoConnection connection, GelatoFileDescriptor descriptor, int tag, String message) {
    resourceController.sendErrorMessage(connection, descriptor, tag, message);
  }

  @Override
  public RequestConnection createConnection(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      int tag) {
    return resourceController.createConnection(connection, descriptor, session, tag);
  }

  @Override
  public CloseRequestHandler getCloseRequestHandler() {
    return resourceController.getCloseRequestHandler();
  }

  @Override
  public void setCloseRequestHandler(CloseRequestHandler closeRequestHandler) {
    resourceController.setCloseRequestHandler(closeRequestHandler);
  }

  @Override
  public CreateRequestHandler getCreateRequestHandler() {
    return resourceController.getCreateRequestHandler();
  }

  @Override
  public void setCreateRequestHandler(CreateRequestHandler createRequestHandler) {
    resourceController.setCreateRequestHandler(createRequestHandler);
  }

  @Override
  public OpenRequestHandler getOpenRequestHandler() {
    return resourceController.getOpenRequestHandler();
  }

  @Override
  public void setOpenRequestHandler(OpenRequestHandler openRequestHandler) {
    resourceController.setOpenRequestHandler(openRequestHandler);
  }

  @Override
  public RemoveRequestHandler getRemoveRequestHandler() {
    return resourceController.getRemoveRequestHandler();
  }

  @Override
  public void setRemoveRequestHandler(RemoveRequestHandler removeRequestHandler) {
    resourceController.setRemoveRequestHandler(removeRequestHandler);
  }

  @Override
  public StatRequestHandler getStatRequestHandler() {
    return resourceController.getStatRequestHandler();
  }

  @Override
  public void setStatRequestHandler(StatRequestHandler statRequestHandler) {
    resourceController.setStatRequestHandler(statRequestHandler);
  }

  @Override
  public WalkRequestHandler getWalkRequestHandler() {
    return resourceController.getWalkRequestHandler();
  }

  @Override
  public void setWalkRequestHandler(WalkRequestHandler walkRequestHandler) {
    resourceController.setWalkRequestHandler(walkRequestHandler);
  }

  @Override
  public WriteRequestHandler getWriteRequestHandler() {
    return resourceController.getWriteRequestHandler();
  }

  @Override
  public void setWriteRequestHandler(WriteRequestHandler writeRequestHandler) {
    resourceController.setWriteRequestHandler(writeRequestHandler);
  }

  @Override
  public WriteStatRequestHandler getWriteStatRequestHandler() {
    return resourceController.getWriteStatRequestHandler();
  }

  @Override
  public void setWriteStatRequestHandler(WriteStatRequestHandler writeStatRequestHandler) {
    resourceController.setWriteStatRequestHandler(writeStatRequestHandler);
  }

  @Override
  public GelatoFileDescriptor generateDescriptor(QID qid, int descriptor) {
    return resourceController.generateDescriptor(qid, descriptor);
  }

  @Override
  public RequestFlushHandler getFlushHandler() {
    return resourceController.getFlushHandler();
  }

  @Override
  public void setFlushHandler(RequestFlushHandler flushHandler) {
    resourceController.setFlushHandler(flushHandler);
  }

  @Override
  public ReadRequestHandler getReadRequestHandler() {
    return resourceController.getReadRequestHandler();
  }

  @Override
  public void setReadRequestHandler(ReadRequestHandler readRequestHandler) {
    resourceController.setReadRequestHandler(readRequestHandler);
  }

  @Override
  public boolean processRequest(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      Message request) {
    return resourceController.processRequest(connection, descriptor, session, request);
  }

  @Override
  public GelatoResourceController getResourceController() {
    return resourceController;
  }

  @Override
  public void setResourceController(GelatoResourceController resourceController) {
    this.resourceController = resourceController;
  }
}
