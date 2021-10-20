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

package ciotola.actor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class MethodRunner implements RunnableScript {

  class MethodStat {
    public Method callingMethod;
    public boolean hasRet;
    public boolean hasParam;

    public MethodStat(Method method) {
      callingMethod = method;
      if (method.getParameterCount() != 0) {
        hasParam = true;
      }
      if (!method.getReturnType().getName().equals("void")) {
        hasRet = true;
      }
      callingMethod = method;
    }

  }

  private RoleImpl role;
  private Object host;
  private Map<String, MethodStat> methodMap = new ConcurrentHashMap<>();


  public MethodRunner(Object host, Method targetMethod, RoleImpl role) {
    this.host = host;
    this.role = role;
    methodMap.put(targetMethod.getName(),new MethodStat(targetMethod));
  }


  public void addMethod(String methodName, Method method) {
    methodMap.put(methodName,new MethodStat(method));
  }

  public RoleImpl getRole() {
    return role;
  }

  @Override
  public Object process(Object message) {
    try {
      Object [] messages = ((MethodParamStack)message).parameters;
      String methodName = ((MethodParamStack)message).methodName;
      MethodStat methodStat = methodMap.get(methodName);
      Object retVal;
      if(methodStat.hasParam) {
         retVal = methodStat.callingMethod.invoke(host,messages);
      } else {
        retVal = methodStat.callingMethod.invoke(host);
      }
      return retVal;
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new ActorException(e);
    }
  }

  @Override
  public boolean hasReturn() {
    return true;
  }

  @Override
  public boolean hasValues() {
    return true;
  }
}