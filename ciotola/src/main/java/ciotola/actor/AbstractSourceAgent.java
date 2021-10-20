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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractSourceAgent<T> implements SourceAgent<T>, RunnableScript, AgentPort<T> {

  private long defaultKey;
  private boolean forkJoinTask = false;
  private List<AgentPort<T>> registeredPorts = new CopyOnWriteArrayList<>();

  public long getDefaultKey() {
    return defaultKey;
  }

  public void setDefaultKey(long defaultKey) {
    this.defaultKey = defaultKey;
  }

  public abstract void process();

  public boolean isForkJoinTask() {
    return forkJoinTask;
  }

  public void setForkJoinTask(boolean forkJoinTask) {
    this.forkJoinTask = forkJoinTask;
  }

  public List<AgentPort<T>> getPorts() {
    return registeredPorts;
  }

  @Override
  public Object process(Object message) {
    process();
    return null;
  }

  @Override
  public void register(AgentPort<T> targetPort) {
    registeredPorts.add(targetPort);
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
  public void write(Long key, T message) {
    for (AgentPort<T> ports : registeredPorts) {
      ports.write(key, message);
    }
  }

  @Override
  public void write(T message) {
    this.write(0L, message);
  }

  @Override
  public void register(SinkAgent<T> listener) {
    throw new ActorException("Not supported - is private");
  }

  @Override
  public String getName() {
    throw new ActorException("Not supported");
  }

  @Override
  public SourceAgent<T> createSource(SourceProducer<T> producer, boolean forkJoinTask) {
    throw new ActorException("Not supported");
  }

}
