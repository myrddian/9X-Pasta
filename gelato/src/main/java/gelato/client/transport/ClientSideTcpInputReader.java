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

package gelato.client.transport;

import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import gelato.client.GelatoMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Decoder;
import protocol.messages.Message;
import protocol.messages.MessageRaw;

import java.io.IOException;
import java.io.InputStream;

public class ClientSideTcpInputReader {

    private InputStream netWorkInputStream;
    private byte[] minHeaderBuffer = new byte[MessageRaw.minSize];
    private boolean shutdown = false;
    private final Logger logger = LoggerFactory.getLogger(ClientSideTcpInputReader.class);
    private GelatoMessaging messaging;

    public static final String INCORRECT_HEADER = "Incorrect header";
    public static final String INVALID_BYTE_COUNT = "Number of bytes read from stream does not match required amount";


    public ClientSideTcpInputReader(InputStream inputStream, GelatoMessaging broker) {
        netWorkInputStream = inputStream;
        messaging = broker;
    }

    public void processMessages() throws IOException {
        Message message = getMessage();
        messaging.addMessageToProcess(message);
    }


    public Message getMessage() throws IOException {

        for (int byteCount = 0; byteCount < MessageRaw.minSize; ++byteCount) {
            int val = netWorkInputStream.read();
            if (val != -1) {
                minHeaderBuffer[byteCount] = (byte) (val & 0xFF);
            } else {
                logger.error(INCORRECT_HEADER);
                shutdown();
                throw new IOException(INCORRECT_HEADER);
            }
        }
        MessageRaw minMessage = Decoder.decodeRawHeader(minHeaderBuffer);
        Message msg = minMessage.toMessage();
        int bytesToRead = msg.getContentSize();
        byte[] content = new byte[bytesToRead];
        int rsize = netWorkInputStream.read(content);
        if(rsize != bytesToRead) {
            logger.error(INVALID_BYTE_COUNT);
            shutdown();
            throw new IOException(INVALID_BYTE_COUNT);
        }
        msg.messageContent = content;
        return msg;
    }


    @CiotolaServiceStop
    public synchronized void shutdown() {
        shutdown = true;
    }

    @CiotolaServiceStart
    public synchronized void start() {
        shutdown = false;
    }

    public synchronized boolean isShutdown() {
        return shutdown;
    }

    @CiotolaServiceRun
    public void process() throws IOException{
        while (!isShutdown()) {
            processMessages();
        }
    }


}
