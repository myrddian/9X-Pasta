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

public class TestServiceProxy implements TestBomb{
    @Override
    public void hello() {
        System.out.println("hello");
    }

    @Override
    public String sayHello(String Name) {
        return "hello " + Name;
    }

    @Override
    public String sayHello(String Name, int Age) {
        return sayHello(Name) + " " + Integer.toString(Age);
    }

    @Override
    public String sayHello(int Age, String Name) {
        return sayHello(Name, Age);
    }

    @Override
    public void hello(String name) {
        System.out.println("hello " +name);
    }
}
