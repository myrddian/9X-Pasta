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

package gelato.server;

import gelato.*;
import gelato.client.*;
import gelato.transport.*;
import org.slf4j.*;
import protocol.*;
import protocol.messages.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class GelatoServerConnection extends Thread implements GelatoConnection {

    final Logger logger = LoggerFactory.getLogger(GelatoServerConnection.class);
    private int portNumber = 7073;
    private ServerSocket serverSocket;
    private GelatoDescriptorManager descriptorManager;
    private Map<GelatoFileDescriptor, GelatoTransport> connections = new HashMap<>();
    private boolean shutdown = false;
    private Gelato libraryReference;
    private boolean started = false;

    public GelatoServerConnection(Gelato library, GelatoConfigImpl config) {
        portNumber = config.getPortNumber();
        descriptorManager = library.getDescriptorManager();
        libraryReference = library;
        logger.info("Starting Server on port: " + Integer.toString(portNumber));
        try {
            serverSocket = new ServerSocket(portNumber);
            logger.info("Server Started listening");

        } catch (IOException e) {
            logger.error("Unable to Start server", e);
            throw new RuntimeException("Unable to start server");
        }
    }


    public void startServer() {

        this.start();
        started = true;
    }

    @Override
    public boolean isStarted() { return started; }

    @Override
    public void begin() {
        this.startServer();
    }

    @Override
    public void closeConnection(GelatoFileDescriptor descriptor) {
        if(connections.containsKey(descriptor)) {
            GelatoTransport transport = connections.get(descriptor);
            transport.close();
            connections.remove(descriptor);
        }
    }

    @Override
    public void run(){
        logger.info("Server Bound and waiting Connections");
        while(!shutdown) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientTCPTransport tcpTransport = new ClientTCPTransport(clientSocket);
                GelatoFileDescriptor fileDescriptor = descriptorManager.generateDescriptor();
                synchronized (this) {
                    logger.info("Connected Client - File Descriptor: " + Long.toString(fileDescriptor.getDescriptorId()));
                    connections.put(fileDescriptor, tcpTransport);
                    libraryReference.getExecutorService().submit(tcpTransport);
                }
            } catch (IOException e) {
                logger.error("Unable to handle connections ", e);
            }
        }
    }

    @Override
    public Message getMessage(GelatoFileDescriptor fileDescriptor) {
        return connections.get(fileDescriptor).readMessage();
    }

    @Override
    public Gelato.MODE getMode() {
        return Gelato.MODE.SERVER;
    }

    @Override
    public List<GelatoFileDescriptor> getConnections() {
        return new ArrayList<>(connections.keySet());
    }

    @Override
    public int getMessageCount(GelatoFileDescriptor fileDescriptor) {
        return connections.get(fileDescriptor).size();
    }

    @Override
    public int connections() {
        return connections.size();
    }

    @Override
    public void sendMessage(GelatoFileDescriptor descriptor, Message msg) {
        connections.get(descriptor).writeMessage(msg);
    }

    @Override
    public int getMessageCount() {
        return 0;
    }

    @Override
    public void sendMessage(Message msg) {
        logger.error("Method not supported (SEND-MESSAGE) - on server you must specify a Descriptor");
        throw new RuntimeException("Invalid Operation");
    }

    @Override
    public synchronized void shutdown() {
        shutdown = true;
        logger.info("Server Shutting Down");
    }

    @Override
    public Message getMessage() {
        logger.error("Method not supported (GET-MESSAGE) - on server you must specify a Descriptor");
        throw new RuntimeException("Invalid Operation");
    }

}
