/*
 * Copyright (c) 2021.  Enzo Reyes Licensed under the Apache License, Version 2.0 (the "License");   you may
 * not use this file except in compliance with the License.   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package ciotola;

import ciotola.connection.TLVParserFactory;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class SimpleTest {

  @Test
  public void simpleTest() throws IOException {
    /*Ciotola ciotola = Ciotola.getInstance();
    CiotolaServerConnection serverConnection = new CiotolaServerConnection(600,ciotola.threadCapacity(),null,ciotola);
    ciotola.addService(serverConnection);
    ciotola.startContainer();

    while(true);*/

    TLVParserFactory<TestTLVMesg> testFactory = new TLVParserFactory<>();
    testFactory.handle(TestTLVMesg.class);
  }
}
