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

import ciotola.actor.ActorCall;
import ciotola.actor.AgentPort;
import ciotola.actor.CiotolaDirector;
import ciotola.actor.CiotolaFuture;
import ciotola.actor.Role;
import ciotola.actor.Script;
import ciotola.actor.SinkAgent;
import ciotola.actor.SourceAgent;
import ciotola.actor.SourceProducer;
import ciotola.actor.SourceRecord;
import ciotola.connection.TLVParserFactory;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

public class SimpleTest {


  class MethodTester {
    public String testMethod(String test) {
      System.out.println(test);
      return test+"---NEW";
    }

    public String testTwo(int inNumber) {
      return "Hello: " + inNumber;
    }
  }

  @Test
  public void simpleTest() throws IOException {
    /*Ciotola ciotola = Ciotola.getInstance();
    CiotolaServerConnection serverConnection = new CiotolaServerConnection(600,ciotola.threadCapacity(),null,ciotola);
    ciotola.addService(serverConnection);
    ciotola.startContainer();

    while(true);*/

    TLVParserFactory<TestTLVMesg> testFactory = new TLVParserFactory<>();
    testFactory.handle(TestTLVMesg.class);
    CiotolaDirector director = Ciotola.getInstance().getDirector();

    MethodTester testing = new MethodTester();
    ActorCall<String> asyncCall = director.createCall(testing,"testMethod");
    ActorCall<String> testMethodTwo = director.createCall(testing,"testTwo");
    CiotolaFuture<String> ret = asyncCall.call("test-bob");
    CiotolaFuture<String> retValues = testMethodTwo.call(1);
    System.out.println(ret.get());
    System.out.println(retValues.get());


    Role<String, String> newRole = director.createRole(new Script<String, String>() {
      @Override
      public String process(String message)
          throws InvocationTargetException, IllegalAccessException {
        System.out.println((String) message);
        return "hello there: " + message;
      }

      @Override
      public boolean hasReturn() {
        return true;
      }

      @Override
      public boolean hasValues() {
        return true;
      }
    });

    AgentPort<String> port = director.getBus().createPort("test",true);
    SourceAgent<String> helloSource = port.createSource(new SourceProducer<String>() {
      @Override
      public void execute(AgentPort<String> target) {
        try {
          Thread.sleep(500);
          target.write("HELLO THERE");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      @Override
      public boolean isReady() {
        return true;
      }
    }, false);
    port.register(new SinkAgent<String>() {
      @Override
      public void onRecord(SourceRecord<String> record) {
        System.out.println(record.getValue()+" One");
        System.out.println(record.getPort());
      }
    });

    port.register(new SinkAgent<String>() {
      @Override
      public void onRecord(SourceRecord<String> record) {
        System.out.println(record.getValue()+" Two");
        System.out.println(record.getPort());
      }
    });

    SourceAgent<String> welcomeSource = port.createSource(new SourceProducer<String>() {
      @Override
      public void execute(AgentPort<String> target) {
        try {
          Thread.sleep(500);
          target.write("WELCOME");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      @Override
      public boolean isReady() {
        return true;
      }
    }, true);

    int counter = 0;
    while (true) {
      CiotolaFuture<String> result = newRole.send("Test: "+ counter);
      ++counter;
        System.out.println(result.get());
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }
}
