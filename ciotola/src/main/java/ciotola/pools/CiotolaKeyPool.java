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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CiotolaKeyPool {

  private final Logger logger = LoggerFactory.getLogger(CiotolaKeyPool.class);
  private List<CiotolaKeyPoolRunner> workerPool = new ArrayList<>();
  private int threadCapacity;

  public CiotolaKeyPool(int threadCapacity) {
    this.threadCapacity = threadCapacity;
    logger.debug("Pool is initialising with - " + Integer.toString(threadCapacity) + " workers");
    for (int counter = 0; counter < threadCapacity; ++counter) {
      CiotolaKeyPoolRunner runner = new CiotolaKeyPoolRunner();
      runner.setRunnerId(counter);
      workerPool.add(runner);
      runner.start();
    }
  }

  public synchronized void addJob(Runnable job, long key) {
    int scheduleGroup = Math.abs((int) (key % threadCapacity));
    workerPool.get(scheduleGroup).addJob(job);
  }

  public synchronized void shutdown() {
    logger.debug("Stopping workers");
    for (CiotolaKeyPoolRunner runner : workerPool) {
      runner.stopWorker();
    }
  }
}
