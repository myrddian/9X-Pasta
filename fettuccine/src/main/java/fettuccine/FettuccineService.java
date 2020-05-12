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

package fettuccine;

import fettuccine.drivers.Root;
import fettuccine.drivers.proc.ProcDriver;
import fettuccine.sys.SysDriver;
import gelato.Gelato;
import gelato.server.GelatoServerConnection;
import gelato.server.GelatoServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FettuccineService {

  private final Logger logger = LoggerFactory.getLogger(FettuccineService.class);
  private FettuccineConfig config;
  private GelatoServerManager serveletManager;
  private ProcDriver procDriver;
  private Root rootDir;
  private SysDriver systemApiDriver;

  public FettuccineService() {
    logger.info("Fettuccine - Interposer/Root VFS - Service is Initialising");
    config = new FettuccineConfig();
  }

  public static void main(String[] args) {
    FettuccineService service = new FettuccineService();
    service.init();
  }

  public void hold() {
    serveletManager.hold();
  }

  public void init() {
    logger.info("Fettuccine - Interposer/Root VFS - Service is Configuring");
    config.loadDefaultConfig();
    serveletManager = new GelatoServerManager(config.getPort());
    rootDir = new Root(serveletManager);
    procDriver = new ProcDriver(serveletManager);
    serveletManager.start();
    rootDir.addDirectory(procDriver.getProcDir());
    serveletManager.setRootDirectory(rootDir);
    systemApiDriver = new SysDriver(serveletManager);
  }
}
