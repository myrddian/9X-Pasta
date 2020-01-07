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
import protocol.messages.*;

import java.util.*;

public class ClientConnection implements GelatoConnection {

    public ClientConnection(GelatoConfigImpl config, Gelato libConfig) {
        transport = TransportFactory.createClientTransport(libConfig, config);
    }

    @Override
    public Message getMessage() {
        return transport.readMessage();
    }

    @Override
    public Message getMessage(GelatoFileDescriptor fileDescriptor) {
        return null;
    }


    @Override
    public Gelato.MODE getMode() {
        return Gelato.MODE.CLIENT;
    }

    @Override
    public List<GelatoFileDescriptor> getConnections() {
        return null;
    }

    @Override
    public int getMessageCount(GelatoFileDescriptor fileDescriptor) {
        return 0;
    }

    @Override
    public int getMessageCount() {
        return transport.size();
    }

    @Override
    public int connections() {
        return 0;
    }

    @Override
    public void sendMessage(GelatoFileDescriptor descriptor, Message msg) {
        sendMessage(msg);
    }

    @Override
    public void sendMessage(Message outbound) {
        transport.writeMessage(outbound);
    }

    @Override
    public void shutdown() {
        return;
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void begin() {

    }

    @Override
    public void closeConnection(GelatoFileDescriptor descriptor) {
        shutdown();
    }

    private GelatoTransport transport;
}
