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

package gelato.client.file.impl;

import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.client.GelatoMessage;
import gelato.client.GelatoMessaging;
import gelato.client.file.GelatoClientCache;
import gelato.client.file.GelatoDirectory;
import gelato.client.file.GelatoFile;
import gelato.server.manager.controllers.GelatoDirectoryController;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.P9Protocol;
import protocol.StatStruct;
import protocol.messages.request.StatRequest;
import protocol.messages.request.WalkRequest;
import protocol.messages.response.StatResponse;
import protocol.messages.response.WalkResponse;

public class GelatoDirectoryImpl extends GelatoFileImpl implements GelatoDirectory {

  public static final String ERROR_INIT_STAT = "Unable to STAT Directory";
  public static final String ERROR_READ_SIZE_STAT =
      "Stat Size larger than signed INT limit for allocation";
  public static final String ERROR_WALK_READ = "Unable to WALK to target";

  private final Logger logger = LoggerFactory.getLogger(GelatoDirectoryImpl.class);
  private Map<String, GelatoDirectoryImpl> directoryMap = new ConcurrentHashMap<>();
  private Map<String, GelatoFileImpl> fileMap = new ConcurrentHashMap<>();
  private GelatoDirectoryImpl parent = this;
  private GelatoSession session;

  public GelatoDirectoryImpl(
      GelatoSession session, GelatoMessaging messaging, GelatoFileDescriptor descriptor) {
    super(messaging, descriptor);
    this.session = session;
  }

  public GelatoDirectoryImpl getParent() {
    return parent;
  }

  public void setParent(GelatoDirectoryImpl parent) {
    this.parent = parent;
  }

  @Override
  public List<GelatoDirectory> getDirectories() {
    return new ArrayList<GelatoDirectory>(directoryMap.values());
  }

  @Override
  public List<GelatoFile> getFiles() {
    return new ArrayList<GelatoFile>(fileMap.values());
  }

  @Override
  public GelatoDirectory getDirectory(String name) {
    if (name.equals(GelatoDirectoryController.CURRENT_DIR)) {
      return this;
    }
    if (name.equals(GelatoDirectoryController.PARENT_DIR)) {
      return parent;
    }
    if (directoryMap.containsKey(name)) {
      GelatoDirectoryImpl target = directoryMap.get(name);
      return target;
    }
    return null;
  }

  @Override
  public GelatoFile getFile(String fileName) {
    return fileMap.get(fileName);
  }

  @Override
  public void refreshSelf() {
    super.refreshSelf();
    List<StatStruct> entries = refreshStatStruct();
    for (StatStruct entry : entries) {
      if (entry.getName().equals(GelatoDirectoryController.CURRENT_DIR)
          || entry.getName().equals(GelatoDirectoryController.PARENT_DIR)) {
        continue;
      }
      if (directoryMap.containsKey(entry.getName()) == false
          && entry.getQid().getType() == P9Protocol.QID_DIR) {
        walkToTarget(entry);
      }
      if (fileMap.containsKey(entry.getName()) == false
          && entry.getQid().getType() == P9Protocol.QID_FILE) {
        walkToTarget(entry);
      }
    }
  }

  private void addFile(
      StatStruct newEntry, WalkResponse response, GelatoFileDescriptor descriptor) {
    GelatoFileImpl newDir = new GelatoFileImpl(getMessaging(), descriptor);
    String path = "";
    if (getName().equals(GelatoDirectoryController.ROOT_DIR)) {
      path = "/";
    } else {
      path = getPath() + getName() + GelatoDirectoryController.ROOT_DIR;
    }
    newDir.setFilePath(path);
    GelatoClientCache.getInstance().addResource(newDir);
    fileMap.put(newEntry.getName(), newDir);
    logger.debug(
        "Found FILE : "
            + newEntry.getName()
            + " Mapped to Resource: "
            + Long.toString(descriptor.getDescriptorId()));
  }

