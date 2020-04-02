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

import gelato.GelatoConnection;
import gelato.GelatoDescriptorManager;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.GelatoTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GelatoServerSession implements GelatoSession {

  private final Logger logger = LoggerFactory.getLogger(GelatoServerSession.class);

  private GelatoFileDescriptor authorisationDescriptor = null;
  private String userName;
  private String nameSpace;
  private String userAuth;
  private GelatoConnection connection;
  private GelatoDescriptorManager manager = null;
  private GelatoTags tags;
  private Map<String, Object> sessionVars = new HashMap<>();

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
}
