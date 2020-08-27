/*
 *   Copyright (c) 2020. Enzo Reyes
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ciotola.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CiotolaActor extends Thread{
    private final Logger logger = LoggerFactory.getLogger(CiotolaActor.class);
    private Map<Long, CiotolaRoleImpl> mappedRoles = new ConcurrentHashMap<>();

    private boolean isRunning = true;
    private int runnerId = 0;
    public void setRunnerId(int runnerId) {
        this.runnerId = runnerId;
    }

    public void addRole(CiotolaRoleImpl newRole) {
        mappedRoles.put(newRole.getRoleId(), newRole);
    }

    public void removeRole(CiotolaRoleImpl removeRole) {
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
        for(Map.Entry<Long, CiotolaRoleImpl> entry: mappedRoles.entrySet()) {
            CiotolaRoleImpl role = entry.getValue();
            if(role.requests()!=0) {
                ActorAction newAction = role.takeAction();
                executeAction(newAction.getMessage(), role.getScript(), newAction.getFuture(), role);
            }
        }
    }

    private void executeAction(Object message, CiotolaScript script, CiotolaFutureImpl future, CiotolaRoleImpl role) {
        try {
            Object ret = script.process(message);
            future.setResult(ret);
        } catch (Throwable ex) {
            logger.error("Exception thrown by role: " + role.getRoleId() + "processed by Actor: " + runnerId, ex);
            future.setError(true);
            future.setException(new ActorException(ex));
        }
    }

    @Override
    public void run() {
        logger.trace("Actor [" + Integer.toString(runnerId) + "] Running");
        while (isRunning()) {
            try{
                processRole();
            } catch (InterruptedException e) {
                logger.error("Actor interrupted", e);
            }
        }
    }

}
