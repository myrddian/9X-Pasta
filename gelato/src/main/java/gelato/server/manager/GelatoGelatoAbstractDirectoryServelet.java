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

package gelato.server.manager;

import gelato.GelatoFileDescriptor;
import gelato.server.manager.implementation.IgnoreFlushRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.ByteEncoder;
import protocol.P9Protocol;
import protocol.StatStruct;
import protocol.messages.response.CloseResponse;
import protocol.messages.response.ReadResponse;
import protocol.messages.response.StatResponse;
import protocol.messages.response.WalkResponse;

import java.util.HashMap;
import java.util.Map;

public abstract class GelatoGelatoAbstractDirectoryServelet extends IgnoreFlushRequests {

  public static final String PARENT_DIR = "..";
  public static final String CURRENT_DIR = ".";
  private final Logger logger =
      LoggerFactory.getLogger(GelatoGelatoAbstractDirectoryServelet.class);
  private Map<String, GelatoGelatoAbstractResourcetHandler> directories = new HashMap<>();
  private Map<String, GelatoGelatoAbstractResourcetHandler> files = new HashMap<>();
  private StatStruct parentDir = null;

  private long calculateSize() {
    long count = 0;
    for (String dirName : directories.keySet()) {
      GelatoGelatoAbstractResourcetHandler dirHandler = directories.get(dirName);
      if (dirName.equals(PARENT_DIR)) {
        count += parentDir.getStatSize();
      } else {
        count += dirHandler.getStat().getStatSize();
      }
    }

    for (GelatoGelatoAbstractResourcetHandler fileHandler : files.values()) {
      count += fileHandler.getStat().getStatSize();
    }

    return count;
  }

  public String getDirectoryName() {
    return this.resourceName();
  }

  public void setDirectoryName(String name) {
    this.setResourceName(name);
  }

  public boolean containsResource(String resourceName) {
    if (directories.containsKey(resourceName) || files.containsKey(resourceName)) {
      return true;
    }
    return false;
  }

  public GelatoGelatoAbstractResourcetHandler getResource(String resourceName) {
    if (!containsResource(resourceName)) {
      return null;
    }
    if (directories.containsKey(resourceName)) {
      return directories.get(resourceName);
    } else {
      return files.get(resourceName);
    }
  }

  public void mapPaths(GelatoGelatoAbstractDirectoryServelet parentDir) {
    directories.put(GelatoGelatoAbstractDirectoryServelet.PARENT_DIR, parentDir);
    this.parentDir = parentDir.getStat().duplicate();
    this.parentDir.setName(GelatoGelatoAbstractDirectoryServelet.PARENT_DIR);
    this.parentDir.updateSize();
  }

  public void addDirectory(GelatoGelatoAbstractDirectoryServelet newDirectory) {
    if (files.containsKey(newDirectory.getDirectoryName())) {
      logger.error("Cannot add the a Directory Handler which is a file");
      return;
    }
    if (directories.containsKey(newDirectory.getDirectoryName())) {
      logger.error("Cannot add the same directory twice");
      return;
    }
    directories.put(newDirectory.getDirectoryName(), newDirectory);
    newDirectory.mapPaths(this);
  }

  public void addFile(GelatoGelatoAbstractFileServelet newFile) {
    if (files.containsKey(newFile.getFileName())) {
      logger.error("Cannot add the a File Handler which is a Directory");
      return;
    }
    if (directories.containsKey(newFile.getFileName())) {
      logger.error("Cannot add the same file twice");
      return;
    }
    files.put(newFile.getFileName(), newFile);
  }

  @Override
  public void walkRequest(
      RequestConnection connection, String fileName, GelatoFileDescriptor newDescriptor) {
    if (!containsResource(fileName)) {
      sendErrorMessage(connection, "File not found");
      return;
    }
    GelatoGelatoAbstractResourcetHandler resourceHandler = getResource(fileName);
    connection.getSession().getManager().mapQID(newDescriptor, resourceHandler.getFileDescriptor());
    WalkResponse response = new WalkResponse();
    response.setQID(resourceHandler.getQID());
    connection.reply(response);
  }

  @Override
  public void closeRequest(
      RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
    connection.getSession().getManager().removeServerResourceMap(clientFileDescriptor);
    CloseResponse closeResponse = new CloseResponse();
    connection.reply(closeResponse);
  }

  @Override
  public void statRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
    StatStruct selfStat = getStat();
    selfStat.setLength(calculateSize());
    StatResponse response = new StatResponse();
    response.setStatStruct(selfStat);
    connection.reply(response);
  }

  @Override
  public void readRequest(
      RequestConnection connection,
      GelatoFileDescriptor clientFileDescriptor,
      long offset,
      int numberOfBytes) {

    if (directories.size() == 0 && files.size() == 0) {
      ReadResponse readResponse = new ReadResponse();
      connection.reply(readResponse);
    }

    // Serialise the whole struct to memory
    byte[] statBuff = new byte[numberOfBytes];
    int ptr = 0;
    byte[] encodedStat = null;

    for (String dirName : directories.keySet()) {
      GelatoGelatoAbstractResourcetHandler dir = directories.get(dirName);
      StatStruct statStruct = dir.getStat();
      if (dirName.equals(GelatoGelatoAbstractDirectoryServelet.PARENT_DIR)) {
        statStruct = this.parentDir;
      }
      encodedStat = statStruct.EncodeStat();
      ByteEncoder.copyBytesTo(encodedStat, statBuff, ptr, encodedStat.length);
      ptr += encodedStat.length;
    }

    for (GelatoGelatoAbstractResourcetHandler files : files.values()) {
      StatStruct statStruct = files.getStat();
      encodedStat = statStruct.EncodeStat();
      ByteEncoder.copyBytesTo(encodedStat, statBuff, ptr, encodedStat.length);
      ptr += encodedStat.length;
    }

    if (statBuff.length < P9Protocol.MAX_MSG_CONTENT_SIZE) {
      ReadResponse readResponse = new ReadResponse();
      readResponse.setData(statBuff);
      connection.reply(readResponse);
    } else {
      ptr = 0;
      while (ptr < statBuff.length) {
        int copy = statBuff.length - ptr;
        if (copy > P9Protocol.MAX_MSG_CONTENT_SIZE) {
          copy = P9Protocol.MAX_MSG_CONTENT_SIZE;
        }
        encodedStat = new byte[copy];
        ReadResponse readResponse = new ReadResponse();
        ByteEncoder.copyBytesTo(encodedStat, statBuff, ptr, copy);
        readResponse.setData(encodedStat);
        connection.reply(readResponse);
        ptr += copy;
      }
    }
  }

  @Override
  public void writeRequest(
      RequestConnection connection,
      GelatoFileDescriptor clientFileDescriptor,
      long offset,
      byte[] data) {
    sendErrorMessage(connection, "Unable to issue WRITE ops to a directory");
  }
}
