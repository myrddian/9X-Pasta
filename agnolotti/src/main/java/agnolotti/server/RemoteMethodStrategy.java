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

package agnolotti.server;

import agnolotti.Agnolotti;
import agnolotti.schema.AgnelottiSchema;
import agnolotti.schema.JsonContract;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.manager.RequestConnection;
import gelato.server.manager.controllers.GelatoFileController;
import gelato.server.manager.controllers.GelatoResourceController;
import gelato.server.manager.controllers.impl.GelatoResourceControllerImpl;
import gelato.server.manager.processchain.CloseRequestHandler;
import gelato.server.manager.processchain.OpenRequestHandler;
import gelato.server.manager.processchain.ReadRequestHandler;
import gelato.server.manager.processchain.StatRequestHandler;
import gelato.server.manager.processchain.WriteRequestHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;
import protocol.messages.response.CloseResponse;
import protocol.messages.response.OpenResponse;
import protocol.messages.response.ReadResponse;
import protocol.messages.response.StatResponse;
import protocol.messages.response.WriteResponse;

public class RemoteMethodStrategy extends GelatoResourceControllerImpl
    implements GelatoFileController,
    WriteRequestHandler,
    ReadRequestHandler,
    StatRequestHandler,
    OpenRequestHandler,
    CloseRequestHandler {

  public static final String WRITE_BYTES = "writes";
  public static final String RESOURCE = "resource";
  public static final String MODE = "mode";
  public static final String RETURN_BYTE = "returnValue";
  private final Logger logger = LoggerFactory.getLogger(RemoteMethodStrategy.class);
  private Method invoke;
  private Object service;
  private JsonContract contract;

  public RemoteMethodStrategy(Method method, Object service, long id) {
    super();

    invoke = method;
    this.service = service;
    setOpenRequestHandler(this);
    setReadRequestHandler(this);
    setWriteRequestHandler(this);
    setStatRequestHandler(this);
    setCloseRequestHandler(this);
    getResourceController().getStat().setName(methodDecorator());
    StatStruct newStat = getStat();
    newStat.setAccessTime(Instant.now().getEpochSecond());
    newStat.setModifiedTime(newStat.getAccessTime());
    newStat.setUid(Agnolotti.DEFAULT_NAME);
    newStat.setGid(Agnolotti.DEFAULT_NAME);
    newStat.setMuid(Agnolotti.DEFAULT_NAME);
    QID qid = getQID();
    qid.setType(P9Protocol.QID_FILE);
    qid.setVersion(0);
    qid.setLongFileId(id);
    newStat.setQid(qid);
    newStat.updateSize();
    setStat(newStat);
    logger.debug("Binding Method to file: " + methodDecorator());
    contract = AgnelottiSchema.getInstance().getJsonIdlForMethod(invoke);
  }

  public String methodDecorator() {
    return RemoteServiceFactory.getMethodDecorator(invoke);
  }

  public Object callMethod(Object[] args) throws Throwable {
    return invoke.invoke(service, args);
  }

  public Method getMethod() {
    return invoke;
  }

  public Map<String, Object> getJsonContract() {
    return contract.getJsonMapping();
  }

  private void invokeParamNoReturn(JsonObject marshalledObject) {
    logger.trace("Invoking Parameter-No-Return Strategy");
    try {
      Object[] methodParameter =
          AgnelottiSchema.getInstance().getParametersFromJson(marshalledObject, contract);
      invoke.invoke(service, methodParameter);
    } catch (IllegalAccessException e) {
      logger.error("Method Access is illegal ", e);
    } catch (InvocationTargetException e) {
      logger.error("Cannot invoke target method", e);
    }
  }

  private void invokeReturnNoParam(Map<String, Object> sessionDescriptorVar) {
    logger.trace("Invoking No-Parameter-Return Strategy");
    try {
      Object[] nullObj = null;
      Object retValue = invoke.invoke(service, nullObj);
      setReturnData(
          AgnelottiSchema.getInstance().generateReturnJson(retValue), sessionDescriptorVar);
    } catch (IllegalAccessException e) {
      logger.error("Method Access is illegal ", e);
    } catch (InvocationTargetException e) {
      logger.error("Cannot invoke target method", e);
    }
  }

  private void invokeNoRetNoParam() {
    logger.trace("Invoking No-Parameter-No-Return Strategy");
    try {
      Object[] nullObj = null;
      invoke.invoke(service, nullObj);
    } catch (IllegalAccessException e) {
      logger.error("Method Access is illegal ", e);
    } catch (InvocationTargetException e) {
      logger.error("Cannot invoke target method", e);
    }
  }

  private void invokeRetParam(
      JsonObject marshalledObject, Map<String, Object> sessionDescriptorVar) {
    logger.trace("Invoking Parameter-Return Strategy");
    try {
      Object[] parameter =
          AgnelottiSchema.getInstance().getParametersFromJson(marshalledObject, contract);
      Object retValue = invoke.invoke(service, parameter);
      setReturnData(
          AgnelottiSchema.getInstance().generateReturnJson(retValue), sessionDescriptorVar);
    } catch (IllegalAccessException e) {
      logger.error("Method Access is illegal ", e);
    } catch (InvocationTargetException e) {
      logger.error("Cannot invoke target method", e);
    }
  }

  private void setReturnData(String returnData, Map<String, Object> sessionDescriptorVar) {
    byte[] bytes = returnData.getBytes();
    StatStruct sessionStatStruct = getResource(sessionDescriptorVar);
    sessionStatStruct.setLength(bytes.length);
    sessionStatStruct.updateSize();
    putReturnData(sessionDescriptorVar, bytes);
  }

  private void invokeServiceMethod(List<byte[]> writes, Map<String, Object> sessionDescriptorVar) {
    logger.trace("Remote Invoke of Service: " + invoke.getName());
    String jsonHeader = AgnelottiSchema.getInstance().decodeJson(writes);
    Reader reader = new InputStreamReader(new ByteArrayInputStream(jsonHeader.getBytes()));
    JsonReader jsonReader = new JsonReader(reader);
    JsonObject jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();
    try {
      if (contract.isVoidMethod()) {
        if (contract.isNoParameters()) {
          invokeNoRetNoParam();
        } else {
          invokeParamNoReturn(jsonObject);
        }
      } else {
        if (contract.isVoidMethod()) {
          invokeReturnNoParam(sessionDescriptorVar);
        } else {
          invokeRetParam(jsonObject, sessionDescriptorVar);
        }
      }
    } catch (NullPointerException | JsonSyntaxException e) {
      logger.error("Incorrect invocation caused an NPE");
      logger.error("JSON involved : " + jsonHeader);
      logger.error("Exception stacktrace ", e);
    }
  }

  @Override
  public GelatoResourceController getResourceController() {
    return this;
  }

  @Override
  public void setResourceController(GelatoResourceController resourceController) {
  }

  @Override
  public boolean readRequest(
      RequestConnection connection,
      GelatoFileDescriptor clientFileDescriptor,
      long offset,
      int numberOfBytes) {
    Map<String, Object> sessionVar = getSessionMap(connection.getSession(), clientFileDescriptor);
    StatStruct sessionStat = getResource(sessionVar);
    if (sessionStat == null) {
      connection
          .getResourceController()
          .sendErrorMessage(connection, "Cannot READ Empty RPC Stream");
      return true;
    }
    if (offset != 0 || numberOfBytes != sessionStat.getLength()) {
      connection
          .getResourceController()
          .sendErrorMessage(connection, "RPC Operations must be atomic");
      return true;
    }

    if (sessionStat.getLength() == 0 || getReturnData(sessionVar).length == 0) {
      connection.getResourceController().sendErrorMessage(connection, "RPC Illegal Read");
      return true;
    }

    byte[] returnData = getReturnData(sessionVar);
    int ptr = 0;

    while (ptr < returnData.length) {
      int copyByte = numberOfBytes - ptr;
      if (copyByte > P9Protocol.MAX_MSG_CONTENT_SIZE) {
        copyByte = P9Protocol.MAX_MSG_CONTENT_SIZE;
      }
      ReadResponse readResponse = new ReadResponse();
      readResponse.setData(Arrays.copyOf(returnData, copyByte));
      connection.reply(readResponse);
      ptr += copyByte;
    }
    // Reset the session free up memory
    resetSession(sessionVar);
    return true;
  }

  @Override
  public boolean statRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {

    GelatoSession clientSession = connection.getSession();
    String desriptorId = descriptoString(clientFileDescriptor);
    StatStruct selfStat;
    Map<String, Object> sessionDescriptorVar = getSessionMap(clientSession, clientFileDescriptor);
    if (sessionDescriptorVar == null) {
      selfStat = getStat();
    } else {
      selfStat = getResource(sessionDescriptorVar);
    }
    StatResponse response = new StatResponse();
    response.setStatStruct(selfStat);
    connection.reply(response);
    return true;
  }

  @Override
  public boolean writeRequest(
      RequestConnection connection,
      GelatoFileDescriptor clientFileDescriptor,
      long offset,
      byte[] data) {
    GelatoSession clientSession = connection.getSession();
    String desriptorId = descriptoString(clientFileDescriptor);
    Map<String, Object> sessionDescriptorVar = getSessionMap(clientSession, desriptorId);
    int mode = getMode(sessionDescriptorVar);
    if (mode != P9Protocol.OPEN_MODE_OWRITE) {
      connection
          .getResourceController()
          .sendErrorMessage(
              connection, "This operation is not supported = File is closed to Writes");
      return true;
    }
    List<byte[]> writes = getWriteRequestData(sessionDescriptorVar);
    // Dont bother adding empty data to the list -- keep len at 0
    if (data.length != 0) {
      writes.add(data);
    }
    WriteResponse response = new WriteResponse();
    response.setBytesWritten(data.length);
    connection.reply(response);

    return true;
  }

  @Override
  public boolean openRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, byte mode) {

    GelatoSession clientSession = connection.getSession();
    String desriptorId = descriptoString(clientFileDescriptor);
    if (clientSession.getSessionVar(desriptorId) == null) {
      Map<String, Object> sessionDescriptorVar = sessionVar();
      clientSession.setSessionVar(desriptorId, sessionDescriptorVar);
    }

    Map<String, Object> sessionDescriptorVar = getSessionMap(clientSession, desriptorId);
    if (getMode(sessionDescriptorVar) == P9Protocol.OPEN_MODE_ORCLOSE) {
      if (mode == P9Protocol.OPEN_MODE_OREAD || mode == P9Protocol.OPEN_MODE_OWRITE) {
        if (mode == P9Protocol.OPEN_MODE_OWRITE) {
          resetSession(sessionDescriptorVar);
        }
        OpenResponse response = new OpenResponse();
        response.setFileQID(getQID());
        connection.reply(response);
        putMode(sessionDescriptorVar, mode);
        return true;
      }
    } else {
      connection
          .getResourceController()
          .sendErrorMessage(connection, "This operation is not supported = File already Open");
      return true;
    }
    connection
        .getResourceController()
        .sendErrorMessage(connection, "This operation is not supported = Unhandled");
    return true;
  }

  @Override
  public boolean closeRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
    GelatoSession clientSession = connection.getSession();
    Map<String, Object> sessionDescriptorVar = getSessionMap(clientSession, clientFileDescriptor);
    if (getMode(sessionDescriptorVar) == P9Protocol.OPEN_MODE_OWRITE) {
      invokeServiceMethod(getWriteRequestData(sessionDescriptorVar), sessionDescriptorVar);
    }
    putMode(sessionDescriptorVar, P9Protocol.OPEN_MODE_ORCLOSE);
    CloseResponse closeResponse = new CloseResponse();
    connection.reply(closeResponse);
    return true;
  }

  private void putReturnData(Map<String, Object> session, byte[] retData) {
    session.put(RETURN_BYTE, retData);
  }

  private byte[] getReturnData(Map<String, Object> session) {
    return (byte[]) session.get(RETURN_BYTE);
  }

  private void resetSession(Map<String, Object> session) {
    session.clear();
    StatStruct sessionStat = getStat().duplicate();
    putMode(session, P9Protocol.OPEN_MODE_ORCLOSE);
    putResource(session, sessionStat);
    putWrite(session, new ArrayList<byte[]>());
  }

  private void putResource(Map<String, Object> session, StatStruct resource) {
    session.put(RESOURCE, resource);
  }

  private void putWrite(Map<String, Object> session, List<byte[]> byteBuffer) {
    session.put(WRITE_BYTES, byteBuffer);
  }

  private StatStruct getResource(Map<String, Object> session) {
    return (StatStruct) session.get(RESOURCE);
  }

  private Map<String, Object> sessionVar() {
    Map<String, Object> sess = new ConcurrentHashMap<>();
    putMode(sess, P9Protocol.OPEN_MODE_ORCLOSE);
    return sess;
  }

  private List<byte[]> getWriteRequestData(Map<String, Object> sessionMap) {
    return (List) sessionMap.get(WRITE_BYTES);
  }

  private String descriptoString(GelatoFileDescriptor descriptorId) {
    return Long.toString(descriptorId.getDescriptorId());
  }

  private Map<String, Object> getSessionMap(
      GelatoSession clientSession, GelatoFileDescriptor descriptorId) {
    String desriptorIdStr = Long.toString(descriptorId.getDescriptorId());
    return getSessionMap(clientSession, desriptorIdStr);
  }

  private Map<String, Object> getSessionMap(GelatoSession clientSession, String descriptorId) {
    return (Map) clientSession.getSessionVar(descriptorId);
  }

  private int getMode(Map<String, Object> sessionMap) {
    return (int) sessionMap.get(MODE);
  }

  private void putMode(Map<String, Object> sessionMap, int mode) {
    sessionMap.put(MODE, mode);
  }
}
