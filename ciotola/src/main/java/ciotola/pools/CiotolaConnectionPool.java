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

package ciotola.pools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CiotolaConnectionPool {

    private List<ConnectionPoolRunner> workerPool = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(CiotolaConnectionPool.class);
    private int threadCapacity  = Runtime.getRuntime().availableProcessors();
    private int jobsScheduled = 0;

    public CiotolaConnectionPool() {
        logger.debug("Pool is initialising with - " + Integer.toString(threadCapacity) +" workers");
        for(int counter= 0 ; counter < threadCapacity; ++counter) {
            ConnectionPoolRunner runner = new ConnectionPoolRunner();
            runner.setRunnerId(counter);
            workerPool.add(runner);
            runner.start();
        }
    }

    public synchronized void addConnection(CiotolaConnectionService service) {
        int scheduleGroup =  Math.abs((jobsScheduled % threadCapacity));
        workerPool.get(scheduleGroup).addService(service);
        ++jobsScheduled;
    }

    public synchronized void setIdleTimeout(long timeInSeconds) {
        for(ConnectionPoolRunner runner:workerPool) {
            runner.setConnectionCloseExpiry(timeInSeconds);
        }
    }

}