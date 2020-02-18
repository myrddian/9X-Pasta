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
import fettuccine.drivers.*;
import fettuccine.drivers.proc.*;
import gelato.*;
import gelato.server.*;
import gelato.server.manager.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FettuccineService {

    public static final String FETTUCCINE_SVC_NAME = "fettuccine_svc";
    public static final String FETTUCCINE_SVC_GRP = "fettuccine_svc";

    private final Logger logger = LoggerFactory.getLogger(FettuccineService.class);
    private Gelato gelato;
    private FettuccineConfig config;
    private GelatoServerConnection serverConnection;
    private GelatoFileServeletManager serveletManager;
    private ProcDriver procDriver;
    private Root rootDir = new Root();

    public void init() {
        logger.info("Fettuccine - Interposer/Root VFS - Service is Configuring");
        logger.info(FettuccineVersion.getVersion());
        config.loadDefaultConfig();
        serverConnection = new GelatoServerConnection(gelato, config.generateConfig());
        serveletManager = new GelatoFileServeletManager(serverConnection, gelato);
        procDriver = new ProcDriver(serveletManager);
        serveletManager.start();
        rootDir.addDirectory(procDriver.getProcDir());
        serveletManager.setRootDirectory(rootDir);
    }

    public FettuccineService() {
        logger.info("Fettuccine - Interposer/Root VFS - Service is Initialising");
        gelato = new Gelato();
        config = new FettuccineConfig();
    }

    public static void main(String[] args) {
        FettuccineService service = new FettuccineService();
        service.init();
    }


}
