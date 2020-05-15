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

import gelato.GelatoConnection;
import gelato.GelatoDescriptorManager;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.GelatoTags;
import gelato.client.GelatoMessage;
import gelato.client.GelatoMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.P9Protocol;
import protocol.messages.MessageRaw;
import protocol.messages.VersionRequest;
import protocol.messages.request.AttachRequest;
import protocol.messages.response.AttachResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GelatoClientSession implements GelatoSession {

  private final Logger logger = LoggerFactory.getLogger(GelatoClientSession.class);

  private GelatoFileDescriptor authorisationDescriptor = null;
  private GelatoFileDescriptor fileServiceRoot = null;
  private String userName;
  private String nameSpace;
  private String userAuth;
  private GelatoMessaging connection;
  private GelatoDescriptorManager manager = null;
  private GelatoTags tags;
  private Map<String, Object> sessionVars = new ConcurrentHashMap<>();
  private boolean useAuth = false;

  public GelatoClientSession(GelatoMessaging messaging) {
    connection = messaging;
  }

  @Override
  public synchronized void setSessionVar(String varName, Object varValue) {
    sessionVars.put(varName, varValue);
  }

  @Override
  public synchronized Object getSessionVar(String varName) {
    return sessionVars.get(varName);
  }

  @Override
  public int descriptorCount() {
    return manager.size();
  }

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
    return null;
  }

  @Override
  public void setConnection(GelatoConnection connection) { }

  @Override
  public GelatoDescriptorManager getManager() {
    return manager;
  }

  @Override
  public void setManager(GelatoDescriptorManager manager) {
    this.manager = manager;
  }

  public boolean initSession() {

    GelatoMessage<VersionRequest,VersionRequest> versionRequest = connection.createVersionRequest();
    connection.submitMessage(versionRequest);
    VersionRequest rspVersion = versionRequest.getMessage();
    logger.info(
        "Started Session -  Server is: "
            + rspVersion.getVersion()
            + " Max Message Size: "
            + Integer.toString(rspVersion.getMaxMsgSize())
            + " Max Content Size: "
            + Integer.toString(rspVersion.getMaxMsgSize() - MessageRaw.minSize));
    connection.close(versionRequest);

    if (useAuth) {
      if (authHandler() == false) {
        logger.error("Unable to Authorise while initialising session");
        return false;
      }
    } else {
      this.authorisationDescriptor = new GelatoFileDescriptor();
      this.authorisationDescriptor.setRawFileDescriptor(P9Protocol.NO_FID);
    }

    // Now Attach
    GelatoFileDescriptor attachDescriptor = manager.generateDescriptor();
    GelatoMessage<AttachRequest,AttachResponse> request = connection.createAttachTransaction();

    request.getMessage().setUsername(getUserName());
    request.getMessage().setNamespace(getNameSpace()); // default should always be blank/empty
    request.getMessage().setFid(attachDescriptor.getRawFileDescriptor());
    request.getMessage().setAfid(authorisationDescriptor.getRawFileDescriptor());

    connection.submitMessage(request);

    AttachResponse response = request.getResponse();
    attachDescriptor.setQid(response.getServerID());
    this.fileServiceRoot = attachDescriptor;
    logger.info("Client Attached to Root of File Service");
    connection.close(request);
    return true;
  }

  //This is done differently
  public boolean authHandler() {
    return false;
  }

  @Override
  public GelatoTags getTags() {
    return tags;
  }

  @Override
  public void setTags(GelatoTags tags) {
    this.tags = tags;
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
