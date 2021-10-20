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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CiotolaDirector {

  public static final String ACTOR_INTERRUPTED_RESULT =
      "Interrupted exception - while fetching result";
  private Map<Integer, CiotolaActor> actorPool = new ConcurrentHashMap<>();
  private Map<Long, RoleImpl> rolePool = new ConcurrentHashMap<>();
  private long scheduleId = 0;
  private BusImpl agentBus = new BusImpl(this);
  private Map<Object,MethodRunner> concurrentObjects = new ConcurrentHashMap<>();

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

  public ActorCall createCall(Object host, String methodName) {
    int count = 0;
    Method methodFound = null;
    for(Method method: host.getClass().getDeclaredMethods()) {
      if(method.getName().equals(methodName)) {
        methodFound = method;
        ++count;
      }
    }
    if(count ==0 || count > 1) {
      throw  new ActorException("Cannot resolve method");
    }
    return createCall(host,methodFound);
  }

  private ActorCall createNewMethodProxy(Object host, Method method) {
    RoleImpl newRole = new RoleImpl();
    MethodRunner runner = new MethodRunner(host,method,newRole);
    method.setAccessible(true);
    newRole.setRoleId(getIncrement());
    newRole.setScript(runner);
    int targetId = (int) newRole.getRoleId() % actorPool.size();
    actorPool.get(targetId).addRole(newRole);
    rolePool.put(newRole.getRoleId(), newRole);
    newRole.setRoleKey(targetId);
    concurrentObjects.put(host,runner);
    return new ActorCallProxy(newRole,method.getName());
  }

  public ActorCall createCall(Object host, Method method) {
    if(concurrentObjects.containsKey(host)) {
      method.setAccessible(true);
      MethodRunner runner = concurrentObjects.get(host);
      runner.addMethod(method.getName(),method);
      return new ActorCallProxy(runner.getRole(),method.getName());
    } else {
      return createNewMethodProxy(host,method);
    }
  }

  public Bus getBus() {
    return this.agentBus;
  }

  private Role createRoleCommon(Script script, int targetId, long increment) {
    RoleImpl newRole = new RoleImpl();
    newRole.setRoleId(increment);

    if(RunnableScript.class.isInstance(script)) {
      newRole.setScript((RunnableScript)script);
    } else {
      newRole.setScript(new RunnableScriptWrapper(script));
    }
    actorPool.get(targetId).addRole(newRole);
    rolePool.put(newRole.getRoleId(), newRole);
    newRole.setRoleKey(targetId);
    return newRole;
  }

  public Role createRole(Script script) {
    long increment = getIncrement();
    int targetId = (int) increment % actorPool.size();
    return createRoleCommon(script,targetId,increment);
  }

  public Role createRole(Script script, int key) {
    int targetId = key % actorPool.size();
    long increment = getIncrement();
    return createRoleCommon(script,targetId,increment);
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
