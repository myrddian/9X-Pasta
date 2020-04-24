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

package agnolotti.server;

import agnolotti.Agnolotti;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.ByteEncoder;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;
import protocol.messages.response.CloseResponse;
import protocol.messages.response.OpenResponse;
import protocol.messages.response.StatResponse;
import protocol.messages.response.WriteResponse;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteMethodStrategy extends GelatoResourceControllerImpl implements GelatoFileController,
        WriteRequestHandler, ReadRequestHandler, StatRequestHandler, OpenRequestHandler, CloseRequestHandler
{

    private Method invoke;
    private Object service;
    private boolean noReturn = false;
    private boolean noParameters= false;
    private final Logger logger = LoggerFactory.getLogger(RemoteMethodStrategy.class);
    private boolean hasReturnType() {
        return !invoke.getReturnType().isInstance(void.class);
    }
    private Gson gson = new Gson();
    private Map<String, Object> jsonFieldMap;


    public RemoteMethodStrategy(Method method, Object service, long id) {
        super();

        invoke = method;
        this.service = service;
        setOpenRequestHandler(this);
        setReadRequestHandler(this);
        setWriteRequestHandler(this);
        setStatRequestHandler(this);
        setCloseRequestHandler(this);
        getResource().getStat().setName(methodDecorator());
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
    }

    public String methodDecorator() {
        return RemoteServiceFactory.getMethodDecorator(invoke);
    }

    public Object callMethod(Object [] args) throws Throwable {
        return invoke.invoke(service,args);
    }

    public Method getMethod() {
        return invoke;
    }


    public Map<String, Object> getJsonContract() {
        jsonFieldMap = new HashMap<>();
        List arrayList = new ArrayList();
        jsonFieldMap.put(Agnolotti.PARAMETERS, arrayList);
        jsonFieldMap.put(Agnolotti.RETURN_FIELD, invoke.getReturnType().getName());
        int fieldPosition = 0;
        for(Parameter param: invoke.getParameters()) {
            Map<String, Object> methodsMap = new HashMap<>();
            methodsMap.put(Agnolotti.PARAMETER_NAME, param.getName());
            methodsMap.put(Agnolotti.PARAMETER_TYPE, param.getType().getName());
            methodsMap.put(Agnolotti.PARAMETER_POS, fieldPosition);
            arrayList.add(methodsMap);
            ++fieldPosition;
        }

        if(invoke.getReturnType().getName().equals(Agnolotti.PARAMETER_VOID)) {
            noReturn = true;
        }

        if(arrayList.isEmpty()) {
            noParameters = true;
        }

        return jsonFieldMap;
    }

    private void invokeParamNoReturn(JsonObject marshalledObject) {
        logger.debug("Invoking Parameter-No-Return Strategy");
        JsonArray parameters = marshalledObject.getAsJsonArray(Agnolotti.PARAMETERS);
        Object[] invokeParameters = invoke.getParameters();
        Object[] methodParameter = new Object[invokeParameters.length];
        List arrayList = (List) jsonFieldMap.get(Agnolotti.PARAMETERS);
        try {

            for(int i=0; i < invokeParameters.length; ++i) {
                JsonElement parameter = parameters.get(i);
                JsonObject objectParamerter = parameter.getAsJsonObject();
                int location = objectParamerter.get(Agnolotti.PARAMETER_POS).getAsInt();
                String value =  objectParamerter.get(Agnolotti.PARAMETER_VALUE).getAsString();
                Map<String, Object> methodsMap = (Map) arrayList.get(i);
                Object objectValue = gson.fromJson(value,Class.forName((String)methodsMap.get(Agnolotti.PARAMETER_TYPE)));
                methodParameter[i] = objectValue;
            }

            invoke.invoke(service,methodParameter);
        } catch (IllegalAccessException e) {
            logger.error("Method Access is illegal ", e);
        } catch (InvocationTargetException e) {
            logger.error("Cannot invoke target method",e);
        } catch (ClassNotFoundException e) {
            logger.error("Cannot Find Class Initialiser",e);
        }

    }


    private void invokeReturnNoParam() {
        logger.debug("Invoking No-Parameter-Return Strategy");

    }


    private void invokeNoRetNoParam() {
        logger.debug("Invoking No-Parameter-No-Return Strategy");
        try {
            invoke.invoke(service,null);
        } catch (IllegalAccessException e) {
            logger.error("Method Access is illegal ", e);
        } catch (InvocationTargetException e) {
            logger.error("Cannot invoke target method",e);
        }
    }

    private void invokeRetParam() {
        logger.debug("Invoking Parameter-Return Strategy");

    }

    private String decodeJson(List<byte[]> byteArray) {
        int byteArraySize = 0;
        for(byte[] item: byteArray) {
            byteArraySize += item.length;
        }
        byte[] buffer = new byte[byteArraySize];
        int ptr=0;
        for(byte[] section: byteArray) {
            ByteEncoder.copyBytesTo(section,buffer,ptr,section.length);
            ptr+= section.length;
        }

        return new String(buffer);
    }

    private void invokeServiceMethod(List<byte[]> writes) {
        logger.debug("Remote Invoke of Service: " + invoke.getName());
        String jsonHeader = decodeJson(writes);
        Reader reader = new InputStreamReader(new ByteArrayInputStream(jsonHeader.getBytes()));
        JsonReader jsonReader = new JsonReader(reader);
        JsonObject jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();
        if(noReturn) {
            if(noParameters) {
                invokeNoRetNoParam();
            } else  {
                invokeParamNoReturn(jsonObject);
            }
        } else {
            if(noParameters) {
                invokeReturnNoParam();
            } else  {
                invokeRetParam();
            }
        }
    }

    @Override
    public void setResourceController(GelatoResourceController resourceController) {}

    @Override
    public GelatoResourceController getResource() {
        return this;
    }

    @Override
    public boolean readRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, long offset, int numberOfBytes) {
        return false;
    }

    @Override
    public boolean statRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {

        GelatoSession clientSession = connection.getSession();
        String desriptorId = Long.toString(clientFileDescriptor.getDescriptorId());
        StatStruct selfStat;
        Map<String, Object> sessionDescriptorVar = (Map) clientSession.getSessionVar(desriptorId);
        if(sessionDescriptorVar == null || sessionDescriptorVar.get(desriptorId) == null ) {
            selfStat = getStat();
        } else  {
            selfStat = (StatStruct)sessionDescriptorVar.get(desriptorId);
        }
        StatResponse response = new StatResponse();
        response.setStatStruct(selfStat);
        connection.reply(response);
        return true;
    }

    @Override
    public boolean writeRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, long offset, byte[] data) {
        GelatoSession clientSession = connection.getSession();
        String desriptorId = Long.toString(clientFileDescriptor.getDescriptorId());
        Map<String, Object> sessionDescriptorVar = (Map) clientSession.getSessionVar(desriptorId);
        int mode = (int) sessionDescriptorVar.get("mode");
        if(mode != P9Protocol.OPEN_MODE_OWRITE) {
            connection.getResourceController()
                    .sendErrorMessage(connection, "This operation is not supported = File is closed to Writes");
            return true;
        }
        List<byte[]> writes = (List) sessionDescriptorVar.get("writes");
        //Dont bother adding empty data to the list -- keep len at 0
        if(data.length !=0) {
            writes.add(data);
        }
        WriteResponse response = new WriteResponse();
        response.setBytesWritten(data.length);
        connection.reply(response);

        return true;
    }

    @Override
    public boolean openRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, byte mode) {

        GelatoSession clientSession = connection.getSession();
        String desriptorId = Long.toString(clientFileDescriptor.getDescriptorId());
        if(mode == P9Protocol.OPEN_MODE_OWRITE || clientSession.getSessionVar(desriptorId) == null) {
            StatStruct sessionStat = getStat().duplicate();
            Map<String, Object> sessionDescriptorVar = sessionVar();
            sessionDescriptorVar.put("resource",sessionStat);
            sessionDescriptorVar.put("mode", P9Protocol.OPEN_MODE_ORCLOSE);
            clientSession.setSessionVar(desriptorId,sessionDescriptorVar);
        }

        Map<String, Object> sessionDescriptorVar = (Map) clientSession.getSessionVar(desriptorId);
        if((int)sessionDescriptorVar.get("mode") == P9Protocol.OPEN_MODE_ORCLOSE ) {
            if (mode == P9Protocol.OPEN_MODE_OREAD || mode == P9Protocol.OPEN_MODE_OWRITE) {
                sessionDescriptorVar.clear();
                OpenResponse response = new OpenResponse();
                response.setFileQID(getQID());
                connection.reply(response);
                sessionDescriptorVar.put("mode",(int)mode);
                sessionDescriptorVar.put("writes", new ArrayList<byte[]>());
                return true;
            }
        } else {
            connection.getResourceController()
                    .sendErrorMessage(connection, "This operation is not supported = File already Open");
            return true;
        }
        connection.getResourceController()
                .sendErrorMessage(connection, "This operation is not supported = Unhandled");
        return true;
    }

    @Override
    public boolean closeRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
        GelatoSession clientSession = connection.getSession();
        String desriptorId = Long.toString(clientFileDescriptor.getDescriptorId());
        Map<String, Object> sessionDescriptorVar = (Map) clientSession.getSessionVar(desriptorId);
        if((int)sessionDescriptorVar.get("mode") == P9Protocol.OPEN_MODE_OWRITE ) {
            invokeServiceMethod((List)sessionDescriptorVar.get("writes"));
        }
        sessionDescriptorVar.put("mode",P9Protocol.OPEN_MODE_ORCLOSE);
        CloseResponse closeResponse = new CloseResponse();
        connection.reply(closeResponse);
        return true;
    }

    private Map<String, Object> sessionVar() {
        return new HashMap<>();
    }
}
