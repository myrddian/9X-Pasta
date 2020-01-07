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
import org.slf4j.*;

public class FettuccineConfig {
    final Logger logger = LoggerFactory.getLogger(FettuccineConfig.class);
    public static String LOCALHOST = "localhost";
    private int port = 9090;

    public int getPort() { return port; }
    public void loadDefaultConfig() {
        logger.info("Loading Configuration");
        logger.info("Listening at port: " + Integer.toString(port));
    }
    public void loadConfigAtPath(String path) {};
    public GelatoConfigImpl generateConfig() {
        GelatoConfigImpl config = new GelatoConfigImpl();
        config.setPort(port);
        config.setHost(LOCALHOST);
        return  config;
    }
}