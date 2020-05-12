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
import gelato.GelatoDescriptorManager;
import gelato.server.manager.GelatoParallelRequestHandler;
import gelato.server.manager.GelatoQIDManager;
import gelato.server.manager.controllers.GelatoDirectoryController;
import gelato.server.manager.controllers.GelatoFileController;
import gelato.server.manager.controllers.GelatoResourceController;
import gelato.server.manager.implementation.QIDInMemoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GelatoServerManager {

  private final Logger logger = LoggerFactory.getLogger(GelatoServerManager.class);

  private GelatoServerConnection connection;
  private GelatoQIDManager qidManager;
  private GelatoParallelRequestHandler parallelRequestHandler;
  private GelatoDescriptorManager descriptorManager = new GelatoDescriptorManager();
  private Ciotola serviceContainer = Ciotola.getInstance();
  private GelatoDirectoryController rootDirectory;
  private boolean shutdown = false;


  public GelatoServerManager(int portNumber) {
    connection = new GelatoServerConnection(descriptorManager,portNumber);
    qidManager = new  QIDInMemoryManager();
    parallelRequestHandler = new GelatoParallelRequestHandler(qidManager);
    setup();
  }

  public GelatoServerManager(GelatoServerConnection connection, GelatoQIDManager qidManager) {
    this.connection = connection;
    this.qidManager = qidManager;
    parallelRequestHandler = new GelatoParallelRequestHandler(qidManager);
    setup();
  }


  private void setup() {
    serviceContainer.addDependency(this);
  }


  public void addResource(GelatoResourceController newServerResource) {
    qidManager.mapResourceHandler(newServerResource.getFileDescriptor(), newServerResource);
  }

  public void removeResource(GelatoFileController remove) {
    qidManager.removeResource(remove.getFileDescriptor());
  }

  public void setRootDirectory(GelatoDirectoryController rootDirectory) {
    this.rootDirectory = rootDirectory;
    addResource(rootDirectory);
    rootDirectory.setDirectoryName(GelatoDirectoryController.ROOT_DIR);
  }

  public void start() {
    serviceContainer.injectService(connection);
  }

  public GelatoDescriptorManager getDescriptorManager() {
    return descriptorManager;
  }

  public GelatoServerConnection getConnection() {
    return connection;
  }

  public GelatoDirectoryController getRoot() {
    return rootDirectory;
  }

  public synchronized void shutdown() {
    shutdown = true;
  }

  public synchronized boolean isShutdown() {
    return shutdown;
  }

  public void hold() {
    while (!isShutdown()) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        logger.error("Server Manager Interrupted" , e);
        shutdown = true;
        return;
      }
    }
  }

  public GelatoParallelRequestHandler getParallelRequestHandler() {
    return parallelRequestHandler;
  }

  public void setParallelRequestHandler(GelatoParallelRequestHandler parallelRequestHandler) {
    this.parallelRequestHandler = parallelRequestHandler;
  }

}
