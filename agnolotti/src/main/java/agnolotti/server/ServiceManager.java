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
import gelato.server.manager.controllers.GelatoDirectoryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceManager {



    private String version = Agnolotti.DEFAULT_VER;
    private String nameSpace = Agnolotti.DEFAULT_NAME;
    private String userName = Agnolotti.DEFAULT_NAME;
    private String userGroup = Agnolotti.DEFAULT_NAME;
    private int port;
    private GelatoServerConnection serverConnection;
    private GelatoServerManager serveletManager;
    private final Logger logger = LoggerFactory.getLogger(ServiceManager.class);
    private RemoteServiceFactory factory;

    private GelatoDirectoryController root;
    private GelatoDirectoryController serviceName;
    private GelatoDirectoryController serviceVersion;


    public ServiceManager(GelatoServerManager manager, String serviceName,
                          String userName,
                          String version) {
        this(manager, manager.getConnection(), manager.getRoot(),serviceName,userName,version);
    }

    public ServiceManager(GelatoServerManager manager,
                          GelatoServerConnection connection,
                          GelatoDirectoryController location,
                          String serviceName,
                          String userName,
                          String version) {
        serveletManager = manager;
        serverConnection = connection;
        this.version = version;
        this.userName = userName;
        this.userGroup = userName;
        nameSpace = serviceName;
        root = location;
        this.serviceName = new ServiceGenericDirectory(userName,userGroup,
                serveletManager.getDescriptorManager().generateDescriptor().getDescriptorId(),serviceName, serveletManager);
        this.serviceVersion =  new ServiceGenericDirectory(userName,userGroup,
                serveletManager.getDescriptorManager().generateDescriptor().getDescriptorId(),version,serveletManager);
        this.serviceName.addDirectory(this.serviceVersion);
        root.addDirectory(this.serviceName);
        logger.info("Agnolotti - Remote RPC Server - Service is Initialising");
        factory = new RemoteServiceFactory(serveletManager);
    }


    public ServiceManager(String newVersion,
                          String newName,
                          int port,
                          String serviceUserName,
                          String serviceUserGroup) {
        version = newVersion;
        nameSpace = newName;
        logger.info("Agnolotti - Remote RPC Server - Service is Initialising");
        this.port = port;
        userName = serviceUserName;
        userGroup = serviceUserGroup;
        serveletManager = new GelatoServerManager(port);
        root = new ServiceGenericDirectory(userName, userGroup, Agnolotti.ROOT_ID, Agnolotti.ROOT_NAME,serveletManager);
        serviceName = new ServiceGenericDirectory(userName,userGroup, Agnolotti.NAME_ID, nameSpace,serveletManager);
        serviceVersion = new ServiceGenericDirectory(userName, userGroup, Agnolotti.VER_ID, version,serveletManager);
        root.addDirectory(serviceName);
        serviceName.addDirectory(serviceVersion);
        serveletManager.start();
        serveletManager.setRootDirectory(root);
        factory = new RemoteServiceFactory(serveletManager);
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
    }



}
