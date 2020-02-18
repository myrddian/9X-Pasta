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

package gelato.client.file.impl;

import gelato.*;
import gelato.client.file.*;
import org.slf4j.*;
import protocol.*;
import protocol.messages.*;
import protocol.messages.request.*;
import protocol.messages.response.*;

import java.util.*;

public class GelatoDirectoryImpl implements GelatoDirectory {

    public static final String ERROR_INIT_STAT = "Unable to STAT Directory";
    public static final String ERROR_READ_SIZE_STAT = "Stat Size larger than signed INT limit for allocation";
    public static final String ERROR_WALK_READ = "Unable to WALK to target";

    private final Logger logger = LoggerFactory.getLogger(GelatoDirectoryImpl.class);
    private int scanDepth = 3;
    private GelatoSession session;
    private GelatoConnection connection;
    private GelatoFileDescriptor descriptor;
    private long cacheLoaded = (System.currentTimeMillis() / 1000);
    private long cacheExpiry = 100;
    private StatStruct directoryStat;
    private Map<String, GelatoDirectoryImpl> directoryMap = new HashMap<>();
    private Map<String, GelatoFile> fileMap = new HashMap<>();
    private String path = "";
    private boolean isValid = true;

    public GelatoDirectoryImpl(GelatoSession session,
                               GelatoConnection connection,
                               GelatoFileDescriptor descriptor) {

        logger.trace("New Directory Entry");
        this.session = session;
        this.connection = connection;
        this.descriptor = descriptor;
        scanDirectory(scanDepth);
    }

    public GelatoDirectoryImpl(GelatoSession session,
                               GelatoConnection connection,
                               GelatoFileDescriptor descriptor,
                               int currentDepth,
                               String parentPath) {
        logger.trace("New Directory Entry parent: " + parentPath + " Current Depth: " + Integer.toString(currentDepth));
        this.session = session;
        this.connection = connection;
        this.descriptor = descriptor;
        path = parentPath;
        scanDirectory(currentDepth);
    }


    @Override
    public List<GelatoDirectory> getDirectories() {
        cacheValidate();
        return new ArrayList<GelatoDirectory>(directoryMap.values());
    }

    @Override
    public List<GelatoFile> getFiles() {
        cacheValidate();
        return new ArrayList<GelatoFile>(fileMap.values());
    }

    @Override
    public GelatoDirectory getDirectory(String name) {
        if(directoryMap.containsKey(name)) {
            GelatoDirectoryImpl target = directoryMap.get(name);
            target.scanDirectory(1);
            return directoryMap.get(name);
        }
        return null;
    }

    @Override
    public boolean valid() {
        return isValid;
    }

