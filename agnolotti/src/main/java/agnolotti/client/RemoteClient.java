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

import ciotola.Ciotola;
import gelato.Gelato;
import gelato.GelatoConfigImpl;
import gelato.GelatoConnection;
import gelato.client.file.GelatoFileManager;

import java.io.IOException;

public class RemoteClient {

    private GelatoConnection connection;
    private GelatoFileManager fileManager;
    private Gelato gelato;
    private String serviceName;
    private String serviceVersion;
    private RemoteFactory remoteFactory;

    public RemoteClient(String server,
                        int port,
                        String serviceName,
                        String version,
                        String user) {


        this.serviceName = serviceName;
        this.serviceVersion = version;
        try {
            fileManager = new GelatoFileManager(server,port, user, user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        remoteFactory = new RemoteFactory(serviceName,version, fileManager.getRoot());
    }


    public Object getRemoteService(Class remoteInterface) {
        return remoteFactory.generateRemoteService(remoteInterface);
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


    public String getServiceName() {
        return serviceName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }


    public void stop() {
        Ciotola.getInstance().stop();
    }

}
