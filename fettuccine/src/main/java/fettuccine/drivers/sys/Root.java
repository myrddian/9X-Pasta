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

package fettuccine.drivers.sys;

import common.api.fettuccine.FettuccineConstants;
import gelato.server.GelatoServerManager;
import gelato.server.manager.controllers.impl.GelatoDirectoryControllerImpl;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;

public class Root extends GelatoDirectoryControllerImpl {

  public static final long ROOT_ID = 001l;
  public static final String ROOT_NAME = "";

  private final Logger logger = LoggerFactory.getLogger(Root.class);

  public Root(GelatoServerManager serverManager) {
    super(serverManager);
    setResourceName(ROOT_NAME);
    StatStruct newStat = getStat();
    newStat.setAccessTime(Instant.now().getEpochSecond());
    newStat.setModifiedTime(newStat.getAccessTime());
    newStat.setUid(FettuccineConstants.FETTUCCINE_SVC_NAME);
    newStat.setGid(FettuccineConstants.FETTUCCINE_SVC_GRP);
    newStat.setMuid(FettuccineConstants.FETTUCCINE_SVC_NAME);
    QID qid = getQID();
    qid.setType(P9Protocol.QID_DIR);
    qid.setVersion(0);
    qid.setLongFileId(ROOT_ID);
    newStat.setQid(qid);
    newStat.updateSize();
    setQID(qid);
    setStat(newStat);
  }
}
