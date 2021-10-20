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

package ciotola.actor;

import ciotola.Ciotola;

public abstract class AbstractBackgroundAgent implements RunnableScript, Runnable{

  private long delay = 250;
  private long lastRun = 0;
  private boolean processing = false;

  public long getDelay() {
    return delay;
  }
  public void setDelay(long newDelay) {
    this.delay = newDelay;
  }

  public abstract void process();

  private void forkJoin() {
    synchronized (this) {
      if(processing == false) {
        Ciotola.getInstance().submitJob(this);
        processing = true;
      }
    }
  }

  @Override
  public Object process(Object message) {
    long diffTime =  System.currentTimeMillis() - lastRun;
    if(diffTime > delay) {
      forkJoin();
      lastRun =  System.currentTimeMillis();
    }
    return null;
  }

  @Override
  public boolean hasReturn() {
    return false;
  }

  @Override
  public boolean hasValues() {
    return false;
  }

  @Override
  public void run() {
    process();
    synchronized (this) {
      processing = false;
    }
  }
}
