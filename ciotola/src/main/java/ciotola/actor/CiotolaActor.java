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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CiotolaActor extends Thread {

  private final Logger logger = LoggerFactory.getLogger(CiotolaActor.class);
  private Map<Long, RoleImpl> mappedRoles = new ConcurrentHashMap<>();

  private boolean isRunning = true;
  private int runnerId = 0;

  public void setRunnerId(int runnerId) {
    this.runnerId = runnerId;
    this.setName("Actor [" + Integer.toString(runnerId) + "]");
  }

  public void addRole(RoleImpl newRole) {
    mappedRoles.put(newRole.getRoleId(), newRole);
  }

  public void removeRole(RoleImpl removeRole) {
    mappedRoles.remove(removeRole.getRoleId());
  }

  public synchronized boolean isRunning() {
    return isRunning;
  }

  public synchronized void startWorker() {
    isRunning = true;
  }

  public synchronized void stopWorker() {
    isRunning = false;
  }


  private void processRole() throws InterruptedException {
    if (mappedRoles.size() == 0) {
      Thread.sleep(10);
    }
    for (Map.Entry<Long, RoleImpl> entry : mappedRoles.entrySet()) {
      RoleImpl role = entry.getValue();
      if (role.getScript().hasValues() == false && role.getScript().hasReturn() == false) {
        if (role.requests() == 0) {
          executeAction(role.getScript(), role);
        }
      }
      if (role.requests() != 0) {
        ActorAction newAction = role.takeAction();
        if (role.getScript().hasReturn() && role.getScript().hasValues()) {
          executeAction(newAction.getMessage(), role.getScript(), newAction.getFuture(), role);
        } else {
          if (role.getScript().hasReturn()) {
            executeActionRetNoCall(role.getScript(), newAction.getFuture(), role);
          } else if (role.getScript().hasValues()) {
            executeActionNoRetCall(
                newAction.getMessage(), role.getScript(), newAction.getFuture(), role);
          }
        }
      }
    }
  }

  private void executeActionNoRetCall(
      Object message, Script script, CiotolaFutureImpl future, RoleImpl role) {
    try {
      script.process(message);
    } catch (Throwable ex) {
      logger.error(
          "Exception thrown by role: " + role.getRoleId() + "processed by Actor: " + runnerId, ex);
      future.setError(true);
      future.setException(new ActorException(ex));
    }
  }

  private void executeActionRetNoCall(
      Script script, CiotolaFutureImpl future, RoleImpl role) {
    try {
      future.setResult(script.process(null));
    } catch (Throwable ex) {
      logger.error(
          "Exception thrown by role: " + role.getRoleId() + "processed by Actor: " + runnerId, ex);
      future.setError(true);
      future.setException(new ActorException(ex));
    }
  }

  private void executeAction(Script script, RoleImpl role) {
    try {
      script.process(null);
    } catch (Throwable ex) {
      logger.error(
          "Exception thrown by role: " + role.getRoleId() + "processed by Actor: " + runnerId, ex);
    }
  }

  private void executeAction(
      Object message, Script script, CiotolaFutureImpl future, RoleImpl role) {
    try {
      Object ret = script.process(message);
      future.setResult(ret);
    } catch (Throwable ex) {
      logger.error(
          "Exception thrown by role: " + role.getRoleId() + "processed by Actor: " + runnerId, ex);
      future.setError(true);
      future.setException(new ActorException(ex));
    }
  }

  private void guardProcess() {
    try {
      processRole();
    } catch (Throwable ex) {
      logger.error("Actor caught exception", ex);
    }
  }

  @Override
  public void run() {
    logger.debug("Actor [" + Integer.toString(runnerId) + "] Running");
    while (isRunning()) {
      guardProcess();
    }
  }
}
