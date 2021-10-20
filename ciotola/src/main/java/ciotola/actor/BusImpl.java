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

final class BusImpl implements Bus {

  private Map<String, AgentPortImpl> portMap = new ConcurrentHashMap<>();
  private CiotolaDirector director;


  public BusImpl(CiotolaDirector director) {
    this.director = director;
  }

  @Override
  public AgentPort getPort(String name) {
    return portMap.get(name);
  }

  @Override
  public AgentPort createPort(String name) {
    AgentPortImpl port = new AgentPortImpl(name, director);
    portMap.put(name, port);
    return port;
  }

  @Override
  public AgentPort createPort(String name, boolean broadcast) {
    AgentPortImpl port = new AgentPortImpl(name, director,broadcast);
    portMap.put(name, port);
    return port;
  }

  @Override
  public void removePort(String name) {
    AgentPortImpl target = portMap.get(name);
    //director.removeRole(target.);
    portMap.remove(name);
  }

  @Override
  public void write(String portName, SourceRecord record) {
    this.getPort(portName).write(record);
  }

  @Override
  public void register(SinkAgent agent, String portName) {
    this.getPort(portName).register(agent);
  }

}
