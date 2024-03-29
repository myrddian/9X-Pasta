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

package gelato;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gelato {

  // 256 KB buffer for network io
  public static final int DEFAULT_NET_IO_MEM_BUFFER = 256 * 1024;
  public static final String DEFAULT_NAME_SPACE = "";
  final Logger logger = LoggerFactory.getLogger(Gelato.class);
  private GelatoTagManager tagManager = new GelatoTagManager();
  private GelatoDescriptorManager descriptorManager = new GelatoDescriptorManager();

  public Gelato() {
    logger.trace("Starting Gelato");
    logger.trace(GelatoVersion.getVersion());
  }

  public GelatoTagManager getTagManager() {
    return tagManager;
  }

  public GelatoDescriptorManager getDescriptorManager() {
    return descriptorManager;
  }

  public enum MODE {
    SERVER,
    CLIENT
  }
}
