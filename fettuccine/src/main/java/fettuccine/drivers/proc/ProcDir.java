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


import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import com.google.gson.Gson;
import gelato.GelatoFileDescriptor;
import gelato.server.GelatoServerManager;
import gelato.server.manager.controllers.impl.GelatoDirectoryControllerImpl;
import gelato.server.manager.controllers.impl.GelatoFileControllerImpl;
import gelato.server.manager.v2.V2TCPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;
import common.api.fettuccine.FettuccineConstants;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcDir extends GelatoDirectoryControllerImpl {

  public static final long PROC_ID = 100l;
  private boolean shutdown = false;
  private Map<GelatoFileDescriptor, GelatoFileControllerImpl> connectionFiles = new ConcurrentHashMap<>();
  private Gson gson = new Gson();

  private final Logger logger = LoggerFactory.getLogger(ProcDir.class);

    public ProcDir(GelatoServerManager serverManager) {
    super(serverManager);
    setResourceName(FettuccineConstants.PROC_DIR);
    StatStruct newStat = getStat();
    newStat.setAccessTime(Instant.now().getEpochSecond());
    newStat.setModifiedTime(newStat.getAccessTime());
    newStat.setUid(FettuccineConstants.FETTUCCINE_SVC_NAME);
    newStat.setGid(FettuccineConstants.FETTUCCINE_SVC_GRP);
    newStat.setMuid(FettuccineConstants.FETTUCCINE_SVC_NAME);
    QID qid = getQID();
    qid.setType(P9Protocol.QID_DIR);
    qid.setVersion(0);
    qid.setLongFileId(PROC_ID);
    newStat.setQid(qid);
    newStat.updateSize();
    setQID(qid);
    setStat(newStat);
  }

  @CiotolaServiceRun
  public void process() {
    while (!isShutdown()) {

      List<GelatoFileDescriptor> activeConnections = getServerManager().getConnection().getConnections();
      synchronized (this) {

        for(GelatoFileDescriptor file: connectionFiles.keySet()) {
          if(!activeConnections.contains(file)) {
            GelatoFileControllerImpl deleted = connectionFiles.get(file);
            connectionFiles.remove(file);
            removeFile(deleted);
          }
        }

        for(GelatoFileDescriptor con: activeConnections) {
          if(!connectionFiles.containsKey(con)) {
            V2TCPTransport tcpTransport = getServerManager().getConnection().getTransport(con);
            Map<String, Object> detail = new HashMap<>();
            detail.put("port", tcpTransport.getSourcePort());
            detail.put("addr", tcpTransport.getAddress());
            detail.put("id", tcpTransport.getConnectionId());
            detail.put("idlecycle", tcpTransport.getProcessedTime());
            ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(gson.toJson(detail).getBytes());
            GelatoFileControllerImpl newFile = new GelatoFileControllerImpl(Long.toString(con.getDescriptorId()),
                    arrayInputStream,gson.toJson(detail).getBytes().length,
                    getServerManager().getDescriptorManager().generateDescriptor());
            newFile.getStat().setUid(FettuccineConstants.FETTUCCINE_SVC_NAME );
            newFile.getStat().setGid(FettuccineConstants.FETTUCCINE_SVC_GRP);
            newFile.getStat().setMuid(FettuccineConstants.FETTUCCINE_SVC_GRP);
            newFile.getStat().updateSize();
            addFile(newFile);
            connectionFiles.put(con,newFile);
          }
        }

      }
      sleep();
    }
  }

  private void sleep() {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      return;
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

  public synchronized boolean isShutdown() {
    return shutdown;
  }

}
