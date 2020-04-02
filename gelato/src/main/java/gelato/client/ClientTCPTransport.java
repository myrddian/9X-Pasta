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

package gelato.client;

import gelato.GelatoConfigImpl;
import gelato.transport.TCPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientTCPTransport extends TCPTransport {

  final Logger logger = LoggerFactory.getLogger(TCPTransport.class);
  private Socket clientSocket;

  public ClientTCPTransport(Socket cliSocket) {
    clientSocket = cliSocket;
  }

  public ClientTCPTransport(GelatoConfigImpl configuration) {
    try {
      clientSocket = new Socket(configuration.getHostName(), configuration.getPortNumber());
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public InputStream getSocketInputStream() {
    try {
      return clientSocket.getInputStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public OutputStream getSocketOutputStream() {
    try {
      return clientSocket.getOutputStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void closeStream() {
    try {
      clientSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
