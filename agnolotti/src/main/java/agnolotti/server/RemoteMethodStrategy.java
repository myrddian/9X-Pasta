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

import com.google.gson.Gson;
import gelato.GelatoFileDescriptor;
import gelato.server.manager.RequestConnection;
import gelato.server.manager.controllers.GelatoFileController;
import gelato.server.manager.controllers.GelatoResourceController;
import gelato.server.manager.controllers.impl.GelatoResourceControllerImpl;
import gelato.server.manager.processchain.OpenRequestHandler;
import gelato.server.manager.processchain.ReadRequestHandler;
import gelato.server.manager.processchain.StatRequestHandler;
import gelato.server.manager.processchain.WriteRequestHandler;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;
import protocol.messages.response.StatResponse;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class RemoteMethodStrategy extends GelatoResourceControllerImpl implements GelatoFileController,
        WriteRequestHandler, ReadRequestHandler, StatRequestHandler, OpenRequestHandler
{

    private Method invoke;
    private Object service;
    private Gson gson = new Gson();

    private boolean hasReturnType() {
        return !invoke.getReturnType().isInstance(void.class);
    }

    public RemoteMethodStrategy(Method method, Object service, long id) {
        super();

        invoke = method;
        this.service = service;
        setOpenRequestHandler(this);
        setReadRequestHandler(this);
        setWriteRequestHandler(this);
        setStatRequestHandler(this);
        getResource().getStat().setName(methodDecorator());
        StatStruct newStat = getStat();
        newStat.setAccessTime(Instant.now().getEpochSecond());
        newStat.setModifiedTime(newStat.getAccessTime());
        newStat.setUid(ServiceManager.DEFAULT_NAME);
        newStat.setGid(ServiceManager.DEFAULT_NAME);
        newStat.setMuid(ServiceManager.DEFAULT_NAME);
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
        Map<String, Object> jsonFieldMap = new HashMap<>();
        Map<String, Object> methodsMap = new HashMap<>();
        jsonFieldMap.put("parameters", methodsMap);
        jsonFieldMap.put("returns", invoke.getReturnType().getName());
        for(Parameter param: invoke.getParameters()) {
            methodsMap.put(param.getType().getName(), param.getName());
        }
        return jsonFieldMap;
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
        StatStruct selfStat = getStat();
        selfStat.setLength(0);
        StatResponse response = new StatResponse();
        response.setStatStruct(selfStat);
        connection.reply(response);
        return true;
    }

    @Override
    public boolean writeRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, long offset, byte[] data) {
        return false;
    }

    @Override
    public boolean openRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, byte mode) {
        return false;
    }
}
