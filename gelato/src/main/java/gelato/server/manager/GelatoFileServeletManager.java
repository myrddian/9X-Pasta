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

package gelato.server.manager;

import gelato.*;
import gelato.server.manager.implementation.*;
import org.slf4j.*;
import protocol.*;

import java.util.*;

public class GelatoFileServeletManager extends Thread {

    private GelatoConnection connection;
    private GelatoQIDManager qidManager;
    private GelatoSessionHandler sessionHandler;
    private GelatoDescriptorHandler descriptorHandler;
    private Gelato library;
    //private Map<Long, >

    private boolean shutdown = false;
    private final Logger logger = LoggerFactory.getLogger(GelatoFileServeletManager.class);

    @Override
    public void run(){
        logger.info("Running File Servelet Manager");
        while(!isShutdown()) {
            descriptorHandler.processDescriptorMessages();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                logger.error("Thread Interrupted", e);
            }
        }
    }

    public GelatoFileServeletManager(GelatoConnection connection,
                                     Gelato library) {
        this.library = library;
        this.connection = connection;
        qidManager = new QIDInMemoryManager();
        sessionHandler = new GelatoSessionHandler(qidManager);
        descriptorHandler = new GelatoDescriptorHandler(library, connection, sessionHandler);

        if(this.connection.isStarted() == false ){
            this.connection.begin();
        }
    }

    public void addResource(GelatoFileDescriptor fileDescriptor,GelatoResourceHandler newServerResource) {
        qidManager.mapResourceHandler(fileDescriptor, newServerResource);
    }

    public void serve() {
        this.start();
    }

    public synchronized void shutdown() {
        shutdown = true;
    }

    public synchronized boolean isShutdown() {
        return shutdown;
    }
}
