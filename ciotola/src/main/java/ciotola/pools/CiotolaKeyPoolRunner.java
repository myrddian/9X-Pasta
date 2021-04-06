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

package ciotola.pools;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CiotolaKeyPoolRunner extends Thread {

  private final Logger logger = LoggerFactory.getLogger(CiotolaKeyPoolRunner.class);
  private BlockingQueue<Runnable> readMessageQueue = new LinkedBlockingQueue<>();
  private boolean isRunning = true;
  private int runnerId = 0;

  public void addJob(Runnable request) {
    try {
      readMessageQueue.put(request);
    } catch (InterruptedException e) {
      logger.error("Interrupted ", e);
    }
  }

  @Override
  public void run() {
    logger.trace("Worker [" + Integer.toString(runnerId) + "] Executing jobs from pool");
    while (isRunning()) {
      try {
        Runnable job = readMessageQueue.take();
        job.run();
      } catch (InterruptedException e) {
        logger.error("Interrupted while runnign job: ", e);
        return;
      } catch (Exception ex) {
        logger.error("Exception thrown by job: ", ex);
        return;
      }
    }
  }

  public void setRunnerId(int runnerId) {
    this.runnerId = runnerId;
  }

  public synchronized boolean isRunning() {
    return isRunning;
  }

  public synchronized void startWorker() {
    isRunning = true;
  }

  public synchronized void stopWorker() {
    isRunning = false;
  }
}
