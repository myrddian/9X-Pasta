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

package gelato.client.file;

import ciotola.Ciotola;
import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GelatoClientCache {

    private boolean shutdown = false;
    private Map<Integer,GelatoResource> cachedResources = new ConcurrentHashMap<>();

    public void addResource(GelatoResource newResource) {
        newResource.cacheValidate();
        cachedResources.put(newResource.getFileDescriptor().getRawFileDescriptor(),newResource);
    }


    private void processMessages() {
        for(GelatoResource resource: cachedResources.values()) {
            resource.cacheValidate();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @CiotolaServiceStop
    public synchronized void shutdown() {
        shutdown = true;
    }

    @CiotolaServiceStart
    public synchronized void start() {
        shutdown = false;
    }

    @CiotolaServiceRun
    public void process() {
        while (!isShutdown()) {
            processMessages();
        }
    }

    public synchronized boolean isShutdown() {
        return shutdown;
    }



    private static GelatoClientCache SINGLE_INSTANCE = null;

    private GelatoClientCache() {}

    public static GelatoClientCache getInstance() {
        if (SINGLE_INSTANCE == null) {
            synchronized (Ciotola.class) {
                if (SINGLE_INSTANCE == null) {
                    SINGLE_INSTANCE = new GelatoClientCache();
                }
            }
        }
        return SINGLE_INSTANCE;
    }

}
