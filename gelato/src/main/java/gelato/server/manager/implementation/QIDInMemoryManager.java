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

package gelato.server.manager.implementation;

import gelato.GelatoFileDescriptor;
import gelato.server.manager.GelatoQIDManager;
import gelato.server.manager.controllers.GelatoResourceController;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.QID;

public class QIDInMemoryManager implements GelatoQIDManager {

  final Logger logger = LoggerFactory.getLogger(QIDInMemoryManager.class);
  private Random rnd = new Random();
  private Map<Long, String> qidMapping = new ConcurrentHashMap<>();
  private Map<Long, GelatoResourceController> resourceHandlerMap = new ConcurrentHashMap<>();

  @Override
  public long generateQIDFieldID(String assetName) {
    long value = Math.abs(rnd.nextLong());
    while (qidMapping.containsKey(value)) {
      value = rnd.nextLong();
    }
    qidMapping.put(value, assetName);
    return value;
  }

  @Override
  public QID generateAuthQID() {
    QID newAuthQID = new QID();
    newAuthQID.setVersion(0);
    newAuthQID.setType((byte) QID.QID_AUTH_NOT_REQUIRED);
    newAuthQID.setLongFileId(generateQIDFieldID("AUTH"));
    return newAuthQID;
  }

  @Override
  public boolean mapResourceHandler(GelatoFileDescriptor id, GelatoResourceController handler) {
    if (resourceHandlerMap.containsKey(id.getQid().getLongFileId()) || handler == null) {
      logger.error("Resource in USE or NULL Handler");
      if (handler != null) {
        logger.error("Clashing mapping: " + id.getQid().getLongFileId());
      } else {
        logger.error("Handler not initialized");
      }
      return false;
    }
    resourceHandlerMap.put(id.getQid().getLongFileId(), handler);
    return true;
  }

  @Override
  public GelatoResourceController getHandler(GelatoFileDescriptor id) {
    if (resourceHandlerMap.containsKey(id.getQid().getLongFileId())) {
      return resourceHandlerMap.get(id.getQid().getLongFileId());
    }
    logger.error("INVALID ID");
    return null;
  }

  @Override
  public boolean removeResource(GelatoFileDescriptor id) {
    resourceHandlerMap.remove(id.getQid().getLongFileId());
    return true;
  }
}
