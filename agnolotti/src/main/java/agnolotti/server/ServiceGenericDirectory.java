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

package agnolotti.server;

import gelato.server.GelatoServerManager;
import gelato.server.manager.controllers.impl.GelatoDirectoryControllerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;

import java.time.Instant;

class ServiceGenericDirectory extends GelatoDirectoryControllerImpl {

  private final Logger logger = LoggerFactory.getLogger(ServiceGenericDirectory.class);

  public ServiceGenericDirectory(
      String serviceUserName,
      String serviceUserGroup,
      long directoryID,
      String directoryName,
      GelatoServerManager serverManager) {
    super(serverManager);
    setResourceName(directoryName);
    StatStruct newStat = getStat();
    newStat.setAccessTime(Instant.now().getEpochSecond());
    newStat.setModifiedTime(newStat.getAccessTime());
    newStat.setUid(serviceUserName);
    newStat.setGid(serviceUserGroup);
    newStat.setMuid(serviceUserName);
    QID qid = getQID();
    qid.setType(P9Protocol.QID_DIR);
    qid.setVersion(0);
    qid.setLongFileId(directoryID);
    newStat.setQid(qid);
    setQID(qid);
    setStat(newStat);
    newStat.updateSize();
  }
}
