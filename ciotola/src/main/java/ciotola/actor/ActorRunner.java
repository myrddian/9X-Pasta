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

import ciotola.annotations.CiotolaScriptMethod;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class ActorRunner implements RunnableScript {

  private boolean hasParams = false;
  private boolean hasRet = false;
  private Method target;
  private Object host;

  public ActorRunner(Object runner) {

    boolean foundMethod = false;
    Method targetMethod;
    host = runner;
    for (Method method : runner.getClass().getMethods()) {
      CiotolaScriptMethod startAnnotation = method.getAnnotation(CiotolaScriptMethod.class);
      if (startAnnotation != null) {
        targetMethod = method;
        targetMethod.setAccessible(true);
        foundMethod = true;
        processMethod(targetMethod);
      }
    }

    if (!foundMethod) {
      throw new RuntimeException("INVALID - NEEDS ANNOTATION");
    }
  }

  private void processMethod(Method targetMethod) {

    if (targetMethod.getParameterCount() != 0) {
      hasParams = true;
    }

    if (!targetMethod.getReturnType().getName().equals("void")) {
      hasRet = true;
    }

    target = targetMethod;
  }

  @Override
  public Object process(Object message) {
    try {
      if (hasParams && hasRet) {
        return target.invoke(host, message);
      } else if (hasParams) {
        target.invoke(host, message);
      } else if (hasRet) {
        return target.invoke(host);
      } else {
        target.invoke(host);
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public boolean hasReturn() {
    return hasRet;
  }

  @Override
  public boolean hasValues() {
    return hasParams;
  }
}
