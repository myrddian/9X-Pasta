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

package ciotola.implementation;

import ciotola.Ciotola;
import ciotola.CiotolaContext;
import ciotola.CiotolaServiceInterface;
import ciotola.annotations.CiotolaAutowire;
import ciotola.annotations.CiotolaService;
import ciotola.pools.CiotolaConnectionPool;
import ciotola.pools.CiotolaConnectionService;
import ciotola.pools.CiotolaKeyPool;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultCiotolaContainer implements CiotolaContext {

  private final Logger logger = LoggerFactory.getLogger(DefaultCiotolaContainer.class);

  private ExecutorService executorService = Executors.newCachedThreadPool();
  private Map<String, CiotolaServiceInterface> serviceInterfaceMap = new HashMap<>();
  private Map<String, Class> candidateService = new ConcurrentHashMap<>();
  private Map<String, Object> autoWires = new ConcurrentHashMap<>();
  private Map<String, Logger> loggerMap = new ConcurrentHashMap<>();
  private List<String> loadedJars = new ArrayList<>();
  private List<String> scanAnnotations = new ArrayList<>();
  private List<PooledServiceRunner> serviceRunners = new ArrayList<>();
  private CiotolaKeyPool keyPoolExecutor = new CiotolaKeyPool();
  private CiotolaConnectionPool connectionPool = new CiotolaConnectionPool();

  public DefaultCiotolaContainer() {
    resetDefaults();
  }

  @Override
  public void addAnnotation(Class annotation) {
    scanAnnotations.add(annotation.getName());
  }

  @Override
  public List<String> getAnnotations() {
    return scanAnnotations;
  }

  @Override
  public List<String> getLoadedJars() {
    return loadedJars;
  }

  @Override
  public void submitJob(Runnable job) {
    executorService.submit(job);
  }

  @Override
  public Collection<CiotolaServiceInterface> getServices() {
    return serviceInterfaceMap.values();
  }

  @Override
  public void addService(CiotolaServiceInterface newService) {
    serviceInterfaceMap.put(newService.serviceName(), newService);
  }

  @Override
  public void addService(Object newService) {
    AnnotatedJavaServiceRunner newRunner = new AnnotatedJavaServiceRunner(newService);
    serviceInterfaceMap.put(newRunner.serviceName(), newRunner);
  }

  @Override
  public void injectService(CiotolaServiceInterface newService, boolean skipInjection) {

    int svcCounter = serviceRunners.size() + 1;
    PooledServiceRunner runner = new PooledServiceRunner(newService, svcCounter);

    if(!skipInjection) {
      serviceInterfaceMap.put(newService.serviceName(), newService);
      processInjection(newService.serviceName());
    } else {
      logger.debug("No injection for " + newService.serviceName() + " Mapped to ID [ " + Integer.toString(svcCounter) + " ] ");
    }
    runner.start();
    executorService.execute(runner);
    serviceRunners.add(runner);
  }

  @Override
  public void injectService(CiotolaServiceInterface newService) {
    injectService(newService,false);
  }

  @Override
  public void injectService(Object newService) {
    injectService(newService, false);
  }

  @Override
  public void injectDependencies(Object newService) {
    processInjection(newService);
  }

  @Override
  public void injectService(Object newService, boolean skipInjection) {
    AnnotatedJavaServiceRunner runner = new AnnotatedJavaServiceRunner(newService);
    injectService(runner, skipInjection);
  }

  @Override
  public ExecutorService getExecutorService() {
    return executorService;
  }

  @Override
  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public int threadCapacity() {
    int capacity = Runtime.getRuntime().availableProcessors() / 2;
    if(capacity < 1) {
      return  1;
    }
    return capacity;
  }

  @Override
  public void execute(Runnable job, long key) {
    keyPoolExecutor.addJob(job,key);
  }

  @Override
  public void execute(CiotolaConnectionService connectionService) {
    connectionPool.addConnection(connectionService);
  }

  @Override
  public boolean startContainer() {
    // Assumption we are embedded some how - dont do additional parsing
    logger.debug("Ciotola Container - Running");
    try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {

      ClassInfoList classInfos = scanResult.getAllClasses();
      for (ClassInfo info : classInfos) {
        for (ClassInfo annotation : info.getAnnotations()) {
          if (scanAnnotations.contains(annotation.getName())) {
            if (annotation.getName().equals(CiotolaService.class.getName())) {
              candidateService.put(info.getName(), info.loadClass());
            } else {
              autoWires.put(info.getName(), info.loadClass());
            }
          }
        }
      }
    }
    if (!processServices()) {
      return false;
    }
    if (!processDependencies()) {
      return false;
    }
    return startServices();
  }

  private boolean startServices() {
    int counter = 0;
    for (String key : serviceInterfaceMap.keySet()) {
      PooledServiceRunner runner = new PooledServiceRunner(serviceInterfaceMap.get(key), counter);
      runner.start();
      executorService.execute(runner);
      ++counter;
      serviceRunners.add(runner);
    }
    return true;
  }

  private boolean processServices() {
    for (String cService : candidateService.keySet()) {
      logger.debug("Creating Service " + cService);
      try {
        Constructor constructor = candidateService.get(cService).getConstructor();
        Object newServiceInstance = constructor.newInstance();
        AnnotatedJavaServiceRunner runner = new AnnotatedJavaServiceRunner(newServiceInstance);
        serviceInterfaceMap.put(cService, runner);
      } catch (InstantiationException
          | IllegalAccessException
          | InvocationTargetException
          | NoSuchMethodException e) {
        logger.error(Ciotola.CLASS_LOADING_ERROR, e);
        return false;
      }
    }
    return true;
  }

  private boolean processInjection(Object wiringComponent) {
    for (Field objField : wiringComponent.getClass().getDeclaredFields()) {
      objField.setAccessible(true);
      for (Annotation annotation : objField.getDeclaredAnnotations()) {
        if (annotation instanceof CiotolaAutowire) {
          Object autoWrite = serviceInterfaceMap.get(objField.getType().getName());
          if (autoWrite == null) {
            if (objField.getType() == CiotolaContext.class) {
              autoWrite = this;
            } else {
              logger.error(Ciotola.UNKNOWN_COMPONENT_SPECIFIED);
            }
          }
          if (autoWrite.getClass().getName().equals(AnnotatedJavaServiceRunner.class.getName())) {
            AnnotatedJavaServiceRunner runner = (AnnotatedJavaServiceRunner) autoWrite;
            autoWrite = runner.getObject();
          }
          logger.debug(
                  "Autowiring: "
                          + wiringComponent.getClass().getName()
                          + " ( "
                          + objField.getName()
                          + " , "
                          + autoWrite.getClass().getName()
                          + " )");
          try {
            objField.set(wiringComponent, autoWrite);
          } catch (IllegalAccessException e) {
            logger.error(Ciotola.CLASS_LOADING_ERROR, e);
            return false;
          }
        }
      }
    }
    return true;
  }

  private boolean processInjection(String serviceClass) {
    Object wiringComponent = serviceInterfaceMap.get(serviceClass).getObject();
    return processInjection(wiringComponent);
  }

  private boolean processDependencies() {
    logger.debug("Injecting Dependencies");
    // Scan Components and autowire the requested attributes

    for (String service : serviceInterfaceMap.keySet()) {
      if (processInjection(service) == false) {
        logger.error(Ciotola.UNKNOWN_COMPONENT_SPECIFIED);
        return false;
      }
    }
    return true;
  }

  private void resetDefaults() {
    scanAnnotations.clear();
    scanAnnotations.add(CiotolaAutowire.class.getName());
    scanAnnotations.add(CiotolaService.class.getName());
    connectionPool.setIdleTimeout(240);
  }
}
