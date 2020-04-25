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
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;
import protocol.messages.Message;
import protocol.messages.request.CloseRequest;
import protocol.messages.request.CreateRequest;
import protocol.messages.request.OpenRequest;
import protocol.messages.request.ReadRequest;
import protocol.messages.request.RemoveRequest;
import protocol.messages.request.StatRequest;
import protocol.messages.request.WalkRequest;
import protocol.messages.request.WriteRequest;
import protocol.messages.request.WriteStatRequest;
import protocol.messages.response.ErrorMessage;

import java.time.Instant;

public class GelatoResourceControllerImpl implements GelatoResourceController {

  private final Logger logger = LoggerFactory.getLogger(GelatoResourceControllerImpl.class);
  private GelatoFileDescriptor fileDescriptor = new GelatoFileDescriptor();
  private StatStruct resourceStat = new StatStruct();
  private GelatoQIDManager resourceManager;
  private CloseRequestHandler closeRequestHandler = new DefaultCloseRequestHandler();
  private CreateRequestHandler createRequestHandler = new NotSupportedHandler();
  private OpenRequestHandler openRequestHandler = new NotSupportedHandler();
  private RemoveRequestHandler removeRequestHandler = new NotSupportedHandler();
  private StatRequestHandler statRequestHandler = new DefaultStatHandler();
  private WalkRequestHandler walkRequestHandler = new NotSupportedHandler();
  private WriteRequestHandler writeRequestHandler = new NotSupportedHandler();
  private WriteStatRequestHandler writeStatRequestHandler = new NotSupportedHandler();
  private RequestFlushHandler flushHandler = new DefaultFlushHandler();
  private ReadRequestHandler readRequestHandler = new NotSupportedHandler();

  public static final String DEFAULT_NAME = "default_resource";
  public static final String DEFAULT_USER = "default_user";
  public static final String DEFAULT_GROUP = "default_group";


  public GelatoResourceControllerImpl() {
    StatStruct newStat = getStat();
    newStat.setAccessTime(Instant.now().getEpochSecond());
    newStat.setModifiedTime(newStat.getAccessTime());
    newStat.setUid(DEFAULT_USER);
    newStat.setGid(DEFAULT_GROUP);
    newStat.setMuid(DEFAULT_GROUP);
    newStat.setName(DEFAULT_NAME);
    QID qid = getQID();
    qid.setType(P9Protocol.QID_FILE);
    qid.setVersion(0);
    qid.setLongFileId(0l);
    newStat.setQid(qid);
    newStat.updateSize();
    setStat(newStat);
  }

  public void updateSize() {
    resourceStat.updateSize();
  }


  @Override
  public QID getQID() {
    return fileDescriptor.getQid();
  }

  @Override
  public void setQID(QID value) {
    fileDescriptor.setQid(value);
  }

  @Override
  public String resourceName() {
    return getStat().getName();
  }

  @Override
  public StatStruct getStat() {
    return resourceStat;
  }

  @Override
  public void setStat(StatStruct newStat) {
    resourceStat = newStat;
    resourceStat.updateSize();
  }

  @Override
  public GelatoFileDescriptor getFileDescriptor() {
    return fileDescriptor;
  }

  @Override
  public void setFileDescriptor(GelatoFileDescriptor descriptor) {
    fileDescriptor = descriptor;
  }

  @Override
  public void setResourceName(String newName) {
    getStat().setName(newName);
    getStat().updateSize();
  }

  @Override
  public void setQidManager(GelatoQIDManager newManager) {
    resourceManager = newManager;
  }

  @Override
  public GelatoQIDManager getResourceManager() {
    return resourceManager;
  }

  @Override
  public boolean processRequest(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      Message request) {
    RequestConnection requestConnection = new RequestConnection();
    requestConnection.setConnection(connection);
    requestConnection.setDescriptor(descriptor);
    requestConnection.setSession(session);
    requestConnection.setResourceController(this);
    requestConnection.setTransactionId(request.tag);

    if (request.messageType == P9Protocol.TOPEN) {
      OpenRequest openRequest = Decoder.decodeOpenRequest(request);
      GelatoFileDescriptor clientDescriptor = new GelatoFileDescriptor();
      clientDescriptor.setQid(getQID());
      clientDescriptor.setRawFileDescriptor(openRequest.getFileDescriptor());
      return openRequestHandler.openRequest(
          requestConnection, clientDescriptor, openRequest.getMode());
    } else if (request.messageType == P9Protocol.TWALK) {
      WalkRequest walkRequest = Decoder.decodeWalkRequest(request);
      GelatoFileDescriptor clientDescriptor =
          generateDescriptor(getQID(), walkRequest.getNewDecriptor());
      return walkRequestHandler.walkRequest(
          requestConnection, walkRequest.getTargetFile(), clientDescriptor);
    } else if (request.messageType == P9Protocol.TFLUSH) {
      return flushHandler.processRequest(
          connection, descriptor, session, Decoder.decodeFlushRequest(request));
    } else if (request.messageType == P9Protocol.TREMOVE) {
      RemoveRequest removeRequest = Decoder.decodeRemoveRequest(request);
      GelatoFileDescriptor descriptor1 =
          generateDescriptor(getQID(), removeRequest.getFileDescriptor());
      return removeRequestHandler.removeRequest(requestConnection, descriptor1);
    } else if (request.messageType == P9Protocol.TWSTAT) {
      WriteStatRequest writeStatRequest = Decoder.decodeStatWriteRequest(request);
      GelatoFileDescriptor descriptor1 =
          generateDescriptor(getQID(), writeStatRequest.getFileDescriptor());
      return writeStatRequestHandler.writeStatRequest(
          requestConnection, descriptor1, writeStatRequest.getStatStruct());
    } else if (request.messageType == P9Protocol.TWRITE) {
      WriteRequest writeRequest = Decoder.decodeWriteRequest(request);
      GelatoFileDescriptor descriptor1 =
          generateDescriptor(getQID(), writeRequest.getFileDescriptor());
      return writeRequestHandler.writeRequest(
          requestConnection,
          descriptor1,
          writeRequest.getFileOffset(),
          writeRequest.getWriteData());
    } else if (request.messageType == P9Protocol.TCLOSE) {
      CloseRequest closeRequest = Decoder.decodeCloseRequest(request);
      GelatoFileDescriptor descriptor1 = generateDescriptor(getQID(), closeRequest.getFileID());
      return closeRequestHandler.closeRequest(requestConnection, descriptor1);
    } else if (request.messageType == P9Protocol.TREAD) {
      ReadRequest readRequest = Decoder.decodeReadRequest(request);
      GelatoFileDescriptor descriptor1 =
          generateDescriptor(getQID(), readRequest.getFileDescriptor());
      return readRequestHandler.readRequest(
          requestConnection,
          descriptor1,
          readRequest.getFileOffset(),
          readRequest.getBytesToRead());
    } else if (request.messageType == P9Protocol.TSTAT) {
      StatRequest statRequest = Decoder.decodeStatRequest(request);
      GelatoFileDescriptor descriptor1 =
          generateDescriptor(getQID(), statRequest.getFileDescriptor());
      return statRequestHandler.statRequest(requestConnection, descriptor1);
    } else if (request.messageType == P9Protocol.TCREATE) {
      CreateRequest createRequest = Decoder.decodeCreateRequest(request);
      return createRequestHandler.createRequest(
          requestConnection,
          createRequest.getFileName(),
          createRequest.getPermission(),
          createRequest.getMode());
    }

    logger.error("Unable to Process Request");
    return false;
  }

