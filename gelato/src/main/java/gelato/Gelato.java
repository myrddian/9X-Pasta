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

package gelato;

import gelato.client.ClientConnection;
import gelato.server.GelatoServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Gelato {
  public static final int BUFFER_SIZE = 4096;;
  final Logger logger = LoggerFactory.getLogger(Gelato.class);;
  private ExecutorService executorService;
  private GelatoTagManager tagManager = new GelatoTagManager();
  private GelatoDescriptorManager descriptorManager = new GelatoDescriptorManager();

  public Gelato() {
    logger.trace("Starting Gelato");
    logger.trace(GelatoVersion.getVersion());
    executorService = Executors.newWorkStealingPool();
  }

  public GelatoTagManager getTagManager() {
    return tagManager;
  }

  public GelatoDescriptorManager getDescriptorManager() {
    return descriptorManager;
  }

  public ClientConnection createClientConnection(GelatoConfigImpl connectionConfig) {
    ClientConnection connection = new ClientConnection(connectionConfig, this);
    return connection;
  }

  public GelatoConnection createConnection(GelatoConfigImpl config) {
    if (config.getMode() == MODE.CLIENT) {
      return createClientConnection(config);
    } else if (config.getMode() == MODE.SERVER) {
      return createServer(config);
    }
    return createServer(config);
  }

  public GelatoServerConnection createServer(GelatoConfigImpl con) {
    GelatoServerConnection connection = new GelatoServerConnection(this, con);
    return connection;
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  public int threadCapacity() {
    return Runtime.getRuntime().availableProcessors();
  }

  public enum TRANSPORT {
    GELATO_TCP
  }

  public enum MODE {
    SERVER,
    CLIENT
  }

  //256 KB buffer for network io
  public final static int DEFAULT_NET_IO_MEM_BUFFER = 256*1024;
}
