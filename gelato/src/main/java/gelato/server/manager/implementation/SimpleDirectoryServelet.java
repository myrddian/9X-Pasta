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

import gelato.GelatoFileDescriptor;
import gelato.server.manager.GelatoGelatoAbstractDirectoryServelet;
import gelato.server.manager.RequestConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.P9Protocol;
import protocol.QID;
import protocol.StatStruct;
import protocol.messages.response.OpenResponse;

import java.time.Instant;

public class SimpleDirectoryServelet extends GelatoGelatoAbstractDirectoryServelet {

  private final Logger logger = LoggerFactory.getLogger(SimpleDirectoryServelet.class);

  public SimpleDirectoryServelet(long resourceDescriptor, String name) {
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

  @Override
  public void openRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, byte mode) {
    if (mode == P9Protocol.OPEN_MODE_OREAD) {
      OpenResponse response = new OpenResponse();
      response.setFileQID(getQID());
      connection.reply(response);
      return;
    }
    sendErrorMessage(connection, "Only READ mode is allowed");
  }

  @Override
  public void createRequest(
      RequestConnection connection, String fileName, int permission, byte mode) {
    sendErrorMessage(connection, "This operation is not supported");
  }

  @Override
  public void removeRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
    sendErrorMessage(connection, "This operation is not supported");
  }

  @Override
  public void writeStatRequest(
      RequestConnection connection,
      GelatoFileDescriptor clientFileDescriptor,
      StatStruct newStruct) {
    sendErrorMessage(connection, "This operation is not supported");
  }
}
