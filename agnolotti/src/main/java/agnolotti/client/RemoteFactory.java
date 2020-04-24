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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import gelato.client.file.GelatoDirectory;
import gelato.client.file.GelatoFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class RemoteFactory {

    private GelatoDirectory serviceName;
    private GelatoDirectory serviceVersion;


    public RemoteFactory(String service,
                         String serviceVersion,
                         GelatoDirectory rootDirectory) {

        this.serviceName = rootDirectory.getDirectory(service);
        this.serviceVersion = serviceName.getDirectory(serviceVersion);

    }

    public Object generateRemoteService(Class remoteInterface) {
        GelatoDirectory service = serviceVersion.getDirectory(remoteInterface.getName());
        if(service == null) {
            return null;
        }

        GelatoFile idl = service.getFile(Agnolotti.IDL);
        InputStream fileStream = idl.getFileInputStream();
        Reader reader = new InputStreamReader(fileStream);
        JsonReader jsonReader = new JsonReader(reader);
        JsonObject jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();

        String serviceName = jsonObject.get(Agnolotti.SERVICE_NAME).getAsString();
        JsonObject methods  = jsonObject.get(Agnolotti.METHOD_FIELD).getAsJsonObject();

        if(!serviceName.equals(remoteInterface.getName())) {
            throw new RuntimeException("Not able to bind service");
        }
        Set<String> methodNames = methods.keySet();
        List<RemoteServiceMethod> methodList = new ArrayList<>();
        for(String methodName: methodNames) {
            GelatoFile methodFile = service.getFile(methodName);
            JsonObject methodIdl = methods.getAsJsonObject(methodName);
            methodList.add(new RemoteServiceMethod(methodFile, methodIdl));
        }

        RemoteServiceProxy serviceProxy = new RemoteServiceProxy(methodList);

        Object proxyInstance = Proxy.newProxyInstance(RemoteFactory.class.getClassLoader(),
                new Class[] {remoteInterface}, serviceProxy);

        return  proxyInstance;

    }


}
