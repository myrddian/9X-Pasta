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
import ciotola.CiotolaConnectionService;
import ciotola.CiotolaContext;
import ciotola.CiotolaServiceInterface;
import ciotola.actor.CiotolaDirector;
import ciotola.annotations.CiotolaAutowire;
import ciotola.annotations.CiotolaBean;
import ciotola.annotations.CiotolaService;
import ciotola.pools.CiotolaConnectionPool;
import ciotola.pools.CiotolaKeyPool;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCiotolaContainer implements CiotolaContext {

  private final Logger logger = LoggerFactory.getLogger(DefaultCiotolaContainer.class);

  private ExecutorService executorService = Executors.newCachedThreadPool();
  private Map<String, CiotolaServiceInterface> serviceInterfaceMap = new ConcurrentHashMap<>();
  private Map<String, Class> candidateService = new ConcurrentHashMap<>();
  private Map<String, Object> autoWires = new ConcurrentHashMap<>();
  private Map<String, Object> beans = new ConcurrentHashMap<>();
  private Map<String, Logger> loggerMap = new ConcurrentHashMap<>();
  private List<String> loadedJars = new ArrayList<>();
  private List<String> scanAnnotations = new ArrayList<>();
  private Map<Integer, PooledServiceRunner> serviceRunners = new ConcurrentHashMap<>();
  private CiotolaKeyPool keyPoolExecutor;
  private CiotolaConnectionPool connectionPool;
  private long connectionTimeOut = 240;
  private CiotolaDirector ciotolaDirector;

  public DefaultCiotolaContainer() {
    resetDefaults();
  }

  private void resetDefaults() {

    int physicalCores = getNumberOfCPUCores();

    /*
     Default schema for this is 1/4 Connection
      Min 2
      3/4 For the remaining key pool
      min 2
    */

    // Initialise pools - some default allocations
    int connectionPoolCounter = physicalCores / 4;
    int keyPool = connectionPoolCounter * 3;
    if (connectionPoolCounter <= 1) {
      connectionPoolCounter = 2;
    }
    if (keyPool <= 1) {
      keyPool = 2;
    }
    connectionPool = new CiotolaConnectionPool(connectionPoolCounter);
    keyPoolExecutor = new CiotolaKeyPool(keyPool);
    ciotolaDirector = new CiotolaDirector(physicalCores);

    // Setup the system
    scanAnnotations.clear();
    scanAnnotations.add(CiotolaAutowire.class.getName());
    scanAnnotations.add(CiotolaService.class.getName());
    scanAnnotations.add(CiotolaBean.class.getName());
    addDependency(CiotolaContext.class, this);
    connectionPool.setIdleTimeout(connectionTimeOut);
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
  public void removeService(int serviceId) {
    serviceRunners.remove(serviceId);
  }

  @Override
  public int injectService(CiotolaServiceInterface newService, boolean skipInjection) {

    int svcCounter = serviceRunners.size() + 1;
    PooledServiceRunner runner = new PooledServiceRunner(newService, svcCounter);

    if (!skipInjection) {
      serviceInterfaceMap.put(newService.serviceName(), newService);
      processInjection(newService.serviceName());
    } else {
      logger.debug(
          "No injection for "
              + newService.serviceName()
              + " Mapped to ID [ "
              + Integer.toString(svcCounter)
              + " ] ");
    }
    runner.start();
    executorService.execute(runner);
    serviceRunners.put(svcCounter, runner);
    return svcCounter;
  }

  @Override
  public int injectService(Object newService, boolean skipInjection) {
    AnnotatedJavaServiceRunner runner = new AnnotatedJavaServiceRunner(newService);
    return injectService(runner, skipInjection);
  }

  @Override
  public int injectService(CiotolaServiceInterface newService) {
    return injectService(newService, false);
  }

  @Override
  public int injectService(Object newService) {
    return injectService(newService, false);
  }

  @Override
  public void injectDependencies(Object newService) {
    processInjection(newService);
  }

  @Override
  public void addDependency(Object dependency) {
    beans.put(dependency.getClass().getName(), dependency);
  }

  @Override
  public void addDependency(Class name, Object wire) {
    beans.put(name.getName(), wire);
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
    if (capacity < 1) {
      return 1;
    }
    return capacity;
  }

  @Override
  public void execute(Runnable job, long key) {
    keyPoolExecutor.addJob(job, key);
  }

  @Override
  public void execute(CiotolaConnectionService connectionService) {
    connectionPool.addConnection(connectionService);
  }

  @Override
  public void stop() {
    connectionPool.shutDownPool();
    keyPoolExecutor.shutdown();
    executorService.shutdownNow();
  }

  @Override
  public CiotolaDirector getDirector() {
    return ciotolaDirector;
  }

  @Override
  public long getConnectionTimeOut() {
    return connectionTimeOut;
  }

  @Override
  public void setConnectionTimeOut(long connectionTimeOut) {
    this.connectionTimeOut = connectionTimeOut;
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
    int counter = serviceRunners.size();
    for (String key : serviceInterfaceMap.keySet()) {
      PooledServiceRunner runner = new PooledServiceRunner(serviceInterfaceMap.get(key), counter);
      runner.start();
      executorService.execute(runner);
      ++counter;
      serviceRunners.put(counter, runner);
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
          Object beanWire = beans.get(objField.getType().getName());
          if (autoWrite == null && beanWire != null) {
            autoWrite = beanWire;
          } else {
            logger.error(Ciotola.UNKNOWN_COMPONENT_SPECIFIED);
            break;
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

  private int getNumberOfCPUCores() {
    OSValidator osValidator = new OSValidator();
    String command = "";
    if (osValidator.isMac()) {
      logger.debug("System is running Mac OS");
      command = "sysctl -n machdep.cpu.core_count";
    } else if (osValidator.isUnix()) {
      logger.debug("System is running a Linux/Unix variant - trying lscpu");
      command = "lscpu";
    } else if (osValidator.isWindows()) {
      logger.debug("System is running Microsoft Windows");
      command = "cmd /C WMIC CPU Get /Format:List";
    }
    Process process = null;
    int numberOfCores = 0;
    int sockets = 0;
    try {
      if (osValidator.isMac()) {
        String[] cmd = {"/bin/sh", "-c", command};
        process = Runtime.getRuntime().exec(cmd);
      } else {
        process = Runtime.getRuntime().exec(command);
      }
    } catch (IOException e) {
      logger.error("Unable to determine physical processor layout - returning default", e);
      return threadCapacity();
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;

    try {
      while ((line = reader.readLine()) != null) {
        if (osValidator.isMac()) {
          numberOfCores = line.length() > 0 ? Integer.parseInt(line) : 0;
        } else if (osValidator.isUnix()) {
          if (line.contains("Core(s) per socket:")) {
            numberOfCores = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
          }
          if (line.contains("Socket(s):")) {
            sockets = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
          }
        } else if (osValidator.isWindows()) {
          if (line.contains("NumberOfCores")) {
            numberOfCores = Integer.parseInt(line.split("=")[1]);
          }
        }
      }
    } catch (IOException e) {
      logger.error("Unable to determine physical processor layout - returning default", e);
      return threadCapacity();
    }
    if (osValidator.isUnix()) {
      return numberOfCores * sockets;
    }
    return numberOfCores;
  }
}
