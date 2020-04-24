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

import gelato.GelatoDescriptorManager;
import gelato.server.GelatoServerManager;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class RemoteServiceFactory {

    private GelatoDescriptorManager descriptorManager;
    private GelatoServerManager manager;

    public RemoteServiceFactory(GelatoDescriptorManager gelatoDescriptorManager,
                                GelatoServerManager serverManager) {
        descriptorManager = gelatoDescriptorManager;
        manager = serverManager;
    }

    private Map<String, RemoteMethodStrategy> pickStrategies(Method [] methods, Object service) {
        Map<String, RemoteMethodStrategy> retVa = new HashMap<>();
        for(Method method:methods) {
            RemoteMethodStrategy strategy = new RemoteMethodStrategy(method, service, descriptorManager.generateDescriptor().getDescriptorId());
            retVa.put(strategy.methodDecorator(), strategy);
        }
        return retVa;
    }

    public static String getMethodDecorator(Method method) {
        int count = method.getParameterCount();
        String methodTypes = "@"+method.getName();
        for(Parameter parameter:method.getParameters()) {
            methodTypes = methodTypes+"$"+parameter.getName()+"-"+parameter.getType();
        }
        return methodTypes+"#count:"+Integer.toString(count);
    }

    public RemoteServiceProxyDirectory generateRpc(Class remoteInterface, Object service) {

        if(!remoteInterface.isInterface()) {
            throw new RuntimeException("Must be an Interace");
        }

        if(!remoteInterface.isInstance(service)) {
            throw new RuntimeException("Service must implement Interface");
        }
        ServiceProxySerialiser proxy = new ServiceProxySerialiser(service);
        Object proxyInstance = Proxy.newProxyInstance(RemoteServiceFactory.class.getClassLoader(),
                new Class[] {remoteInterface}, proxy);
        String objectName = remoteInterface.getName();
        Method [] methodList = remoteInterface.getDeclaredMethods();
        Map<String, RemoteMethodStrategy>  strategyMap = pickStrategies(methodList, proxyInstance);
        RemoteServiceProxyDirectory retVal =  new RemoteServiceProxyDirectory(strategyMap, objectName,
                descriptorManager.generateDescriptor().getDescriptorId(), manager);
        return retVal;
    }


}
