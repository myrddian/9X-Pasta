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

import ciotola.Ciotola;
import gelato.Gelato;
import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoTagManager;
import gelato.client.GelatoMessaging;
import gelato.client.file.impl.GelatoDirectoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GelatoFileManager {

  private final Logger logger = LoggerFactory.getLogger(GelatoFileManager.class);




  private GelatoClientSession clientSession;
  private Gelato gelato;
  private GelatoTagManager tagManager;
  private GelatoFileDescriptor authDescriptor;
  private GelatoDirectoryImpl root;
  private GelatoMessaging messaging;

  public GelatoFileManager(String hostName, int portNumber,
                           String userName) throws IOException {
    this(hostName,portNumber,userName,Gelato.DEFAULT_NAME_SPACE);
  }

  public GelatoFileManager(String hostName, int portNumber,
                           String userName, String nameSpace) throws IOException {

    gelato = new Gelato();
    messaging = new GelatoMessaging(hostName, portNumber);
    tagManager = gelato.getTagManager();
    authDescriptor = gelato.getDescriptorManager().generateDescriptor();
    tagManager = gelato.getTagManager();
    tagManager.createTagHandler(authDescriptor);


    clientSession = new GelatoClientSession(messaging);
    clientSession.setManager(gelato.getDescriptorManager());
    clientSession.setAuthorisationDescriptor(authDescriptor);
    clientSession.setUserName(userName);
    clientSession.setNameSpace(nameSpace);

    if (!clientSession.initSession()) {
      logger.error("Unable to establish session");
      throw new RuntimeException("Unable to establish session");
    }
    Ciotola.getInstance().injectService(GelatoClientCache.getInstance());
    root = new GelatoDirectoryImpl(clientSession, messaging, clientSession.getFileServiceRoot());
    GelatoClientCache.getInstance().addResource(root);
  }



  public GelatoDirectory getRoot() {
    return root;
  }
  public GelatoMessaging getConnection() { return messaging;}
  public GelatoClientSession getClientSession() {
    return clientSession;
  }

}
