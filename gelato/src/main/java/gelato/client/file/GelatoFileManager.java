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

import gelato.Gelato;
import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoTagManager;
import gelato.client.file.impl.GelatoDirectoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GelatoFileManager {

  private final Logger logger = LoggerFactory.getLogger(GelatoFileManager.class);

  private GelatoConnection connection;
  private GelatoClientSession clientSession;
  private Gelato gelato;
  private GelatoTagManager tagManager;
  private GelatoFileDescriptor authDescriptor;
  private GelatoDirectoryImpl root;

  public GelatoFileManager(GelatoConnection con, Gelato library, String userName, String userAuth) {
    connection = con;
    gelato = library;
    tagManager = gelato.getTagManager();
    clientSession = new GelatoClientSession();
    authDescriptor = gelato.getDescriptorManager().generateDescriptor();
    tagManager = gelato.getTagManager();
    tagManager.createTagHandler(authDescriptor);

    clientSession.setTags(tagManager.getManager(authDescriptor));
    clientSession.setConnection(connection);
    clientSession.setManager(gelato.getDescriptorManager());
    clientSession.setAuthorisationDescriptor(authDescriptor);
    clientSession.setUserName(userName);
    clientSession.setUserAuth(userAuth);

    if (clientSession.initSession() != true) {
      logger.error("Unable to establish session");
      throw new RuntimeException("Unable to establish session");
    }
    root = new GelatoDirectoryImpl(clientSession, connection, clientSession.getFileServiceRoot());
  }

  public GelatoDirectory getRoot() {
    return root;
  }

  private String[] parsePath(String path) {
    return path.split("/");
  }
}
