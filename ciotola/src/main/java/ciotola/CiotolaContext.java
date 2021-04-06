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

import ciotola.actor.CiotolaDirector;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

public interface CiotolaContext {

  Collection<CiotolaServiceInterface> getServices();

  void addService(CiotolaServiceInterface newService);

  void addService(Object newService);

  void removeService(int serviceId);

  int injectService(CiotolaServiceInterface newService);

  int injectService(CiotolaServiceInterface newService, boolean skipInjection);

  int injectService(Object newService, boolean skipInjection);

  int injectService(Object newService);

  void injectDependencies(Object newService);

  void addDependency(Object dependency);

  void addDependency(Class name, Object wire);

  boolean startContainer();

  List<String> getLoadedJars();

  void addAnnotation(Class annotation);

  List<String> getAnnotations();

  void submitJob(Runnable job);

  ExecutorService getExecutorService();

  void setExecutorService(ExecutorService executorService);

  int threadCapacity();

  long getConnectionTimeOut();

  void setConnectionTimeOut(long connectionTimeOut);

  void execute(Runnable job, long key);

  void execute(CiotolaConnectionService connectionService);

  void stop();

  CiotolaDirector getDirector();
}
