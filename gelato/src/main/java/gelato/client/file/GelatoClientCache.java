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

package gelato.client.file;

import ciotola.Ciotola;
import ciotola.actor.AbstractBackgroundAgent;
import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GelatoClientCache extends AbstractBackgroundAgent {

  private static GelatoClientCache SINGLE_INSTANCE = null;
  private final Logger logger = LoggerFactory.getLogger(GelatoClientCache.class);
  private boolean shutdown = false;
  private Map<Integer, GelatoResource> cachedResources = new ConcurrentHashMap<>();

  private GelatoClientCache() {
    this.setDelay(25);
  }

  public static GelatoClientCache getInstance() {
    if (SINGLE_INSTANCE == null) {
      synchronized (Ciotola.class) {
        if (SINGLE_INSTANCE == null) {
          SINGLE_INSTANCE = new GelatoClientCache();
        }
      }
    }
    return SINGLE_INSTANCE;
  }

  public void addResource(GelatoResource newResource) {
    cachedResources.put(newResource.getFileDescriptor().getRawFileDescriptor(), newResource);
  }


  @CiotolaServiceStop
  public synchronized void shutdown() {
    shutdown = true;
  }

  @CiotolaServiceStart
  public synchronized void start() {
    shutdown = false;
  }


  public void process() {
    for (GelatoResource resource : cachedResources.values()) {
      resource.cacheValidate();
    }
  }

  public synchronized boolean isShutdown() {
    return shutdown;
  }
}
