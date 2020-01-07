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
import gelato.*;
import gelato.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FettuccineService {
    final Logger logger = LoggerFactory.getLogger(FettuccineService.class);
    private Gelato gelato;
    private FettuccineConfig config;
    private GelatoServerConnection serverConnection;

    public void init() {
        logger.info("Fettuccine - Interposer/Root VFS - Service is Configuring");
        logger.info(FettuccineVersion.getVersion());
        config.loadDefaultConfig();
        serverConnection = new GelatoServerConnection(gelato, config.generateConfig());
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
