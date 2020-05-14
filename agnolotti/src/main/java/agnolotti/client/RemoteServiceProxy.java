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

import agnolotti.server.RemoteServiceFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteServiceProxy  implements InvocationHandler {

    Map<String, RemoteServiceMethod> methodMap = new ConcurrentHashMap<>();

    public RemoteServiceProxy(List<RemoteServiceMethod> methods) {
        for(RemoteServiceMethod method: methods) {
            methodMap.put(method.getMethodName(), method);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodDecorator = RemoteServiceFactory.getMethodDecorator(method);
        if(!methodMap.containsKey(methodDecorator)) {
            throw new RuntimeException("Invalid Function " + methodDecorator);
        }
        RemoteServiceMethod serviceMethod = methodMap.get(methodDecorator);
        return serviceMethod.invoke(args);
    }
}
