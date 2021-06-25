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

package ciotola.implementation;

import ciotola.Ciotola;
import ciotola.CiotolaServiceInterface;
import ciotola.annotations.CiotolaScriptMethod;
import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotatedJavaServiceRunner implements CiotolaServiceInterface {

  private final Logger logger = LoggerFactory.getLogger(AnnotatedJavaServiceRunner.class);
  private Object javaServiceObject;
  private Method startMethod;
  private Method stopMethod;
  private Method runMethod;

  public AnnotatedJavaServiceRunner(Object serviceObject) {

    boolean foundStart = false;
    boolean foundStop = false;
    boolean foundRun = false;

    javaServiceObject = serviceObject;

    logger.debug("Loading Service: " + serviceObject.getClass().getName());

    // Scan Start
    for (Method method : javaServiceObject.getClass().getMethods()) {
      CiotolaServiceStart startAnnotation = method.getAnnotation(CiotolaServiceStart.class);
      if (startAnnotation != null) {
        startMethod = method;
        foundStart = true;
        break;
      }
    }

    // Scan Stop
    for (Method method : javaServiceObject.getClass().getMethods()) {
      CiotolaServiceStop shutdownAnnotation = method.getAnnotation(CiotolaServiceStop.class);
      if (shutdownAnnotation != null) {
        stopMethod = method;
        foundStop = true;
        break;
      }
    }

    // Scan run
    for (Method method : javaServiceObject.getClass().getMethods()) {
      CiotolaServiceRun runAnnotation = method.getAnnotation(CiotolaServiceRun.class);
      if (runAnnotation != null) {
        runMethod = method;
        foundRun = true;
        break;
      }
    }

    if (!foundRun || !foundStop || !foundStart) {
      logger.error(Ciotola.SERVICE_INIT_METHOD_ERROR);
      throw new RuntimeException(Ciotola.SERVICE_INIT_METHOD_ERROR);
    }
    logger.debug("Class " + serviceObject.getClass().getName() + " Loaded into Proxy");
  }

  @Override
  public boolean startUp() {
    try {
      startMethod.invoke(javaServiceObject);
      return true;
    } catch (Exception e) {
      logger.error(Ciotola.GENERIC_SERVICE_START_ERROR, e);
    }
    return false;
  }

  @Override
  public boolean shutdown() {
    try {
      stopMethod.invoke(javaServiceObject);
      return true;
    } catch (Exception e) {
      logger.error(Ciotola.GENERIC_SERVICE_START_ERROR, e);
    }
    return false;
  }

  @Override
  @CiotolaScriptMethod
  public boolean run() {
    try {
      runMethod.invoke(javaServiceObject);
      return true;
    } catch (InvocationTargetException e) {
      logger.error(Ciotola.GENERIC_SERVICE_START_ERROR, e.getCause());
    } catch (Exception ex) {
      logger.error(Ciotola.GENERIC_SERVICE_START_ERROR, ex.getCause());
    }
    return false;
  }

  @Override
  public String serviceName() {
    return javaServiceObject.getClass().getName();
  }

  @Override
  public Object getObject() {
    return javaServiceObject;
  }
}
