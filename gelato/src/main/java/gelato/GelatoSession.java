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

package gelato;

import org.slf4j.*;
import protocol.*;
import protocol.messages.*;
import protocol.messages.request.*;
import protocol.messages.response.*;

import java.util.*;

public class GelatoSession {

    private final Logger logger = LoggerFactory.getLogger(GelatoSession.class);

    private GelatoFileDescriptor authorisationDescriptor = null;
    private String userName;
    private String nameSpace;
    private String userAuth;
    private GelatoConnection connection;
    private GelatoDescriptorManager manager = null;
    private GelatoTags tags;
    private Map<String, Object> sessionVars = new HashMap<>();


    public void setSessionVar(String varName, Object varValue) {
        sessionVars.put(varName, varValue);
    }

    public Object getSessionVar(String varName) {
        return sessionVars.get(varName);
    }

    public int descriptorCount() { return manager.size(); }

    public GelatoFileDescriptor getAuthorisationDescriptor() {
        return authorisationDescriptor;
    }

    public void setAuthorisationDescriptor(GelatoFileDescriptor authorisationDescriptor) {
        this.authorisationDescriptor = authorisationDescriptor;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public GelatoConnection getConnection() {
        return connection;
    }

    public void setConnection(GelatoConnection connection) {
        this.connection = connection;
    }

    public GelatoDescriptorManager getManager() {
        return manager;
    }

    public void setManager(GelatoDescriptorManager manager) {
        this.manager = manager;
    }

    public boolean initSession() {
        VersionRequest versionRequest = new VersionRequest();
        versionRequest.setMessageTag(tags.generateTag());
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
        tags.closeTag(versionRequest.getMessageTag());

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUserName(getUserName());
        authRequest.setUserAuth(getUserAuth());
        authRequest.setTag(tags.generateTag());
        if(this.authorisationDescriptor == null) {
            authorisationDescriptor = manager.generateDescriptor();
        }
        authRequest.setAuthFileID(authorisationDescriptor.getDescriptorId());
        connection.sendMessage(authRequest.toMessage());
        resp = connection.getMessage();
        if(resp.messageType != P9Protocol.RAUTH) {
            logger.error("Invalid Message received while initialising session - Expected RAUTH");
            throw new RuntimeException("INVALID RSP received");
        }
        AuthRequestResponse authResponse = Decoder.decodeAuthResponse(resp);
        authorisationDescriptor.setQid(authResponse.getQid());
        if(authResponse.getQid().getType() == QID.QID_AUTH_NOT_REQUIRED) {
            logger.info("Server requires no authorisation");
        }
        logger.info("Authorisation Complete - QID: " + Long.toString(authResponse.getQid().getLongFileId()) + " " +
                Byte.toString(authResponse.getQid().getType()));
        return true;
    }

    public GelatoTags getTags() {
        return tags;
    }

    public void setTags(GelatoTags tags) {
        this.tags = tags;
    }

    public String getUserAuth() {
        return userAuth;
    }

    public void setUserAuth(String userAuth) {
        this.userAuth = userAuth;
    }
}
