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

package fettuccine.drivers.proc;

import fettuccine.*;
import gelato.*;
import gelato.server.manager.*;
import gelato.server.manager.implementation.*;
import gelato.server.manager.requests.*;
import org.slf4j.*;
import protocol.messages.response.*;

public class ProcDriver implements ResponseAttachHandler {

    private ProcDir procDir;
    private GelatoFileServeletManager serveletManager;
    private final Logger logger = LoggerFactory.getLogger(ProcDriver.class);

    public ProcDriver(GelatoFileServeletManager gelatoFileServeletManager) {
        procDir = new ProcDir();
        serveletManager = gelatoFileServeletManager;
        serveletManager.getSessionHandler().setResponseAttachHandler(this);
        serveletManager.addResource(procDir);
    }


    @Override
    public synchronized boolean writeResponse(GelatoConnection connection, GelatoFileDescriptor fileDescriptor, AttachResponse response) {
        logger.info("Mapping Session to PROC");
        SimpleDirectoryServelet connectionDir = new SimpleDirectoryServelet(fileDescriptor.getDescriptorId(), Long.toString(fileDescriptor.getDescriptorId()));
        connectionDir.setUid(FettuccineService.FETTUCCINE_SVC_NAME);
        connectionDir.setGid(FettuccineService.FETTUCCINE_SVC_GRP);
        connectionDir.setMuid(FettuccineService.FETTUCCINE_SVC_NAME);
        procDir.addDirectory(connectionDir);
        serveletManager.addResource(connectionDir);
        connection.sendMessage(fileDescriptor, response.toMessage());
        return true;
    }

    public ProcDir getProcDir() {
        return procDir;
    }
}
