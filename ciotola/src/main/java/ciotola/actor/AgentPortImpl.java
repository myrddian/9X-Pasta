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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class AgentPortImpl<T> implements AgentPort<T>, RunnableScript<SourceRecord<T>, Object> {

  private String portName;
  private Role<SourceRecord<T>, Object> agentRole;
  private Map<Long, SinkAgentRunner<T>> agentMap = new ConcurrentHashMap<>();
  private Map<Long, Role> sourceRoles = new ConcurrentHashMap<>();
  private CiotolaDirector director;
  private boolean duplicate;

  public AgentPortImpl(String name, CiotolaDirector director, boolean sendAll) {
    this.portName = name;
    agentRole = director.createRole(this);
    this.director = director;
    this.duplicate = sendAll;
  }

  public AgentPortImpl(String name, CiotolaDirector director) {
    this.portName = name;
    agentRole = director.createRole(this);
    this.director = director;
    this.duplicate = false;
  }

  @Override
  public void write(Long key, T message) {
    SourceRecord<T> sourceRecord = new SourceRecordImpl<>(message, key, portName);
    agentRole.send(sourceRecord);
  }

  @Override
  public void write(T message) {
    this.write(0L, message);
  }

  @Override
  public void register(SinkAgent<T> listener) {
    SinkAgentRunner runner = new SinkAgentRunner();
    runner.addAgent(listener);
    Role agentRole = director.createRole(runner);
    long id = agentMap.size();
    runner.setRole(agentRole);
    agentMap.put(id, runner);
  }

  @Override
  public String getName() {
    return portName;
  }

  @Override
  public SourceAgent<T> createSource(SourceProducer<T> producer, boolean forkJoin) {
    SourceProducerRunner runner = new SourceProducerRunner(producer, forkJoin);
    runner.register(this);
    Role sourceRole = director.createRole(runner);
    sourceRoles.put(sourceRole.getRoleId(),sourceRole);
    return runner;
  }

  private void scheduleType(SourceRecord<T> message) {
    if(duplicate) {
      broadcastSchedule(message);
    } else {
      hashSchedule(message);
    }
  }

  private void broadcastSchedule(SourceRecord<T> message) {
    for(SinkAgentRunner runner: agentMap.values()) {
      runner.getAgentRole().send(message);
    }
  }

  private void hashSchedule(SourceRecord<T> message) {
    Long msgTarget = 0L;
    if (agentMap.size() > 1) {
      msgTarget = message.getKey() % agentMap.size();
    }
    agentMap.get(msgTarget).getAgentRole().send(message);
  }

  @Override
  public Object process(SourceRecord<T> message){
    scheduleType(message);
    return null;
  }

  @Override
  public boolean hasReturn() {
    return false;
  }

  @Override
  public boolean hasValues() {
    return true;
  }
}
