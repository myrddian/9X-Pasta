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
import org.slf4j.*;
import protocol.*;
import protocol.messages.*;
import protocol.messages.request.*;
import protocol.messages.response.*;

import java.util.*;

public class GelatoClientSession implements GelatoSession {

    private final Logger logger = LoggerFactory.getLogger(GelatoClientSession.class);

    private GelatoFileDescriptor authorisationDescriptor = null;
    private GelatoFileDescriptor fileServiceRoot = null;
    private String userName;
    private String nameSpace;
    private String userAuth;
    private GelatoConnection connection;
    private GelatoDescriptorManager manager = null;
    private GelatoTags tags;
    private Map<String, Object> sessionVars = new HashMap<>();
    private boolean useAuth = false;


    @Override
    public synchronized void setSessionVar(String varName, Object varValue) {
        sessionVars.put(varName, varValue);
    }

    @Override
    public synchronized Object getSessionVar(String varName) {
        return sessionVars.get(varName);
    }

    @Override
    public int descriptorCount() { return manager.size(); }

    @Override
    public GelatoFileDescriptor getAuthorisationDescriptor() {
        return authorisationDescriptor;
    }

    @Override
    public void setAuthorisationDescriptor(GelatoFileDescriptor authorisationDescriptor) {
        this.authorisationDescriptor = authorisationDescriptor;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getNameSpace() {
        return nameSpace;
    }

    @Override
    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    @Override
    public GelatoConnection getConnection() {
        return connection;
    }

    @Override
    public void setConnection(GelatoConnection connection) {
        this.connection = connection;
    }

    @Override
    public GelatoDescriptorManager getManager() {
        return manager;
    }

    @Override
    public void setManager(GelatoDescriptorManager manager) {
        this.manager = manager;
    }

    public boolean initSession() {
        VersionRequest versionRequest = new VersionRequest();
        versionRequest.setMessageTag(P9Protocol.NO_TAG);
        connection.sendMessage(versionRequest.toMessage());
        Message resp = connection.getMessage();
        if(resp.messageType != P9Protocol.RVERSION) {
            logger.error("Invalid Message received while initialising session");
            throw new RuntimeException("INVALID RSP received");
        }
        VersionRequest rspVersion = Decoder.decodeVersionRequest(resp);
        logger.info("Started Session -  Server is: " + rspVersion.getVersion() + " Max Message Size: "
                + Integer.toString(rspVersion.getMaxMsgSize()) + " Max Content Size: "
                + Integer.toString(rspVersion.getMaxMsgSize() - MessageRaw.minSize) );

        if(useAuth) {
            if(authHandler() == false) {
                logger.error("Unable to Authorise while initialising session");
                return false;
            }
        } else  {
            this.authorisationDescriptor = new GelatoFileDescriptor();
            this.authorisationDescriptor.setRawFileDescriptor(P9Protocol.NO_FID);
        }

        //Now Attach
        GelatoFileDescriptor attachDescriptor = manager.generateDescriptor();
        AttachRequest request = new AttachRequest();
        request.setTag(tags.generateTag());
        request.setUsername(getUserName());
        request.setNamespace(""); //default should always be blank/empty
        request.setFid(attachDescriptor.getRawFileDescriptor());
        request.setAfid(authorisationDescriptor.getRawFileDescriptor());

        connection.sendMessage(request.toMessage());
        resp = connection.getMessage();
        if(resp.messageType != P9Protocol.RATTACH) {
            logger.error("Invalid Message received while initialising session - Expected RATTACH");
            return false;
        }
        AttachResponse response = Decoder.decodeAttachResponse(resp);
        attachDescriptor.setQid(response.getServerID());
        this.fileServiceRoot = attachDescriptor;
        logger.info("Client Attached to Root of File Service");
        return true;
    }

    public boolean authHandler() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUserName(getUserName());
        authRequest.setUserAuth(getUserAuth());
        authRequest.setTag(tags.generateTag());
        if(this.authorisationDescriptor == null) {
            authorisationDescriptor = manager.generateDescriptor();
        }
        authRequest.setAuthFileID(authorisationDescriptor.getRawFileDescriptor());
        connection.sendMessage(authRequest.toMessage());
        Message resp = connection.getMessage();
        if(resp.messageType != P9Protocol.RAUTH) {
            logger.error("Invalid Message received while initialising session - Expected RAUTH");
            return false;
        }
        AuthResponse authResponse = Decoder.decodeAuthResponse(resp);
        authorisationDescriptor.setQid(authResponse.getQid());
        if(authResponse.getQid().getType() == QID.QID_AUTH_NOT_REQUIRED) {
            logger.info("Server requires no authorisation");
        }
        logger.info("Authorisation Complete - QID: " + Long.toString(authResponse.getQid().getLongFileId()) + " " +
                Byte.toString(authResponse.getQid().getType()));
        tags.closeTag(authRequest.getTag());
        return true;
    }

    @Override
    public GelatoTags getTags() {
        return tags;
    }

    @Override
    public void setTags(GelatoTags tags) {
        this.tags = tags;
    }

    @Override
    public String getUserAuth() {
        return userAuth;
    }

    @Override
    public void setUserAuth(String userAuth) {
        this.userAuth = userAuth;
    }

    public boolean isUseAuth() {
        return useAuth;
    }

    public void setUseAuth(boolean useAuth) {
        this.useAuth = useAuth;
    }

    public GelatoFileDescriptor getFileServiceRoot() {
        return fileServiceRoot;
    }
}