    @Override
    public String getName() {
        cacheValidate();
        return directoryStat.getName();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getFullName() {
        return path+"/"+getName();
    }

    @Override
    public long getSize() {
        cacheValidate();
        return directoryStat.getLength();
    }

    private void scanDirectory(int currentDepth) {
        logger.info("Scanning Directory depth: " + Integer.toString(currentDepth));
        ErrorMessage err;
        StatRequest requestRootStat = new StatRequest();
        requestRootStat.setFileDescriptor(descriptor.getRawFileDescriptor());
        requestRootStat.setTransactionId(session.getTags().generateTag());
        connection.sendMessage(requestRootStat.toMessage());
        Message response = connection.getMessage();
        if(response.messageType != P9Protocol.RSTAT) {
            logger.error(ERROR_INIT_STAT);
            isValid = false;
            if(response.messageType == P9Protocol.RERROR) {
                 err = Decoder.decodeError(response);
                 logger.error(err.getErrorMessage());
            }
        }
        StatResponse statResponse = Decoder.decodeStatResponse(response);
        directoryStat = statResponse.getStatStruct();

        //Process the read request for Stat entries

        //directory is empty
        if(statResponse.getStatStruct().getLength() == 0 ) {
            return;
        }
        int sizeOfStatEntries = (int)directoryStat.getLength();
        if(sizeOfStatEntries < 0 ) {
            logger.error(ERROR_READ_SIZE_STAT);
            isValid = false;
            if(response.messageType == P9Protocol.RERROR) {
                err = Decoder.decodeError(response);
                logger.error(err.getErrorMessage());
            }
        }

        //Allocate buffer
        byte []statBuff = new byte[sizeOfStatEntries];
        int remaining = 0;

        //Send read request
        ReadRequest requestDirEntries = new ReadRequest();

        requestDirEntries.setTag(session.getTags().generateTag());
        requestDirEntries.setFileDescriptor(descriptor.getRawFileDescriptor());
        requestDirEntries.setBytesToRead((int)statResponse.getStatStruct().getLength());
        connection.sendMessage(requestDirEntries.toMessage());

        //Read Bytes to buffer
        while(remaining < sizeOfStatEntries) {
            response = connection.getMessage();
            ReadResponse readResponse = Decoder.decodeReadResponse(response);
            ByteEncoder.copyBytesTo(readResponse.getData(), statBuff, remaining, readResponse.getData().length - 1);
            remaining += readResponse.getData().length;
        }

        session.getTags().closeTag(requestDirEntries.getTag());

        //Decode Stat structure into array for processing
        List<StatStruct> statEntries = new ArrayList<>();
        remaining = 0;

        while(remaining < sizeOfStatEntries) {
            StatStruct currStat = new StatStruct();
            currStat = currStat.DecodeStat(statBuff,remaining);
            statEntries.add(currStat);
            remaining += currStat.getStatSize();
        }

        if(currentDepth == 0 || currentDepth == -1) {
            return;
        }

        //Generate stat Map
        Map<String, StatStruct> mappedValues = new HashMap<>();

        for(StatStruct entry:statEntries) {
            mappedValues.put(entry.getName(), entry);
        }

        logger.info("Navigating Directory Tree for: " + directoryStat.getName());
        //Attempt a walk and stat - Directories

        for(GelatoDirectoryImpl directory: directoryMap.values()) {
            directory.cacheValidate();
            if(directory.isValid == false) {
                directoryMap.remove(directory.directoryStat.getName());
            }
        }

        for(StatStruct entry: statEntries) {
            if(entry.getQid().getType() == P9Protocol.QID_DIR && directoryMap.containsKey(entry.getName()) == false) {
               //Issue a new walk request
               WalkRequest walkRequest = new WalkRequest();
               GelatoFileDescriptor newFileDescriptor = session.getManager().generateDescriptor();
               walkRequest.setNewDecriptor(newFileDescriptor.getRawFileDescriptor());
               walkRequest.setBaseDescriptor(descriptor.getRawFileDescriptor());
               walkRequest.setTargetFile(entry.getName());
               walkRequest.setTag(session.getTags().generateTag());
               connection.sendMessage(walkRequest.toMessage());
               response = connection.getMessage();
               if(response.messageType != P9Protocol.RWALK) {
                   logger.error(ERROR_WALK_READ);
                   processError(response);
               } else {
                   WalkResponse walkResponse = Decoder.decodeWalkResponse(response);
                   newFileDescriptor.setQid(walkResponse.getQID());
                   session.getTags().closeTag(walkRequest.getTag());
                   GelatoDirectoryImpl newDir = new GelatoDirectoryImpl(session,
                           connection, newFileDescriptor, currentDepth-1, path+"/"+getName());
                   directoryMap.put(entry.getName(), newDir);
                   logger.info("Found : " + entry.getName() + " Mapped to Resource: " + Long.toString(newFileDescriptor.getDescriptorId()) +
                           " Path: " + newDir.getPath());
               }
            }
        }

        for(StatStruct entry: statEntries) {
            if(entry.getQid().getType() == P9Protocol.QID_FILE && directoryMap.containsKey(entry.getName()) == false) {
                //Issue a new walk request
                WalkRequest walkRequest = new WalkRequest();
                GelatoFileDescriptor newFileDescriptor = session.getManager().generateDescriptor();
                walkRequest.setNewDecriptor(newFileDescriptor.getRawFileDescriptor());
                walkRequest.setBaseDescriptor(descriptor.getRawFileDescriptor());
                walkRequest.setTargetFile(entry.getName());
                walkRequest.setTag(session.getTags().generateTag());
                connection.sendMessage(walkRequest.toMessage());
                response = connection.getMessage();
                if(response.messageType != P9Protocol.RWALK) {
                    logger.error(ERROR_WALK_READ);
                    processError(response);
                } else {
                    WalkResponse walkResponse = Decoder.decodeWalkResponse(response);
                    newFileDescriptor.setQid(walkResponse.getQID());
                    session.getTags().closeTag(walkRequest.getTag());

                    //This GelatoFile needs to be redone
                    GelatoFileImpl newFile = new GelatoFileImpl();
                    newFile.setDescriptor(newFileDescriptor);
                    newFile.setFileName(entry.getName());
                    newFile.setFilePath(path);

                    fileMap.put(entry.getName(), newFile);

                }
            }
        }
    }


    private void processError(Message err) {
        ErrorMessage msgErr;
        if(err.messageType == P9Protocol.RERROR) {
            msgErr = Decoder.decodeError(err);
            logger.error(msgErr.getErrorMessage());
        } else {
            logger.error("Unable to process message type is: " + Byte.toString(err.messageType));
        }
    }

    private void cacheValidate() {
        logger.trace("Validating Cache entries");
        long lifeSpan = cacheLoaded - (System.currentTimeMillis()/1000);
        if(lifeSpan > cacheExpiry) {
            scanDirectory(-1);
        }
    }


}
