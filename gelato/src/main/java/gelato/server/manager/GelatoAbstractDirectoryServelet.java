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

import gelato.*;
import gelato.server.manager.implementation.*;
import gelato.server.manager.requests.*;
import org.slf4j.*;
import protocol.*;
import protocol.messages.response.*;

import java.util.*;

public abstract class GelatoAbstractDirectoryServelet extends IgnoreFlushRequests {

    private Map<String, GelatoResourceHandler> directories = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(GelatoAbstractDirectoryServelet.class);
    private Map<String, GelatoResourceHandler> files = new HashMap<>();

    private long calculateSize(long resStatSize) {
        long count = 0;
        for(GelatoResourceHandler dirHandler: directories.values()) {
            count += dirHandler.getStat().getStatSize();
        }

        for(GelatoResourceHandler fileHandler: files.values()) {
            count += fileHandler.getStat().getStatSize();
        }

        return count;
    }

    public void setDirectoryName(String name) {
        this.setResourceName(name);
    }

    public String getDirectoryName() {
        return this.resourceName();
    }

    public boolean containsResource(String resourceName) {
        if(directories.containsKey(resourceName) || files.containsKey(resourceName)) {
            return true;
        }
        return false;
    }

    public GelatoResourceHandler getResource(String resourceName) {
        if(!containsResource(resourceName)) {
            return null;
        }
        if(directories.containsKey(resourceName)) {
            return directories.get(resourceName);
        }
        else {
            return files.get(resourceName);
        }
    }

    public void addDirectory(GelatoAbstractDirectoryServelet newDirectory) {
        if(files.containsKey(newDirectory.getDirectoryName())){
            logger.error("Cannot add the a Directory Handler which is a file");
            return;
        }
        if(directories.containsKey(newDirectory.getDirectoryName())) {
            logger.error("Cannot add the same directory twice");
            return;
        }
        directories.put(newDirectory.getDirectoryName(), newDirectory);
        StatStruct selfStat = getStat();
        selfStat.setLength(calculateSize(newDirectory.getStat().getStatSize()));
    }

    public void addFile(GelatoAbstractFileServelet newFile) {
        if(files.containsKey(newFile.getFileName())){
            logger.error("Cannot add the a File Handler which is a Directory");
            return;
        }
        if(directories.containsKey(newFile.getFileName())) {
            logger.error("Cannot add the same file twice");
            return;
        }
        files.put(newFile.getFileName(), newFile);
        StatStruct selfStat = getStat();
        selfStat.setLength(calculateSize(newFile.getStat().getStatSize()));
    }

    @Override
    public void walkRequest(RequestConnection connection, String fileName, GelatoFileDescriptor newDescriptor) {
        if(!containsResource(fileName)) {
            sendErrorMessage(connection, "File not found");
            return;
        }
        GelatoResourceHandler resourceHandler = getResource(fileName);
        connection.getSession().getManager().mapQID(newDescriptor, resourceHandler.getFileDescriptor());
        WalkResponse response = new WalkResponse();
        response.setQID(resourceHandler.getQID());
        connection.reply(response);
    }

    @Override
    public void closeRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
        connection.getSession().getManager().removeServerResourceMap(clientFileDescriptor);
        CloseResponse closeResponse = new CloseResponse();
        connection.reply(closeResponse);
    }

    @Override
    public void statRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
        StatResponse response = new StatResponse();
        response.setStatStruct(getStat());
        connection.reply(response);
    }

    @Override
    public void readRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, long offset, int numberOfBytes) {

        if(directories.size()==0 && files.size()==0) {
            ReadResponse readResponse = new ReadResponse();
            connection.reply(readResponse);
        }

        //This should already be mapped perhaps?
        for(GelatoResourceHandler dir: directories.values()) {
            StatStruct statStruct = dir.getStat();
            ReadResponse readResponse = new ReadResponse();
            readResponse.setData(statStruct.EncodeStat());
            connection.reply(readResponse);
        }
        for(GelatoResourceHandler files: files.values()) {
            StatStruct statStruct = files.getStat();
            ReadResponse readResponse = new ReadResponse();
            readResponse.setData(statStruct.EncodeStat());
            connection.reply(readResponse);
        }

    }

    @Override
    public void writeRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, long offset, byte[] data) {
        sendErrorMessage(connection, "Unable to issue WRITE ops to a directory");
    }
}
