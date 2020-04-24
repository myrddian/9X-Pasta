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

package agnolotti.client;
import agnolotti.Agnolotti;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gelato.client.file.GelatoFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteServiceMethod {

    private GelatoFile remoteMethodFile;
    private JsonObject idl;
    private boolean noReturn = false;
    private boolean noParameters= false;
    private final Logger logger = LoggerFactory.getLogger(RemoteServiceMethod.class);
    private Gson gson = new Gson();

    public RemoteServiceMethod(GelatoFile file, JsonObject methodIdl) {
        remoteMethodFile = file;
        idl = methodIdl;
        if(idl.getAsJsonPrimitive(Agnolotti.RETURN_FIELD).getAsString().equals(Agnolotti.PARAMETER_VOID)) {
            noReturn = true;
        }
        JsonArray methodParameters = idl.getAsJsonArray(Agnolotti.PARAMETERS);
        if(methodParameters.size() == 0 ) {
            noParameters = true;
        }

    }

    private Map<String, Object> generateRequest() {
        Map<String,Object> topLevel = new HashMap<>();
        topLevel.put(Agnolotti.PARAMETER_NAME, getMethodName());
        topLevel.put(Agnolotti.PARAMETERS, new ArrayList<>());
        return topLevel;
    }

    public String getMethodName() {
        return remoteMethodFile.getName();
    }

    private Object invokeParamNoReturn(Object [] parameters) {
        logger.debug("Invoking Parameter-No-Return Strategy");
        Map<String,Object> invocationMap = generateRequest();

        JsonArray parameterArray = idl.getAsJsonArray(Agnolotti.PARAMETERS);
        int number = parameterArray.size();
        if(number != parameters.length) {
            throw new RuntimeException("Invalid Parameter Count");
        }

        List<Object> paramters = (List<Object>)invocationMap.get(Agnolotti.PARAMETERS);
        for(int i=0; i < number; ++i){
            Map<String,Object> param = new HashMap<>();
            param.put(Agnolotti.PARAMETER_POS, i);
            param.put(Agnolotti.PARAMETER_VALUE,parameters[i]);
            paramters.add(param);
        }

        String jsonString = gson.toJson(invocationMap);
        try {
            OutputStream invokeStream = remoteMethodFile.getFileOutputStream();
            invokeStream.write(jsonString.getBytes());
            invokeStream.close();
        } catch (IOException e) {
            logger.error("Error invoking parameters",e);
        }

        return null;
    }


    private Object invokeReturnNoParam() {
        logger.debug("Invoking No-Parameter-Return Strategy");
        return null;
    }


    private Object invokeNoRetNoParam() {
        logger.debug("Invoking No-Parameter-No-Return Strategy");

        Map<String, Object> invocationMap = generateRequest();
        String jsonString = gson.toJson(invocationMap);
        OutputStream invokeStream = remoteMethodFile.getFileOutputStream();
        try {
            byte [] bytesTosend = jsonString.getBytes();
            invokeStream.write(bytesTosend);
            invokeStream.close();
        } catch (IOException e) {
            logger.error("Error Invoking handler exception:",e);
        }
        return null;
    }

    private Object invokeRetParam(Object [] parameters) {
        logger.debug("Invoking Parameter-Return Strategy");
        return null;
    }

    public Object invoke(Object [] parameters) {
        logger.debug("Remote Invoke of Service: " + getMethodName());
        if(noReturn) {
            if(noParameters) {
                return invokeNoRetNoParam();
            } else  {
                return invokeParamNoReturn(parameters);
            }
        } else {
            if(noParameters) {
                return invokeReturnNoParam();
            } else  {
                return invokeRetParam(parameters);
            }
        }
    }

}
