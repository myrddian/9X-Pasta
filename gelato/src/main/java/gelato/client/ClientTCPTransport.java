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

import gelato.*;
import gelato.transport.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;

public class ClientTCPTransport extends TCPTransport {

    private Socket clientSocket;
    final Logger logger = LoggerFactory.getLogger(TCPTransport.class);

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
    public InputStream getSocketInputStream()  {
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
        return  null;
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
