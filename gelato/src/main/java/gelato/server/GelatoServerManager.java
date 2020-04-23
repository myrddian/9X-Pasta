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

package gelato.server;

import ciotola.Ciotola;
import gelato.Gelato;
import gelato.GelatoConnection;
import gelato.GelatoDescriptorManager;
import gelato.GelatoFileDescriptor;
import gelato.server.manager.GelatoDescriptorHandler;
import gelato.server.manager.GelatoParallelRequestHandler;
import gelato.server.manager.GelatoQIDManager;
import gelato.server.manager.GelatoSessionHandler;
import gelato.server.manager.GelatoValidateRequestHandler;
import gelato.server.manager.controllers.GelatoDirectoryController;
import gelato.server.manager.controllers.GelatoResourceController;
import gelato.server.manager.implementation.QIDInMemoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GelatoServerManager {

  private final Logger logger = LoggerFactory.getLogger(GelatoServerManager.class);
  private GelatoConnection connection;
  private GelatoQIDManager qidManager;
  private GelatoSessionHandler sessionHandler;
  private GelatoDescriptorHandler descriptorHandler;
  private GelatoValidateRequestHandler validateRequestHandler = new GelatoValidateRequestHandler();
  private GelatoParallelRequestHandler parallelRequestHandler;
  private GelatoDescriptorManager descriptorManager;
  private Ciotola serviceContainer = Ciotola.getInstance();
  private boolean shutdown = false;

  public GelatoServerManager(GelatoConnection connection, Gelato library) {

    this.connection = connection;
    qidManager = new QIDInMemoryManager();
    sessionHandler = new GelatoSessionHandler(qidManager, validateRequestHandler);
    descriptorHandler = new GelatoDescriptorHandler(library, connection, sessionHandler);
    parallelRequestHandler = new GelatoParallelRequestHandler(qidManager);
    descriptorManager = library.getDescriptorManager();
    validateRequestHandler.setNextHandler(parallelRequestHandler);
  }

  public GelatoDescriptorManager getDescriptorManager() {
    return descriptorManager;
  }

  public void addResource(
      GelatoFileDescriptor fileDescriptor, GelatoResourceController newServerResource) {
    newServerResource.setFileDescriptor(fileDescriptor);
    qidManager.mapResourceHandler(fileDescriptor, newServerResource);
  }

  public void addResource(GelatoResourceController newServerResource) {
    qidManager.mapResourceHandler(newServerResource.getFileDescriptor(), newServerResource);
  }

  public void setRootDirectory(GelatoDirectoryController rootDirectory) {
    sessionHandler.setRootAttach(rootDirectory);
    addResource(rootDirectory);
    rootDirectory.setDirectoryName("/");
  }

  public void start() {
    serviceContainer.injectService(descriptorHandler);
    serviceContainer.injectService(connection);
  }

  public void hold() {
    while (!shutdown) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        return;
      }
    }
  }

  public synchronized void shutdown() {
    shutdown = true;
  }

  public synchronized boolean isShutdown() {
    return shutdown;
  }

  public GelatoSessionHandler getSessionHandler() {
    return sessionHandler;
  }
}
