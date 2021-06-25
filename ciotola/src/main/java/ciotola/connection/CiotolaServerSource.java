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

package ciotola.connection;

import ciotola.Ciotola;
import ciotola.actor.AgentPort;
import ciotola.actor.Bus;
import ciotola.actor.CiotolaDirector;
import ciotola.actor.SourceProducer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CiotolaServerSource implements SourceProducer {
  private final Logger logger = LoggerFactory.getLogger(CiotolaServerSource.class);
  private int port;
  private ServerSocketChannel serverSocketChannel;
  private Selector selector;
  private boolean running = true;
  private Long connections = 0L;
  private ConnectionRegistrar registrar;
  private CiotolaDirector director;



  public CiotolaServerSource(
      int port, int services, ConnectionRegistrar registrar, Ciotola container) throws IOException {
    this.port = port;
    this.serverSocketChannel = ServerSocketChannel.open();
    this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
    this.serverSocketChannel.configureBlocking(false);
    this.selector = Selector.open();
    this.registrar = registrar;
    this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    this.director  = container.getDirector();
  }


  private void registerHandler() {

  }

  @Override
  public void execute(AgentPort target) {
    Iterator<SelectionKey> iter;
    SelectionKey key;
    iter = selector.selectedKeys().iterator();
    while (iter.hasNext()) {
      key = iter.next();
      iter.remove();
      if (key.isAcceptable()) {
        SocketChannel clientConnection = null;
        try {
          clientConnection = ((ServerSocketChannel) key.channel()).accept();
          ChannelAttributesImpl attributes = new ChannelAttributesImpl(clientConnection, key);
          CiotolaConnectionHandler handler = registrar.registerConnection(attributes);
          attributes.setCallBackHandler(handler);

          logger.info("Connection accepted  - ID: {}  - Pool: {}", connections, target);
          ++connections;
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public boolean isReady() {
    try {
      return serverSocketChannel.isOpen() &&(selector.select() != 0);
    } catch (IOException e) {
      return false;
    }
  }
}
