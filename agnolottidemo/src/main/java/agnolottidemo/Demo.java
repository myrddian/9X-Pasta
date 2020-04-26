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

package agnolottidemo;

import agnolotti.Agnolotti;
import agnolotti.client.RemoteClient;
import agnolotti.server.ServiceManager;

public class Demo {

    public static final String serviceName = "demo";
    public static final int LOOP_COUNT = 1000;
    public static final int MOD_FACTOR = (int)(LOOP_COUNT * 0.1);

    public static void main(String[] args) {
        System.out.println("Agnolotti Demo");
        if(args[0].equals("server")) {
            serverDemo();
        } else if(args[0].equals("client")){
            clientDemo();
        } else {
            System.out.println("pick - server or client");
        }
    }

    public static void clientDemo() {

        RemoteClient client = new RemoteClient("localhost",9092,serviceName, Agnolotti.DEFAULT_VER,
                serviceName);
        TestService testService = (TestService) client.getRemoteService(TestService.class);

        long startTime = System.currentTimeMillis();
        System.out.println("Starting Demo");
        for(int i=0; i < LOOP_COUNT; ++i) {
            testService.nullCall();
            if( (i % MOD_FACTOR) == 0 ) {
                System.out.println(Integer.toString(i) + " - invocations completed");
            }
        }
        long stopTime = System.currentTimeMillis();

        long time = (stopTime - startTime) / 1000;

        System.out.println("Total Time taken - " + Long.toString(time));
        System.out.println("Performance: " + Long.toString(LOOP_COUNT/time) + " Invocations per second on a single client");

    }

    public static void serverDemo() {
        ServiceManager serviceManager = new ServiceManager(Agnolotti.DEFAULT_VER,serviceName, 9092,
                serviceName, serviceName);
        serviceManager.addRemoteService(TestService.class,new TestServiceImpl());

        serviceManager.startService();
    }

}
