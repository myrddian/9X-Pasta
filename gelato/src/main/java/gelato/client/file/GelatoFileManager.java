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

package gelato.client.file;

import gelato.*;
import gelato.client.file.impl.*;
import org.slf4j.*;
import protocol.*;
import protocol.messages.*;
import protocol.messages.request.*;
import protocol.messages.response.*;

import java.util.*;

public class GelatoFileManager {

    final Logger logger = LoggerFactory.getLogger(GelatoFileManager.class);


    private  GelatoConnection connection;
    private  GelatoSession clientSession;
    private  Gelato gelato;
    private  GelatoTagManager tagManager;
    private  GelatoFileDescriptor authDescriptor;

    public GelatoFile open(String file, byte mode) {
        GelatoFileDescriptor tmp = clientSession.getManager().generateDescriptor();
        GelatoFileImpl newFile = new GelatoFileImpl();
        newFile.setDescriptor(tmp);
        newFile.setFileName(file);
        //WALK to file
        //Open File
        OpenRequest openRequest = new OpenRequest();
        openRequest.setFileDescriptor(tmp.getRawFileDescriptor());
        openRequest.setMode(mode);
        openRequest.setTag(tagManager.getManager(authDescriptor).generateTag());
        Message msgOpenRequest = openRequest.toMessage();
        connection.sendMessage(msgOpenRequest);
        Message rspMessage = connection.getMessage();
        if(rspMessage.messageType != P9Protocol.ROPEN) {
            logger.error("GOT INCORRECT RESPOST ");
            throw new RuntimeException("Unable to open file");
        }
        OpenResponse rsp = Decoder.decodeOpenResponse(rspMessage);
        tmp.setQid(rsp.getFileQID());
        newFile.setIoSize(rsp.getSizeIO());
        tagManager.getManager(authDescriptor).closeTag(openRequest.getTag());
        return newFile;
    }

    public GelatoFile createFile(String fileName) {
        return null;
    }

    public GelatoDirectory getRoot() {
        GelatoFileDescriptor rootDescriptor = clientSession.getFileServiceRoot();

        StatRequest requestRootStat = new StatRequest();
        requestRootStat.setFileDescriptor(rootDescriptor.getRawFileDescriptor());
        requestRootStat.setTransactionId(clientSession.getTags().generateTag());
        connection.sendMessage(requestRootStat.toMessage());
        Message response = connection.getMessage();
        if(response.messageType != P9Protocol.RSTAT) {
            return null;
        }
        StatResponse statResponse = Decoder.decodeStatResponse(response);
        long dirSize = statResponse.getStatStruct().getLength() - statResponse.getStatStruct().getStatSize();
        ReadRequest requestDirEntries = new ReadRequest();

        requestDirEntries.setTag(clientSession.getTags().generateTag());
        requestDirEntries.setFileDescriptor(rootDescriptor.getRawFileDescriptor());
        requestDirEntries.setBytesToRead((int)statResponse.getStatStruct().getLength());
        connection.sendMessage(requestDirEntries.toMessage());
        response = connection.getMessage();
        GelatoDirectoryImpl retValue = new GelatoDirectoryImpl();

        return retValue;
    }

    public List<GelatoFile> fileList() {
        return null;
    }

    public void close(GelatoFile closeFile) {}
    public void terminate() {}

    public GelatoFileManager(GelatoConnection con,
                             Gelato library,
                             String userName,
                             String userAuth) {
        connection = con;
        gelato = library;
        tagManager = gelato.getTagManager();
        clientSession = new GelatoSession();
        authDescriptor = gelato.getDescriptorManager().generateDescriptor();
        tagManager = gelato.getTagManager();
        tagManager.createTagHandler(authDescriptor);

        clientSession.setTags(tagManager.getManager(authDescriptor));
        clientSession.setConnection(connection);
        clientSession.setManager(gelato.getDescriptorManager());
        clientSession.setAuthorisationDescriptor(authDescriptor);
        clientSession.setUserName(userName);
        clientSession.setUserAuth(userAuth);


        if(clientSession.initSession()!=true) {
            logger.error("Unable to establish session");
            throw new RuntimeException("Unable to establish session");
        }

    }


    private String [] parsePath(String path) {
        return path.split("/");
    }

}
