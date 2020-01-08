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

package gelato.server.manager.implementation;

import gelato.*;
import gelato.server.manager.*;
import org.slf4j.*;
import protocol.*;

import java.util.*;

public class QIDInMemoryManager implements GelatoQIDManager {

    private Random rnd = new Random();
    private Map<Long, String> qidMapping = new HashMap<>();
    private Map<Long, GelatoResourceHandler> resourceHandlerMap = new HashMap<>();
    final Logger logger = LoggerFactory.getLogger(QIDInMemoryManager.class);

    @Override
    public synchronized long generateQIDFieldID(String assetName) {
        long value = rnd.nextLong();
        while(qidMapping.containsKey(value)) {
            value = rnd.nextLong();
        }
        qidMapping.put(value,assetName);
        return value;
    }


    @Override
    public synchronized QID generateAuthQID() {
        QID newAuthQID = new QID();
        newAuthQID.setVersion(0);
        newAuthQID.setType((byte)QID.QID_AUTH_NOT_REQUIRED);
        newAuthQID.setLongFileId(generateQIDFieldID("AUTH"));
        return newAuthQID;
    }

    @Override
    public synchronized boolean mapResourceHandler(GelatoFileDescriptor id, GelatoResourceHandler handler) {
        if(resourceHandlerMap.containsKey(id.getQid().getLongFileId()) || handler == null) {
            logger.error("Resource in USE or NULL Handler");
            return false;
        }
        resourceHandlerMap.put(id.getQid().getLongFileId(), handler);
        return true;
    }

    @Override
    public synchronized GelatoResourceHandler getHandler(GelatoFileDescriptor id) {
        if(resourceHandlerMap.containsKey(id.getQid().getLongFileId())) {
            return resourceHandlerMap.get(id.getQid().getLongFileId());
        }
        logger.error("INVALID ID");
        return null;
    }
}
