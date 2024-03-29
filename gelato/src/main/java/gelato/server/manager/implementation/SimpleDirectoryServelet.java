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

package gelato.server.manager.implementation;

import gelato.server.GelatoServerManager;
import gelato.server.manager.controllers.impl.GelatoDirectoryControllerImpl;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;

public class SimpleDirectoryServelet extends GelatoDirectoryControllerImpl {

  private final Logger logger = LoggerFactory.getLogger(SimpleDirectoryServelet.class);

  public SimpleDirectoryServelet(
      GelatoServerManager serverManager, long resourceDescriptor, String name) {
    super(serverManager);
    setResourceName(name);
    StatStruct newStat = getStat();
    newStat.setAccessTime(Instant.now().getEpochSecond());
    newStat.setModifiedTime(newStat.getAccessTime());
    newStat.setUid("TEST");
    newStat.setGid("TEST");
    newStat.setMuid("TEST");
    QID qid = getQID();
    qid.setType(P9Protocol.QID_DIR);
    qid.setVersion(0);
    qid.setLongFileId(resourceDescriptor);
    newStat.setQid(qid);
    newStat.updateSize();
    setQID(qid);
    setStat(newStat);
  }

  public void setUid(String uid) {
    getStat().setUid(uid);
    getStat().updateSize();
  }

  public void setGid(String gid) {
    getStat().setGid(gid);
    getStat().updateSize();
  }

  public void setMuid(String muid) {
    getStat().setMuid(muid);
    getStat().updateSize();
  }
}
