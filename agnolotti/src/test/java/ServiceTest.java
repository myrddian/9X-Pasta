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

import agnolotti.Agnolotti;
import agnolotti.client.RemoteClient;
import agnolotti.server.ServiceManager;
import org.junit.jupiter.api.Test;

public class ServiceTest {

  @Test
  public void serviceInitTest() throws InterruptedException {
    ServiceManager serviceManager =
        new ServiceManager(Agnolotti.DEFAULT_VER, "test", 9092, "example", "example");
    serviceManager.addRemoteService(TestBomb.class, new TestServiceProxy());

    Thread.sleep(100);

    RemoteClient client =
        new RemoteClient("localhost", 9092, "test", Agnolotti.DEFAULT_VER, "test");

    TestBomb tst = (TestBomb) client.getRemoteService(TestBomb.class);
    // tst.hello();
    // tst.hello("fun");
    // Thread.sleep(4000);

    // RemoteClient client2 = new RemoteClient("localhost",9092,"test", Agnolotti.DEFAULT_VER,
    //        "test");

    // TestBomb tst2 = (TestBomb) client2.getRemoteService(TestBomb.class);
    // tst2.hello();
    // tst2.hello("fun2");

    // tst.hello("test1");
    // tst2.hello("test2");
    // System.out.println(tst.sayHello("tst"));
    // System.out.println(tst.sayHello("tst2"));
    // System.out.println(tst.sayHello("tst3"));
    // System.out.println(tst.sayHello("tst"));
    System.out.println(tst.sayHello(10, "bob"));
    Thread.sleep(4000);
  }
}
