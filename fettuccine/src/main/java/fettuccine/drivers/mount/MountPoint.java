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

package fettuccine.drivers.mount;

import fettuccine.drivers.MountService;
import gelato.GelatoFileDescriptor;
import gelato.client.GelatoMessage;
import gelato.client.GelatoMessaging;
import gelato.client.file.GelatoDirectory;
import gelato.client.file.GelatoFile;
import gelato.client.file.GelatoFileManager;
import gelato.client.transport.MessageProxy;
import gelato.server.manager.RequestConnection;
import gelato.server.manager.controllers.GelatoDirectoryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.messages.Message;
import protocol.messages.request.ReadRequest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MountPoint implements MessageProxy {

    private GelatoMessaging connection;
    private Map<Integer, GelatoMessageProxy> proxyMap = new ConcurrentHashMap<>();

    public GelatoFileManager getMountPoint() {
        return mountPoint;
    }

    private GelatoFileManager mountPoint;
    private MountService mountService;
    private MountDirectory directoryController;
    private final Logger logger = LoggerFactory.getLogger(MountPoint.class);
    private String mountDetail = "";

    public MountPoint(String host, int port, String userName, String mountpoint, MountService service) throws IOException {
        mountPoint = new GelatoFileManager(host,port,userName,userName);
        connection = mountPoint.getConnection();
        directoryController = new MountDirectory(service.getServerManager(), this, mountPoint.getRoot(), mountpoint);
        directoryController.setDirectoryName(mountpoint);
        GelatoFileDescriptor desc = service.getServerManager().getDescriptorManager().generateDescriptor();
        desc.getQid().setLongFileId(desc.getDescriptorId());
        directoryController.setFileDescriptor(desc);
        mountDetail = mountpoint+" -> /"+host+":"+port;
        mountService = service;
        mapMount();
    }

    public String getMountDetail() {
        return mountDetail;
    }

    private void mapArtefact(RemoteResource remoteResource) {
        GelatoFileDescriptor descriptor = mountService.getServerManager().getDescriptorManager().generateDescriptor();
        descriptor.getQid().setLongFileId(descriptor.getDescriptorId());
        remoteResource.setFileDescriptor(descriptor);
        mountService.getServerManager().addResource(remoteResource);
    }

    private void mapMount() {
        logger.info("Mounting to " + directoryController.getDirectoryName());
        GelatoDirectory rootOfMount = mountPoint.getRoot();
        logger.debug("Starting file system tree scan");

        for(GelatoDirectory dir: rootOfMount.getDirectories()) {
            RemoteResource newResource = new RemoteResource(this, dir);
            directoryController.addArtefact(newResource);
            mapArtefact(newResource);
            mapDirectory(dir, newResource);
        }

        for(GelatoFile file: rootOfMount.getFiles()) {
            RemoteResource newResource = new RemoteResource(this, file);
            directoryController.addArtefact(newResource);
            mapArtefact(newResource);
        }
    }

    private void mapDirectory(GelatoDirectory directory, RemoteResource parent) {
        logger.debug("Scanning directory " + directory.getName());
        for(GelatoDirectory directoryEntries: directory.getDirectories()) {
            RemoteResource newResource = new RemoteResource(this , directoryEntries);
            parent.addArtefact(newResource);
            mapArtefact(newResource);
            mapDirectory(directoryEntries, newResource);
        }

        for(GelatoFile file: directory.getFiles()) {
            RemoteResource newResource = new RemoteResource(this, file);
            parent.addArtefact(newResource);
            mapArtefact(newResource);
        }
    }

    public GelatoDirectoryController getDirectory() {
        return directoryController;
    }

    @Override
    public void processMessage(GelatoMessage gelatoMessage, Message message, GelatoMessaging connection) {
        GelatoMessageProxy backToSource = proxyMap.get(gelatoMessage.getProxyId());
        message.tag = gelatoMessage.getProxyId();
        backToSource.getRequestConnection().reply(message);
        int counter = backToSource.getQueueSize();
        if(counter <= 0 ) {
            proxyMap.remove(gelatoMessage.getProxyId());
            connection.close(gelatoMessage);
        } else {
            --counter;
            backToSource.setQueueSize(counter);
        }
    }

    public void sendProxyMessage(Message message, RequestConnection connection) {
        GelatoMessage newFuture = new GelatoMessage(message);
        newFuture.setProxy(this);
        GelatoMessageProxy proxiedMesg = new GelatoMessageProxy();
        proxiedMesg.setForwardedMessage(newFuture);
        proxiedMesg.setOriginalTransactionId(message.getTag());
        proxiedMesg.setRequestConnection(connection);
        proxiedMesg.setQueueSize(1);
        newFuture.setProxyId(message.tag);
        //Estimate blocks
        if(message.messageType == P9Protocol.TREAD) {
            ReadRequest readRequest = Decoder.decodeReadRequest(message);
            double numBlocks =  readRequest.getBytesToRead() / this.connection.getIoSize();
            proxiedMesg.setQueueSize((int)Math.ceil(numBlocks));
        }
        proxyMap.put(message.tag, proxiedMesg);
        this.connection.submitMessage(newFuture);
    }

    public GelatoMessaging getConnection() {
        return connection;
    }

    public void setConnection(GelatoMessaging connection) {
        this.connection = connection;
    }

    public static String generateId(GelatoMessageProxy msg) {
        return Integer.toString(msg.getOriginalTransactionId())+":"+Long.toString(msg.getConnectionDescriptor());
    }
}