  private void addDirectory(
      StatStruct newEntry, WalkResponse response, GelatoFileDescriptor descriptor) {
    GelatoDirectoryImpl newDir = new GelatoDirectoryImpl(session, getMessaging(), descriptor);
    newDir.setParent(this);
    String path = "";
    if (getName().equals(GelatoDirectoryController.ROOT_DIR)) {
      path = "/";
    } else {
      path = getPath() + getName() + GelatoDirectoryController.ROOT_DIR;
    }
    newDir.setFilePath(path);
    GelatoClientCache.getInstance().addResource(newDir);
    directoryMap.put(newEntry.getName(), newDir);
    logger.debug(
        "Found : "
            + newEntry.getName()
            + " Mapped to Resource: "
            + Long.toString(descriptor.getDescriptorId())
            + " Path: "
            + newDir.getPath());
  }

  private void walkToTarget(StatStruct newEntry) {
    GelatoMessage<WalkRequest, WalkResponse> walkRequest = getMessaging().createWalkTransaction();
    GelatoFileDescriptor newFileDescriptor = session.getManager().generateDescriptor();
    walkRequest.getMessage().setNewDecriptor(newFileDescriptor.getRawFileDescriptor());
    walkRequest.getMessage().setBaseDescriptor(getFileDescriptor().getRawFileDescriptor());
    walkRequest.getMessage().setTargetFile(newEntry.getName());

    getMessaging().submitMessage(walkRequest);
    WalkResponse walkResponse = walkRequest.getResponse();
    if (walkRequest.isError()) {
      logger.error("Error validating cache for object " + walkRequest.getErrorMessage());
      return;
    }
    newFileDescriptor.setQid(walkResponse.getQID());
    if (newEntry.getQid().getType() == P9Protocol.QID_DIR) {
      addDirectory(newEntry, walkResponse, newFileDescriptor);
    } else {
      addFile(newEntry, walkResponse, newFileDescriptor);
    }
    getMessaging().close(walkRequest);
  }

  private List<StatStruct> refreshStatStruct() {

    List<StatStruct> statEntries = new ArrayList<>();
    GelatoMessage<StatRequest, StatResponse> statRequest = getMessaging().createStatTransaction();
    statRequest.getMessage().setFileDescriptor(getFileDescriptor().getRawFileDescriptor());
    getMessaging().submitMessage(statRequest);
    StatResponse response = statRequest.getResponse();
    getMessaging().close(statRequest);
    if (response == null) {
      logger.error(ERROR_INIT_STAT);
      setResourceValid(false);
      logger.error(statRequest.getErrorMessage());
      return statEntries;
    }
    StatStruct statStruct = response.getStatStruct();
    setStatStruct(statStruct);
    // Process the read request for Stat entries

    // directory is empty
    if (response.getStatStruct().getLength() == 0) {
      return statEntries;
    }
    int sizeOfStatEntries = (int) statStruct.getLength();
    if (sizeOfStatEntries < 0) {
      logger.error(ERROR_READ_SIZE_STAT);
      setResourceValid(false);
      return statEntries;
    }

    // Allocate buffer
    byte[] statBuff = new byte[sizeOfStatEntries];
    int remaining = 0;

    InputStream readStream = getFileInputStream();
    int content;
    int location = 0;
    try {
      while ((content = readStream.read()) != -1) {
        statBuff[location] = (byte) content;
        ++location;
      }
    } catch (IOException e) {
      logger.error("Exception in refresh ", e);
      setResourceValid(false);
      return statEntries;
    }

    // Decode Stat structure into array for processing
    remaining = 0;

    while (remaining < sizeOfStatEntries) {
      StatStruct currStat = new StatStruct();
      currStat = currStat.DecodeStat(statBuff, remaining);
      statEntries.add(currStat);
      remaining += currStat.getStatSize();
    }

    return statEntries;
  }
}
