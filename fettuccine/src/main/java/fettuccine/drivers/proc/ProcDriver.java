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

package fettuccine.drivers.proc;

import ciotola.Ciotola;
import gelato.server.GelatoServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProcDriver  {

  private final Logger logger = LoggerFactory.getLogger(ProcDriver.class);
  private ProcDir procDir;
  private GelatoServerManager serveletManager;


  public ProcDriver(GelatoServerManager gelatoServerManager) {
    serveletManager = gelatoServerManager;
    procDir = new ProcDir(serveletManager);
    Ciotola.getInstance().injectService(procDir);
  }






  public ProcDir getProcDir() {
    return procDir;
  }
}
