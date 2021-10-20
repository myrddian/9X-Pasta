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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CiotolaActor extends Thread {


  class ScheduledRole {
    public ActorAction  action;
    public RoleImpl role;
    public boolean background;
  }


  private final Logger logger = LoggerFactory.getLogger(CiotolaActor.class);
  private Map<Long, RoleImpl> mappedRoles = new ConcurrentHashMap<>();

  private boolean isRunning = true;
  private int runnerId = 0;
  private long threadWaitTime = 100L;
  private long noOpWait = threadWaitTime;
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


  /*
   * Generates a list of tasks to execute
   */
  private List<ScheduledRole> scheduler() throws InterruptedException {
    List<ScheduledRole> scheduledRoleList = new ArrayList<>();
    for (Map.Entry<Long, RoleImpl> entry : mappedRoles.entrySet()) {
      RoleImpl role = entry.getValue();
      if (role.getScript().hasValues() == false && role.getScript().hasReturn() == false) {
        if (role.requests() == 0) {
          ScheduledRole scheduledRole = new ScheduledRole();
          scheduledRole.background = true;
          scheduledRole.role = role;
          scheduledRoleList.add(scheduledRole);
        }
      }
      if (role.requests() >= 1) {
        int currentCount = role.requests();
        for(int i=0; i < currentCount; ++i) {
          ScheduledRole scheduledRole = new ScheduledRole();
          scheduledRole.background = false;
          scheduledRole.action = role.takeAction();
          scheduledRole.role = role;
          scheduledRoleList.add(scheduledRole);
        }
      }
    }
    return scheduledRoleList;
  }

  private void processRole() throws InterruptedException {
    //We have nothing mapped wait
    if (mappedRoles.size() == 0) {
      Thread.sleep(threadWaitTime);
      return;
    }

    //long startTime = System.currentTimeMillis();

    List<ScheduledRole> schedule = scheduler();
    int requests = schedule.size();
    if(requests == 0 ) {
      Thread.sleep(noOpWait/4);
      return;
    } else {
      for (ScheduledRole scheduledRole:schedule) {
        if(scheduledRole.background) {
          executeAction(scheduledRole.role.getScript(),scheduledRole.role);
        } else {
          execute(scheduledRole.action,scheduledRole.role.getScript(),scheduledRole.action.getFuture(),scheduledRole.role);
        }
      }
    }
  }

  private void execute(ActorAction action, Script script, CiotolaFutureImpl future, RoleImpl role) {
    try {
      Object retVal;
      if(role.getScript().hasValues()) {
        retVal = script.process(action.getMessage());
      } else {
        retVal  = script.process(null);
      }

      if(retVal!=null) {
        future.setResult(retVal);
      }
    } catch (Throwable ex) {
      logger.error(
          "Exception thrown by role: [" + role.getRoleId() + "] processed by Actor: " + runnerId, ex);
      future.setError(true);
      future.setException(new ActorException(ex));
    }
  }

  private void executeAction(Script script, RoleImpl role) {
    try {
      script.process(null);
    } catch (Throwable ex) {
      logger.error(
          "Exception thrown by role: [" + role.getRoleId() + "] processed by Actor: " + runnerId, ex);
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
