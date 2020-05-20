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
import gelato.server.GelatoServerManager;
import gelato.server.manager.GelatoQIDManager;
import gelato.server.manager.RequestConnection;
import gelato.server.manager.controllers.GelatoDirectoryController;
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
import protocol.ByteEncoder;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;
import protocol.messages.Message;
import protocol.messages.response.OpenResponse;
import protocol.messages.response.ReadResponse;
import protocol.messages.response.StatResponse;
import protocol.messages.response.WalkResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GelatoDirectoryControllerImpl
    implements GelatoDirectoryController,
        WalkRequestHandler,
        ReadRequestHandler,
        OpenRequestHandler,
        StatRequestHandler {

  private final Logger logger = LoggerFactory.getLogger(GelatoDirectoryControllerImpl.class);
  private Map<String, GelatoDirectoryController> directories = new ConcurrentHashMap<>();
  private Map<String, GelatoFileController> files = new ConcurrentHashMap<>();
  private GelatoResourceController resourceController = new GelatoResourceControllerImpl();
  private StatStruct parentDir = new StatStruct();
  private GelatoServerManager serverManager;

  public GelatoDirectoryControllerImpl(GelatoServerManager gelatoServerManager) {
    resourceController.setWalkRequestHandler(this);
    resourceController.setReadRequestHandler(this);
    resourceController.setOpenRequestHandler(this);
    resourceController.setStatRequestHandler(this);
    resourceController.getStat().getQid().setType(P9Protocol.QID_DIR);
    serverManager = gelatoServerManager;
  }

  private long calculateSize() {
    long count = 0;
    for (String dirName : directories.keySet()) {
      GelatoDirectoryController dirHandler = directories.get(dirName);
      if (dirName.equals(PARENT_DIR)) {
        count += parentDir.getStatSize();
      } else {
        count += dirHandler.getResourceController().getStat().getStatSize();
      }
    }

    for (GelatoFileController fileHandler : files.values()) {
      count += fileHandler.getResourceController().getStat().getStatSize();
    }
    return count;
  }

  public GelatoServerManager getServerManager() {
    return serverManager;
  }

  // Directory Functionality

  @Override
  public GelatoResourceController getResourceController() {
    return resourceController;
  }

  @Override
  public void setResourceController(GelatoResourceController resourceController) {
    this.resourceController = resourceController;
  }

  @Override
  public String getDirectoryName() {
    return resourceController.resourceName();
  }

  @Override
  public void setDirectoryName(String name) {
    resourceController.setResourceName(name);
  }

  @Override
  public boolean containsResource(String resourceName) {
    if (directories.containsKey(resourceName) || files.containsKey(resourceName)) {
      return true;
    }
    return false;
  }

  @Override
  public GelatoResourceController getResource(String resourceName) {
    if (!containsResource(resourceName)) {
      return null;
    }
    if (directories.containsKey(resourceName)) {
      return directories.get(resourceName).getResourceController();
    } else {
      return files.get(resourceName).getResourceController();
    }
  }

  @Override
  public void mapPaths(GelatoDirectoryController parentDir) {
    directories.put(GelatoDirectoryController.PARENT_DIR, parentDir);
    this.parentDir = parentDir.getResourceController().getStat().duplicate();
    this.parentDir.setName(GelatoDirectoryController.PARENT_DIR);
    this.parentDir.updateSize();
  }

  @Override
  public void addDirectory(GelatoDirectoryController newDirectory) {
    if (files.containsKey(newDirectory.getDirectoryName())) {
      logger.error("Cannot add the a Directory Handler which is a file");
      return;
    }
    if (directories.containsKey(newDirectory.getDirectoryName())) {
      logger.error("Cannot add the same directory twice");
      return;
    }
    directories.put(newDirectory.getDirectoryName(), newDirectory);
    newDirectory.mapPaths(this);
    serverManager.addResource(newDirectory);
  }

  @Override
  public void addFile(GelatoFileController newFile) {
    if (files.containsKey(newFile.getResourceController().resourceName())) {
      logger.error("Cannot add the same file twice");
      return;
    }
    if (directories.containsKey(newFile.getResourceController().resourceName())) {
      logger.error("Cannot add the a File Handler which is a Directory");
      return;
    }
    files.put(newFile.getResourceController().resourceName(), newFile);
    serverManager.addResource(newFile);
  }

  @Override
  public void removeFile(GelatoFileController file) {
    files.remove(file.getResourceController().resourceName());
    serverManager.removeResource(file);
  }

  // Provided built in functionality that can be overridden

  @Override
  public boolean walkRequest(
      RequestConnection connection, String fileName, GelatoFileDescriptor newDescriptor) {
    if (!containsResource(fileName)) {
      sendErrorMessage(connection, "File not found");
      return false;
    }
    GelatoResourceController resourceHandler = getResource(fileName);
    connection.getSession().getManager().mapQID(newDescriptor, resourceHandler.getFileDescriptor());
    WalkResponse response = new WalkResponse();
    response.setQID(resourceHandler.getQID());
    connection.reply(response);
    return true;
  }

  @Override
  public boolean readRequest(
      RequestConnection connection,
      GelatoFileDescriptor clientFileDescriptor,
      long offset,
      int numberOfBytes) {
    if (directories.size() == 0 && files.size() == 0) {
      ReadResponse readResponse = new ReadResponse();
      connection.reply(readResponse);
    }

    // Serialise the whole struct to memory
    byte[] statBuff = new byte[numberOfBytes];
    int ptr = 0;
    byte[] encodedStat = null;

    for (String dirName : directories.keySet()) {
      GelatoResourceController dir = directories.get(dirName);
      StatStruct statStruct = dir.getStat();
      if (dirName.equals(GelatoDirectoryController.PARENT_DIR)) {
        statStruct = this.parentDir;
      }
      encodedStat = statStruct.EncodeStat();
      ByteEncoder.copyBytesTo(encodedStat, statBuff, ptr, encodedStat.length);
      ptr += encodedStat.length;
    }

    for (GelatoFileController files : files.values()) {
      StatStruct statStruct = files.getResourceController().getStat();
      encodedStat = statStruct.EncodeStat();
      ByteEncoder.copyBytesTo(encodedStat, statBuff, ptr, encodedStat.length);
      ptr += encodedStat.length;
    }

    if (statBuff.length < P9Protocol.MAX_MSG_CONTENT_SIZE) {
      ReadResponse readResponse = new ReadResponse();
      readResponse.setData(statBuff);
      connection.reply(readResponse);
    } else {
      ptr = 0;
      while (ptr < statBuff.length) {
        int copy = statBuff.length - ptr;
        if (copy > P9Protocol.MAX_MSG_CONTENT_SIZE) {
          copy = P9Protocol.MAX_MSG_CONTENT_SIZE;
        }
        encodedStat = new byte[copy];
        ReadResponse readResponse = new ReadResponse();
        ByteEncoder.copyBytesTo(encodedStat, statBuff, ptr, copy);
        readResponse.setData(encodedStat);
        connection.reply(readResponse);
        ptr += copy;
      }
    }
    return true;
  }

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
  public boolean statRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
    StatStruct selfStat = getStat();
    selfStat.setLength(calculateSize());
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
}
