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
import gelato.client.GelatoMessage;
import gelato.client.GelatoMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.messages.Message;
import protocol.messages.MessageRaw;
import protocol.messages.response.ErrorMessage;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.server.ExportException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSideTcpInputReader {

    private InputStream netWorkInputStream;
    private Map<Integer, GelatoMessage> replyBucket = new ConcurrentHashMap<>();
    private byte[] minHeaderBuffer = new byte[MessageRaw.minSize];
    private boolean shutdown = false;
    private final Logger logger = LoggerFactory.getLogger(ClientSideTcpInputReader.class);
    private GelatoMessaging messaging;

    public static final String INCORRECT_HEADER = "Incorrect header";
    public static final String INVALID_BYTE_COUNT = "Number of bytes read from stream does not match required amount";

    public void addFuture(GelatoMessage future) {
        replyBucket.put(future.getTag(),future);
    }
    public void closeFuture(GelatoMessage future) { replyBucket.remove(future.getTag()); }


    public ClientSideTcpInputReader(InputStream inputStream, GelatoMessaging broker) {
        netWorkInputStream = inputStream;
        messaging = broker;
    }

    public void processMessages() throws IOException {
        Message message = getMessage();
        GelatoMessage future = replyBucket.get(message.tag);
        if(future == null) {
            logger.error("No future found for message");
            return;
        }
        if(future.isComplete()) {
            replyBucket.remove(message.tag);
            determineError(message);
        } else {
            MessageCompletion completion = new MessageCompletion();
            completion.setFuture(future);
            completion.setMessage(message);
            messaging.addMessageToProcess(completion);
        }

    }


    public void determineError(Message message) {
        if(message.messageType == P9Protocol.RERROR) {
            ErrorMessage errorMessage = Decoder.decodeError(message);
            logger.error("Reply from server was an Error message but the transaction is completed - reply from server below");
            logger.error(errorMessage.getErrorMessage());
            throw new RuntimeException("Future Transaction not completed - Error not Handled");
        }
    }

    public Message getMessage() throws IOException {

        for (int byteCount = 0; byteCount < MessageRaw.minSize; ++byteCount) {
            int val = netWorkInputStream.read();
            if (val != -1) {
                minHeaderBuffer[byteCount] = (byte) (val & 0xFF);
            } else {
                logger.error("Unable to read header");
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
            throw new ExportException(INVALID_BYTE_COUNT);
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
    public void process() throws IOException, InterruptedException{
        while (!isShutdown()) {
            processMessages();
        }
    }


}
