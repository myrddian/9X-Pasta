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

import fettuccine.FettuccineService;
import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.server.GelatoServerManager;
import gelato.server.manager.implementation.SimpleDirectoryServelet;
import gelato.server.manager.response.ResponseAttachHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.messages.response.AttachResponse;

public class ProcDriver implements ResponseAttachHandler {

  private final Logger logger = LoggerFactory.getLogger(ProcDriver.class);
  private ProcDir procDir;
  private GelatoServerManager serveletManager;

  public ProcDriver(GelatoServerManager gelatoServerManager) {
    procDir = new ProcDir();
    serveletManager = gelatoServerManager;
    serveletManager.getSessionHandler().setResponseAttachHandler(this);
    serveletManager.addResource(procDir);
  }

  @Override
  public synchronized boolean writeResponse(
      GelatoConnection connection, GelatoFileDescriptor fileDescriptor, AttachResponse response) {
    logger.info("Mapping Session " + Long.toString(fileDescriptor.getDescriptorId())+ " to PROC");
    SimpleDirectoryServelet connectionDir =
        new SimpleDirectoryServelet(
            fileDescriptor.getDescriptorId(), Long.toString(fileDescriptor.getDescriptorId()));
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
