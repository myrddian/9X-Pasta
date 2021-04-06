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

package ciotola.pools;

import ciotola.Ciotola;
import ciotola.CiotolaConnectionService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionPoolRunner extends Thread {

  private final Logger logger = LoggerFactory.getLogger(ConnectionPoolRunner.class);
  private List<CiotolaConnectionService> serviceList = new ArrayList<>();
  private boolean isRunning = true;
  private int runnerId = 0;
  private long connectionCloseExpiry = 60 * 1000; // 60 seconds

  @Override
  public void run() {
    logger.trace("Worker [" + Integer.toString(runnerId) + "] now servicing connections");
    List<CiotolaConnectionService> removeServices = new ArrayList<>();
    while (isRunning()) {
      boolean processed = false;
      int sizeOfService = getSize();
      if (sizeOfService > 0) {
        for (int counter = 0; counter < sizeOfService; ++counter) {
          CiotolaConnectionService service = getService(counter);
          try {
            if (service.isClosed()) {
              service.notifyClose();
              removeServices.add(service);
            } else {
              long timeProcessed = System.currentTimeMillis();
              if (service.bytesToProcessInbound() > 0) {
                service.processInbound();
                service.setProcessedTime(timeProcessed);
                processed = true;
              }
              if (service.messagesToProcessOutbound() > 0) {
                service.processOutbound();
                service.setProcessedTime(timeProcessed);
                processed = true;
              }
              long timeSpent = timeProcessed - service.getProcessedTime();
              if (timeSpent > connectionCloseExpiry) {
                logger.debug(
                    "["
                        + Integer.toString(runnerId)
                        + "] - Closing connection: "
                        + Long.toString(service.getConnectionId())
                        + "  - CONNECTION_IDLE ["
                        + Long.toString(timeSpent / 1000)
                        + "] Seconds - Expiry at: "
                        + Long.toString(connectionCloseExpiry / 1000));
                service.notifyClose();
                removeServices.add(service);
              }
            }

          } catch (Exception ex) {
            logger.error("Exception thrown by job: ", ex);
            logger.debug(
                "["
                    + Integer.toString(runnerId)
                    + "] - Closing connection: "
                    + Long.toString(service.getConnectionId())
                    + "  - EXCEPTION_ERROR");
            service.notifyClose();
            removeServices.add(service);
          }
        }
        removeDeadServices(removeServices);
      }
      if (sizeOfService == 0) {
        try {
          Thread.sleep(5);
        } catch (InterruptedException e) {
          logger.error("Interrupted while running job: ", e);
          return;
        }
      }
    }
  }

  public void setRunnerId(int runnerId) {
    this.runnerId = runnerId;
  }

  public void setConnectionCloseExpiry(long timeInSeconds) {
    connectionCloseExpiry = timeInSeconds * 1000;
  }

  public synchronized void addService(CiotolaConnectionService service) {
    logger.debug(
        "Added new Connection to worker "
            + Integer.toString(runnerId)
            + " Connection id: "
            + Long.toString(service.getConnectionId()));
    serviceList.add(service);
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

  private synchronized int getSize() {
    return serviceList.size();
  }

  private synchronized CiotolaConnectionService getService(int id) {
    return serviceList.get(id);
  }

  private synchronized void removeDeadServices(List<CiotolaConnectionService> removal) {
    for (CiotolaConnectionService service : removal) {
      logger.trace("Removing Service - " + Long.toString(service.getConnectionId()));
      if (service.getProxyId() != CiotolaConnectionService.NO_PROXY_TERMINATION) {
        Ciotola.getInstance().removeService(service.getProxyId());
      }
      serviceList.remove(service);
    }
  }
}
