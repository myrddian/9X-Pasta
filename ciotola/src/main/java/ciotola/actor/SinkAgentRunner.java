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


final class SinkAgentRunner<T> implements RunnableScript<SourceRecord<T>, Object> {

  private SinkAgent<T> sinkAgent;
  private Role agentRole;

  public void addAgent(SinkAgent<T> newAgent) {
    this.sinkAgent = newAgent;
  }

  public void setRole(Role agentRole) {
    this.agentRole = agentRole;
  }

  public Role getAgentRole() {
    return agentRole;
  }

  @Override
  public Object process(SourceRecord<T> message){
    sinkAgent.onRecord(message);
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
