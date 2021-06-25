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

public final class CiotolaDirector {

  public static final String ACTOR_INTERRUPTED_RESULT =
      "Interrupted exception - while fetching result";
  private Map<Integer, CiotolaActor> actorPool = new ConcurrentHashMap<>();
  private Map<Long, RoleImpl> rolePool = new ConcurrentHashMap<>();
  private long scheduleId = 0;
  private BusImpl agentBus = new BusImpl(this);

  public CiotolaDirector(int poolSize) {
    for (int counter = 0; counter < poolSize; ++counter) {
      CiotolaActor actor = new CiotolaActor();
      actor.setRunnerId(counter);
      actorPool.put(counter, actor);
      actor.start();
    }
  }

  public Role createRole(Object javaObject) {
    return createRole(new ActorRunner(javaObject));
  }

  public Role createRole(Object javaObject, int key) {
    return createRole(new ActorRunner(javaObject), key);
  }

  public Bus getBus() {
    return this.agentBus;
  }

  public Role createRole(Script script) {
    RoleImpl newRole = new RoleImpl();
    newRole.setRoleId(getIncrement());
    newRole.setScript(script);

    int targetId = (int) newRole.getRoleId() % actorPool.size();
    actorPool.get(targetId).addRole(newRole);
    rolePool.put(newRole.getRoleId(), newRole);
    newRole.setRoleKey(targetId);
    return newRole;
  }

  public Role createRole(Script script, int key) {
    RoleImpl newRole = new RoleImpl();
    newRole.setRoleId(getIncrement());
    newRole.setScript(script);

    int targetId = key % actorPool.size();
    actorPool.get(targetId).addRole(newRole);
    rolePool.put(newRole.getRoleId(), newRole);
    newRole.setRoleKey(targetId);
    return newRole;
  }

  public Role getRole(Long roleId) {
    return rolePool.get(roleId);
  }

  public void removeRole(Role role) {
    RoleImpl targetRole = rolePool.get(role.getRoleId());
    actorPool.get(targetRole.getRoleKey()).removeRole(targetRole);
    rolePool.remove(targetRole.getRoleId());
  }

  private synchronized long getIncrement() {
    return ++scheduleId;
  }
}
