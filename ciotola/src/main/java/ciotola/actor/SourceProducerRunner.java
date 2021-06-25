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

public final class SourceProducerRunner<T> extends AbstractSourceAgent<T> implements Runnable {

  private SourceProducer<T> producer;
  private boolean processing = false;

  public SourceProducerRunner(SourceProducer<T> producer, boolean forkJoin) {
    this.producer = producer;
    this.setForkJoinTask(forkJoin);
  }

  public SourceProducerRunner(SourceProducer<T> producer) {
    this.producer = producer;
    this.setForkJoinTask(true);
  }

  private void forkJoin() {
    if (!processing) {
      Ciotola.getInstance().submitJob(this);
      processing = true;
    }
  }

  private void executeTask() {
    producer.execute(this);
  }

  private void isReady() {
    if (producer.isReady()) {
      execReadyTask();
    }
  }

  private void execReadyTask() {
    if (this.isForkJoinTask()) {
      forkJoin();
    } else {
      executeTask();
    }
  }

  @Override
  public void process() {
    this.isReady();
  }


  @Override
  public void run() {
    executeTask();
    processing = false;
  }
}
