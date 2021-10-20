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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RoleImpl<T, R> implements Role<T, R> {

  private final Logger logger = LoggerFactory.getLogger(RoleImpl.class);
  private long roleId;
  private int roleKey;
  private BlockingQueue<ActorAction<T>> readMessageQueue = new LinkedBlockingQueue<>();
  private RunnableScript<T, R> executeTask = null;

  public int requests() {
    return readMessageQueue.size();
  }

  public ActorAction<T> takeAction() throws InterruptedException {
    return readMessageQueue.take();
  }

  @Override
  public CiotolaFuture<R> send(T message) {

    CiotolaFutureImpl<R> resp = new CiotolaFutureImpl<>();
    ActorAction<T> action = new ActorAction<>();
    action.setFuture(resp);
    action.setMessage(message);
    try {
      readMessageQueue.put(action);
    } catch (InterruptedException e) {
      logger.error("Role interrupted", e);
      throw new ActorException(e);
    }
    return resp;
  }

  public RunnableScript<T, R> getScript() {
    return executeTask;
  }

  public void setScript(RunnableScript script) {
    executeTask = script;
  }

  @Override
  public long getRoleId() {
    return roleId;
  }

  public void setRoleId(long newId) {
    roleId = newId;
  }

  public int getRoleKey() {
    return roleKey;
  }

  public void setRoleKey(int roleKey) {
    this.roleKey = roleKey;
  }
}
