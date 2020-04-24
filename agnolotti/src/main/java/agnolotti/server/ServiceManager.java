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

package agnolotti.server;

import agnolotti.Agnolotti;
import gelato.Gelato;
import gelato.server.GelatoServerConnection;
import gelato.server.GelatoServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceManager {



    private String version = Agnolotti.DEFAULT_VER;
    private String nameSpace = Agnolotti.DEFAULT_NAME;
    private String userName = Agnolotti.DEFAULT_NAME;
    private String userGroup = Agnolotti.DEFAULT_NAME;
    private int port;
    private Gelato gelato;
    private GelatoServerConnection serverConnection;
    private GelatoServerManager serveletManager;
    private final Logger logger = LoggerFactory.getLogger(ServiceManager.class);
    private RemoteServiceFactory factory;

    private ServiceGenericDirectory root;
    private ServiceGenericDirectory serviceName;
    private ServiceGenericDirectory serviceVersion;

    public ServiceManager(String newVersion,
                          String newName,
                          int port,
                          String serviceUserName,
                          String serviceUserGroup) {
        version = newVersion;
        nameSpace = newName;
        logger.info("Agnolotti - Remote RPC Server - Service is Initialising");
        gelato = new Gelato();
        this.port = port;
        userName = serviceUserName;
        userGroup = serviceUserGroup;
        serverConnection = new GelatoServerConnection(gelato,port);
        serveletManager = new GelatoServerManager(serverConnection, gelato);
        root = new ServiceGenericDirectory(userName, userGroup, Agnolotti.ROOT_ID, Agnolotti.ROOT_NAME);
        serviceName = new ServiceGenericDirectory(userName,userGroup, Agnolotti.NAME_ID, nameSpace);
        serviceVersion = new ServiceGenericDirectory(userName, userGroup, Agnolotti.VER_ID, version);
        root.addDirectory(serviceName);
        serviceName.addDirectory(serviceVersion);
        serveletManager.start();
        serveletManager.setRootDirectory(root);
        serveletManager.addResource(serviceName);
        serveletManager.addResource(serviceVersion);
        factory = new RemoteServiceFactory(gelato.getDescriptorManager(), serveletManager);
    }


    public void startService()  {
        serveletManager.hold();
    }

    public RemoteServiceFactory getServiceFactory() {
        return factory;
    }

    public void addRemoteService(Class remoteInterface, Object service) {
        RemoteServiceProxyDirectory proxyDirectory = factory.generateRpc(remoteInterface, service);
        serviceVersion.addDirectory(proxyDirectory);
        serveletManager.addResource(proxyDirectory);
    }



}
