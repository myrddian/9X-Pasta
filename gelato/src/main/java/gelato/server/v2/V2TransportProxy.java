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

package gelato.server.v2;

import gelato.Gelato;
import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import protocol.messages.Message;

import java.util.List;

public class V2TransportProxy implements GelatoConnection {
    public V2TCPTransport getTransport() {
        return transport;
    }

    public void setTransport(V2TCPTransport transport) {
        this.transport = transport;
    }

    private V2TCPTransport transport;

    public V2TransportProxy(V2TCPTransport tcpTransport) {
        transport = tcpTransport;
    }

    @Override
    public Message getMessage() {
        return null;
    }

    @Override
    public Message getMessage(GelatoFileDescriptor fileDescriptor) {
        return null;
    }

    @Override
    public Gelato.MODE getMode() {
        return null;
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
        return 0;
    }

    @Override
    public int connections() {
        return 0;
    }

    @Override
    public void sendMessage(GelatoFileDescriptor descriptor, Message msg) {
        transport.writeMessage(msg);
    }

    @Override
    public void sendMessage(Message msg) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void begin() {

    }

    @Override
    public void closeConnection(GelatoFileDescriptor descriptor) {

    }
}
