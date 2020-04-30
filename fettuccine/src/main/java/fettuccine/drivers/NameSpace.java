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

package fettuccine.drivers;

import common.api.fettuccine.FettuccineConstants;
import common.api.fettuccine.FettuccineNameSpace;
import gelato.GelatoFileDescriptor;
import gelato.server.GelatoServerManager;
import gelato.server.manager.controllers.GelatoDirectoryController;
import gelato.server.manager.controllers.impl.GelatoDirectoryControllerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NameSpace implements FettuccineNameSpace {

    private GelatoServerManager serverManager;
    private final Logger logger = LoggerFactory.getLogger(NameSpace.class);
    private Map<String, GelatoDirectoryControllerImpl> nameSpace = new ConcurrentHashMap<>();
    private GelatoDirectoryController rootNsDir;

    public NameSpace(GelatoServerManager server) {
        serverManager = server;
        rootNsDir = new GelatoDirectoryControllerImpl(serverManager);
        rootNsDir.setDirectoryName(FettuccineConstants.NS_DIR);
        GelatoFileDescriptor desc = serverManager.getDescriptorManager().generateDescriptor();
        desc.getQid().setLongFileId(desc.getDescriptorId());
        rootNsDir.setFileDescriptor(desc);
        serverManager.getRoot().addDirectory(rootNsDir);
    }

    @Override
    public List<String> getNameSpaces() {
        return new ArrayList<>(nameSpace.keySet());
    }

    @Override
    public boolean createNameSpace(String nsName) {
        GelatoDirectoryControllerImpl newDir = new GelatoDirectoryControllerImpl(serverManager);
        newDir.setDirectoryName(nsName);
        GelatoFileDescriptor desc = serverManager.getDescriptorManager().generateDescriptor();
        desc.getQid().setLongFileId(desc.getDescriptorId());
        newDir.setFileDescriptor(desc);
        rootNsDir.addDirectory(newDir);
        nameSpace.put(nsName,newDir);
        return true;
    }

    @Override
    public boolean deleteNameSpace(String nsName) {
        return false;
    }
}
