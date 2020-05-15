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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo {

    public static final String serviceName = "demo";
    public static final int LOOP_COUNT = 10000;
    public static final int MOD_FACTOR = (int)(LOOP_COUNT * 0.1);
    public static final int RUNNERS = 12;

    public static void main(String[] args) {
        System.out.println("Agnolotti Demo");
        if(args[0].equals("server")) {
            serverDemo();
        } else if(args[0].equals("client")){
            if(args.length < 3){
                System.out.println("Specify host and message");
            } else {
                clientDemo(args[1], args[2]);
            }
        } else if(args[0].equals("loadrunner")) {
            parallelRun(args[1], args[2]);
        } else {
            System.out.println("pick - server or client");
        }
    }



    public static void parallelRun(String host, String msg) {
        class runner implements Runnable {

            int location;
            long[] results;
            String hostName;
            String msg;
            CountDownLatch cnt;

            public runner(int loc, long [] res, String host, String mesg, CountDownLatch countDownLatch)
            {
                location = loc;
                results = res;
                hostName = host;
                msg = mesg;
                cnt = countDownLatch;
            }

            @Override
            public void run() {
                RemoteClient client = new RemoteClient(hostName,9092,serviceName, Agnolotti.DEFAULT_VER,
                        serviceName);
                TestService testService = (TestService) client.getRemoteService(TestService.class);
                long startTime = System.currentTimeMillis();
                for(int i=0; i< LOOP_COUNT; ++i) {
                    testService.echo(msg +" " + Integer.toString(i));
                }
                long stopTime = System.currentTimeMillis();
                results[location] = (stopTime - startTime);
                System.out.println("Total Time Executing - " + Long.toString(results[location]));
                cnt.countDown();
            }
        }

        long [] timingResult = new long[RUNNERS];
        ExecutorService service = Executors.newCachedThreadPool();
        System.out.println("Launching jobs");
        CountDownLatch latch = new CountDownLatch(timingResult.length);
        long taskStart = System.currentTimeMillis();

        for(int counter=0; counter < timingResult.length; ++counter) {
            service.submit(new runner(counter,timingResult,host,msg, latch));
        }
         try {
             latch.await();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
        long stopTask = System.currentTimeMillis();
        System.out.println("Stopping Jobs");
        long totalTime = stopTask - taskStart;
        int executedInvokes = timingResult.length * LOOP_COUNT;
        System.out.println("Total Invocations: " + Integer.toString(executedInvokes));
        System.out.println("Total milliseconds " + Long.toString(totalTime) +" Transactions per second "+ Double.toString(executedInvokes/(totalTime/1000)));
        System.out.println("Effetive Cost: " + Long.toString(totalTime/executedInvokes) +" ms per call");

    }

    public static void nullOrRspTest(TestService testService, boolean test, String echoMsg) {
        long startTime = System.currentTimeMillis();
        System.out.println("Starting Demo");
        for(int i=0; i < LOOP_COUNT; ++i) {
            String rsp="";
            if(test)  {
                rsp = testService.echo(echoMsg +" "+ Integer.toString(i));
            } else {
                testService.nullCall();
            }

            if( (i % MOD_FACTOR) == 0 ) {
                System.out.println(Integer.toString(i) + " - invocations completed");
                if(test) {
                    System.out.println("Echo reply from server: "+rsp);
                }
            }
        }
        long stopTime = System.currentTimeMillis();

        long time = (stopTime - startTime) / 1000;
        if(time <= 0) {
            time = 1;
        }
        long callPerSec = LOOP_COUNT/time;
        long invocationCost =  1000 / callPerSec;
        System.out.println("Total Time taken - " + Long.toString(time));
        System.out.println("Performance: " + Long.toString(callPerSec) + " Invocations per second on a single client");
        System.out.println("Total invocation cost: " + Long.toString(invocationCost) + "ms");
    }

    public static void clientDemo(String host, String msg) {

        RemoteClient client = new RemoteClient(host,9092,serviceName, Agnolotti.DEFAULT_VER,
                serviceName);
        TestService testService = (TestService) client.getRemoteService(TestService.class);

        System.out.println("Executing null call test: ");
        nullOrRspTest(testService,false,msg);
        System.out.println("Executing full call and return marshall test");
        nullOrRspTest(testService,true, msg);
        System.out.println("Tests Over - hit CRTL-C to quit");


    }

    public static void serverDemo() {
        ServiceManager serviceManager = new ServiceManager(Agnolotti.DEFAULT_VER,serviceName, 9092,
                serviceName, serviceName);
        serviceManager.addRemoteService(TestService.class,new TestServiceImpl());

        serviceManager.startService();
    }

}
