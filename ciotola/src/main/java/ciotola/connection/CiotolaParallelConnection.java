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

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CiotolaParallelConnection extends Thread {

  private final Logger logger = LoggerFactory.getLogger(CiotolaParallelConnection.class);
  private Selector selector;
  private boolean isStarted = false;

  public CiotolaParallelConnection() throws IOException {
    this.selector = Selector.open();
  }

  public boolean registerChannel(SocketChannel newSocket, ChannelAttributesImpl channelId) {
    try {
      newSocket.configureBlocking(false);
      newSocket.register(selector, SelectionKey.OP_READ, channelId);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private void process() throws IOException {
    selector.selectNow();
    Iterator<SelectionKey> iter;
    SelectionKey key;
    iter = selector.selectedKeys().iterator();
    while (iter.hasNext()) {
      key = iter.next();
      iter.remove();
      if (key.isReadable()) {
        ChannelAttributesImpl attributes = (ChannelAttributesImpl) key.attachment();
        attributes.getCallback().process(attributes);
      }
    }
  }

  @Override
  public void run() {
    while (isStarted) {
      try {
        process();
      } catch (Throwable ex) {
        logger.error("An Exception has occurred ", ex);
      }
    }
  }

  public boolean getStarted() {
    return this.isStarted;
  }

  public void setStarted() {
    this.isStarted = true;
    this.start();
  }

  public void halt() {
    this.isStarted = false;
  }
}