  @Override
  public void sendErrorMessage(RequestConnection connection, String message) {
    ErrorMessage msg = new ErrorMessage();
    msg.setTag(connection.getTransactionId());
    msg.setErrorMessage(message);
    logger.error(message);
    connection.reply(msg);
  }

  @Override
  public void sendErrorMessage(
      GelatoConnection connection, GelatoFileDescriptor descriptor, int tag, String message) {
    ErrorMessage msg = new ErrorMessage();
    msg.setTag(tag);
    msg.setErrorMessage(message);
    logger.error(message);
    connection.sendMessage(descriptor, msg.toMessage());
  }

  @Override
  public RequestConnection createConnection(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      int tag) {
    RequestConnection con = new RequestConnection();
    con.setConnection(connection);
    con.setDescriptor(descriptor);
    con.setSession(session);
    con.setTransactionId(tag);
    return con;
  }

  @Override
  public CloseRequestHandler getCloseRequestHandler() {
    return closeRequestHandler;
  }

  @Override
  public void setCloseRequestHandler(CloseRequestHandler closeRequestHandler) {
    this.closeRequestHandler = closeRequestHandler;
  }

  @Override
  public CreateRequestHandler getCreateRequestHandler() {
    return createRequestHandler;
  }

  @Override
  public void setCreateRequestHandler(CreateRequestHandler createRequestHandler) {
    this.createRequestHandler = createRequestHandler;
  }

  @Override
  public OpenRequestHandler getOpenRequestHandler() {
    return openRequestHandler;
  }

  @Override
  public void setOpenRequestHandler(OpenRequestHandler openRequestHandler) {
    this.openRequestHandler = openRequestHandler;
  }

  @Override
  public RemoveRequestHandler getRemoveRequestHandler() {
    return removeRequestHandler;
  }

  @Override
  public void setRemoveRequestHandler(RemoveRequestHandler removeRequestHandler) {
    this.removeRequestHandler = removeRequestHandler;
  }

  @Override
  public StatRequestHandler getStatRequestHandler() {
    return statRequestHandler;
  }

  @Override
  public void setStatRequestHandler(StatRequestHandler statRequestHandler) {
    this.statRequestHandler = statRequestHandler;
  }

  @Override
  public WalkRequestHandler getWalkRequestHandler() {
    return walkRequestHandler;
  }

  @Override
  public void setWalkRequestHandler(WalkRequestHandler walkRequestHandler) {
    this.walkRequestHandler = walkRequestHandler;
  }

  @Override
  public WriteRequestHandler getWriteRequestHandler() {
    return writeRequestHandler;
  }

  @Override
  public void setWriteRequestHandler(WriteRequestHandler writeRequestHandler) {
    this.writeRequestHandler = writeRequestHandler;
  }

  @Override
  public WriteStatRequestHandler getWriteStatRequestHandler() {
    return writeStatRequestHandler;
  }

  @Override
  public void setWriteStatRequestHandler(WriteStatRequestHandler writeStatRequestHandler) {
    this.writeStatRequestHandler = writeStatRequestHandler;
  }

  @Override
  public GelatoFileDescriptor generateDescriptor(QID qid, int descriptor) {
    GelatoFileDescriptor clientDescriptor = new GelatoFileDescriptor();
    clientDescriptor.setQid(getQID());
    clientDescriptor.setRawFileDescriptor(descriptor);
    return clientDescriptor;
  }

  @Override
  public RequestFlushHandler getFlushHandler() {
    return flushHandler;
  }

  @Override
  public void setFlushHandler(RequestFlushHandler flushHandler) {
    this.flushHandler = flushHandler;
  }

  @Override
  public ReadRequestHandler getReadRequestHandler() {
    return readRequestHandler;
  }

  @Override
  public void setReadRequestHandler(ReadRequestHandler readRequestHandler) {
    this.readRequestHandler = readRequestHandler;
  }
}
