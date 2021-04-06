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

import ciotola.CiotolaServiceInterface;
import ciotola.ServiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PooledServiceRunner implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(PooledServiceRunner.class);
  private CiotolaServiceInterface javaService;
  private ServiceStatus serviceStatus;
  private int serviceId = 0;

  public PooledServiceRunner(CiotolaServiceInterface service, int id) {
    javaService = service;
    serviceId = id;
    serviceStatus = ServiceStatus.SERVICE_START;
  }

  @Override
  public void run() {
    serviceStatus = ServiceStatus.SERVICE_RUNNING;
    logger.debug("[" + Integer.toString(serviceId) + "] - Running " + javaService.serviceName());
    javaService.run();
    logger.debug("[" + Integer.toString(serviceId) + "] - Stopped " + javaService.serviceName());
    serviceStatus = ServiceStatus.SERVICE_STOP;
  }

  public void stop() {
    logger.debug("[" + Integer.toString(serviceId) + "] - Stopping " + javaService.serviceName());
    javaService.shutdown();
  }

  public void start() {
    logger.debug("[" + Integer.toString(serviceId) + "] - Starting " + javaService.serviceName());
    javaService.startUp();
  }

  public String serviceName() {
    return javaService.serviceName();
  }

  public ServiceStatus getStatus() {
    return serviceStatus;
  }
}
