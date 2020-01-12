
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

import gelato.client.file.*;
import gelato.server.*;
import gelato.server.manager.*;
import gelato.server.manager.implementation.*;
import org.junit.jupiter.api.Test;
import protocol.*;
import protocol.messages.*;

class LibraryTest {
    @Test
    void testSomeLibraryMethod() throws InterruptedException {
        Gelato library = new Gelato();
        GelatoConfigImpl config = new GelatoConfigImpl();
        config.setHost("localhost");
        config.setPort(9093);
        GelatoServerConnection newServer = new GelatoServerConnection(library, config);
        GelatoFileServeletManager serveletManager = new GelatoFileServeletManager(newServer, library);
        serveletManager.start();

        SimpleDirectoryServelet testServe = new SimpleDirectoryServelet(10l,"");
        serveletManager.setRootDirectory(testServe);

        GelatoConnection client = library.createClientConnection(config);
        GelatoFileManager fileManager = new GelatoFileManager(client, library, "TEST", "TEST");

        System.out.println("OK");

     }
}
