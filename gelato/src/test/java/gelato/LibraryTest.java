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

import gelato.server.GelatoServerConnection;
import gelato.server.GelatoServerManager;
import gelato.server.manager.controllers.GelatoDirectoryController;
import gelato.server.manager.controllers.impl.GelatoDirectoryControllerImpl;
import org.junit.jupiter.api.Test;
import protocol.P9Protocol;

class LibraryTest {
 /* @Test
  void testSomeLibraryMethod() throws InterruptedException {

     GelatoServerManager serveletManager = new GelatoServerManager(9090);
     serveletManager.start();

     GelatoDirectoryController testServe = new GelatoDirectoryControllerImpl(serveletManager);
     testServe.setDirectoryName(GelatoDirectoryController.ROOT_DIR);
     GelatoDirectoryController newDirectory = new GelatoDirectoryControllerImpl(serveletManager);
     testServe.addDirectory(newDirectory);
     serveletManager.setRootDirectory(testServe);
     serveletManager.addResource(newDirectory);

     /*GelatoConnection client = library.createClientConnection(config);
     GelatoFileManager fileManager = new GelatoFileManager(client, library, "TEST", "TEST");
     GelatoDirectory dir = fileManager.getRoot();
     System.out.println("OK");
  }*/

}
