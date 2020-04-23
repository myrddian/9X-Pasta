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

package agnolotti.client;

import gelato.Gelato;
import gelato.GelatoConfigImpl;
import gelato.GelatoConnection;
import gelato.client.file.GelatoFileManager;

public class RemoteClient {

    private GelatoConnection connection;
    private GelatoFileManager fileManager;
    private Gelato gelato;

    public RemoteClient(String server,
                        int port,
                        String user) {

        gelato = new Gelato();
        GelatoConfigImpl config = new GelatoConfigImpl();
        config.setHost(server);
        config.setPort(port);
        connection = gelato.createClientConnection(config);
        if (connection == null) {
            throw new RuntimeException("Cannot connect");
        }
        fileManager = new GelatoFileManager(connection, gelato, user, user);
    }


    public RemoteFactory getFactory(Class remoteInterface) {
        return new RemoteFactory();
    }

    public GelatoConnection getConnection() {
        return connection;
    }

    public void setConnection(GelatoConnection connection) {
        this.connection = connection;
    }

    public GelatoFileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(GelatoFileManager fileManager) {
        this.fileManager = fileManager;
    }

    public Gelato getGelato() {
        return gelato;
    }

    public void setGelato(Gelato gelato) {
        this.gelato = gelato;
    }


}
