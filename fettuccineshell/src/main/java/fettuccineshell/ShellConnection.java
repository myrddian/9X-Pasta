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

package fettuccineshell;
import gelato.*;
import gelato.client.file.*;
import org.springframework.stereotype.Service;


@Service
public class ShellConnection {

    private Gelato gelato;
    private GelatoConnection client;

    public GelatoFileManager getFileManager() {
        return fileManager;
    }

    private GelatoFileManager fileManager;
    private GelatoConfigImpl config;

    public boolean isConnected() {
        return isConnected;
    }

    private boolean isConnected = false;

    public boolean connect(String hostName, int port, String userName) {
        gelato = new Gelato();
        config = new GelatoConfigImpl();
        config.setHost(hostName);
        config.setPort(port);
        client = gelato.createClientConnection(config);
        if(client == null) {
            return false;
        }
        fileManager = new GelatoFileManager(client, gelato, userName, userName);
        isConnected = true;
        return true;
    }

    public String getHost() {
        return config.getHostName();
    }

}
