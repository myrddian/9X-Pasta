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

package gelato.server.manager.implementation;

import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ParallelHandler {
  private final Logger logger = LoggerFactory.getLogger(ParallelHandler.class);
  private BlockingQueue<ParallelRequest> readMessageQueue = new LinkedBlockingQueue<>();
  private boolean isRunning = true;

  public void addMessage(ParallelRequest request) {
    try {
      readMessageQueue.put(request);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @CiotolaServiceStart
  public synchronized void startService() {
    isRunning = true;
  }

  @CiotolaServiceStop
  public synchronized void stopService() {
    isRunning = false;
  }

  public synchronized boolean isRunning() {
    return isRunning;
  }

  @CiotolaServiceRun
  public void run() {
    while (isRunning()) {
      ParallelRequest request = null;
      try {
        request = readMessageQueue.take();
        if (!request
            .getHandler()
            .processRequest(
                request.getConnection(),
                request.getDescriptor(),
                request.getSession(),
                request.getMessage())) {
          logger.error("Failed to process Request");
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
