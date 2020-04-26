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

import ciotola.CiotolaContext;
import ciotola.annotations.CiotolaAutowire;
import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import gelato.Gelato;
import gelato.GelatoConfigImpl;
import gelato.GelatoConnection;
import gelato.GelatoDescriptorManager;
import gelato.GelatoFileDescriptor;
import gelato.server.manager.v2.V2TCPTransport;
import gelato.transport.GelatoTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.messages.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GelatoServerConnection implements GelatoConnection {

  private final Logger logger = LoggerFactory.getLogger(GelatoServerConnection.class);
  private int portNumber = 7073;
  private ServerSocket serverSocket;
  private GelatoDescriptorManager descriptorManager;
  private Map<GelatoFileDescriptor, GelatoTransport> connections = new ConcurrentHashMap<>();
  private boolean shutdown = false;
  private boolean started = false;
  private MODE transportMode = MODE.V2_API;
  @CiotolaAutowire private CiotolaContext context;

  public GelatoServerConnection(Gelato library, GelatoConfigImpl config) {
    portNumber = config.getPortNumber();
    descriptorManager = library.getDescriptorManager();
    logger.debug("Starting Server on port: " + Integer.toString(portNumber));
    try {
      serverSocket = new ServerSocket(portNumber);
      logger.debug("Server Started listening");

    } catch (IOException e) {
      logger.error("Unable to Start server", e);
      throw new RuntimeException("Unable to start server");
    }
  }

  public GelatoServerConnection(Gelato library, int portNumber) {
    descriptorManager = library.getDescriptorManager();
    logger.debug("Starting Server on port: " + Integer.toString(portNumber));
    try {
      serverSocket = new ServerSocket(portNumber);
      logger.debug("Server Started listening");

    } catch (IOException e) {
      logger.error("Unable to Start server", e);
      throw new RuntimeException("Unable to start server");
    }
  }

  public void startServer() {
    started = true;
  }

  @Override
  public boolean isStarted() {
    return started;
  }

  @CiotolaServiceStart
  @Override
  public void begin() {
    this.startServer();
  }

  @Override
  public void closeConnection(GelatoFileDescriptor descriptor) {
    if (connections.containsKey(descriptor)) {
      GelatoTransport transport = connections.get(descriptor);
      transport.close();
      connections.remove(descriptor);
    }
  }

  @CiotolaServiceRun
  public void run() {
    logger.debug("Server Bound and waiting Connections");
    if (transportMode == MODE.V1_SELECT) {
      oldV1Api();
    } else if (transportMode == MODE.V2_API || transportMode == MODE.API_LATEST) {
      processMessages();
    }
  }

  private void processMessages() {
    while (!shutdown) {
      try {
        Socket clientSocket = serverSocket.accept();
        GelatoFileDescriptor fileDescriptor = descriptorManager.generateDescriptor();
        logger.debug(
            "Connected Client - File Descriptor: "
                + Long.toString(fileDescriptor.getDescriptorId()));
        V2TCPTransport tcpTransport = new V2TCPTransport(clientSocket, fileDescriptor);
        context.injectDependencies(tcpTransport);
        context.execute(tcpTransport);
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

  @CiotolaServiceStop
  @Override
  public synchronized void shutdown() {
    shutdown = true;
    logger.debug("Server Shutting Down");
  }

  @Override
  public Message getMessage() {
    logger.error("Method not supported (GET-MESSAGE) - on server you must specify a Descriptor");
    throw new RuntimeException("Invalid Operation");
  }

  private void oldV1Api() {
    throw new RuntimeException("Model not supported");
  }

  public enum MODE {
    V1_SELECT,
    V2_API,
    API_LATEST
  }
}
