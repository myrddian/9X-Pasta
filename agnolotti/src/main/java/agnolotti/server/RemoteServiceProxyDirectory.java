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
import gelato.server.GelatoServerManager;
import gelato.server.manager.controllers.impl.GelatoDirectoryControllerImpl;
import gelato.server.manager.controllers.impl.GelatoFileControllerImpl;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class RemoteServiceProxyDirectory extends GelatoDirectoryControllerImpl {

    private Map<String, RemoteMethodStrategy> methodStrategyMap;
    private String definedIDL = "";
    private Gson gson = new Gson();
    private GelatoServerManager manager;
    private GelatoFileControllerImpl fileIdl;

    private void initRpc() {
        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> methodMap = new HashMap<>();
        jsonMap.put(Agnolotti.SERVICE_NAME,getDirectoryName());
        jsonMap.put(Agnolotti.METHOD_FIELD, methodMap);
        for(RemoteMethodStrategy element: methodStrategyMap.values()) {
            methodMap.put(element.methodDecorator(), element.getJsonContract());
            addFile(element);
            manager.addResource(element);
        }
        definedIDL = gson.toJson(jsonMap);
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(definedIDL.getBytes());
        fileIdl = new GelatoFileControllerImpl(Agnolotti.IDL, arrayInputStream,definedIDL.getBytes().length, manager.getDescriptorManager().generateDescriptor());
        addFile(fileIdl);
        manager.addResource(fileIdl);
    }


    public String getIDL() {
        return definedIDL;
    }

    public RemoteServiceProxyDirectory(Map<String, RemoteMethodStrategy> methodStrategyMap,
                                       String dirName,
                                       long id, GelatoServerManager serverManager) {


        super();
        manager = serverManager;
        this.methodStrategyMap = methodStrategyMap;
        setResourceName(dirName);
        StatStruct newStat = getStat();
        newStat.setAccessTime(Instant.now().getEpochSecond());
        newStat.setModifiedTime(newStat.getAccessTime());
        newStat.setUid(Agnolotti.DEFAULT_NAME);
        newStat.setGid(Agnolotti.DEFAULT_NAME);
        newStat.setMuid(Agnolotti.DEFAULT_NAME);
        QID qid = getQID();
        qid.setType(P9Protocol.QID_DIR);
        qid.setVersion(0);
        qid.setLongFileId(id);
        newStat.setQid(qid);
        newStat.updateSize();
        setQID(qid);
        setStat(newStat);
        initRpc();
    }

}
