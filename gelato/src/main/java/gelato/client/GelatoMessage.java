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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.P9Protocol;
import protocol.messages.Message;
import protocol.messages.TransactionMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GelatoMessage<M,R>  implements TransactionMessage, Iterator, Iterable {

    private M originalMessage;
    private List<R> responseMessage = new ArrayList<>();
    private TransactionMessage transactionMessage;
    private boolean transactionComplete = false;
    private boolean error = false;
    private final Logger logger = LoggerFactory.getLogger(GelatoMessage.class);
    private String errorMessage;
    private int location = 0;
    private BlockingQueue<R> future = new LinkedBlockingQueue<>();
    private Message rawReplyMessage;
    private R futureMessage;

    private void takeMessage() {
        if(!isComplete()) {
            try {
                futureMessage = future.take();
                setCompleted();
            } catch (InterruptedException e) {
                logger.error("Problem fetching future");
            }
        }
    }

    public GelatoMessage(M message) {
        originalMessage = message;
        transactionMessage = (TransactionMessage)message;
    }

    public R getResponse() {
       takeMessage();
       return futureMessage;
    }

    public M getMessage() {
        return originalMessage;
    }

    public void setFuture(R futureMessage) {
        future.add(futureMessage);
        setResponseMessage(futureMessage);
    }

    public synchronized int size() {
        return responseMessage.size();
    }

    public synchronized void setResponseMessage(R message) {
        responseMessage.add(message);
    }

    public synchronized boolean isError() {
        return error;
    }

    public synchronized boolean isComplete() {
        return transactionComplete;
    }

    public synchronized void setError() {
        error = true;
    }

    public synchronized void setCompleted() {
        transactionComplete = true;
    }

    public synchronized void setErrorMessage(String error) {
        errorMessage = error;
    }

    public synchronized String getErrorMessage() {
        return errorMessage;
    }

    public synchronized Message getRawReplyMessage() {
        return rawReplyMessage;
    }

    public synchronized void setRawReplyMessage(Message rawReplyMessage) {
        this.rawReplyMessage = rawReplyMessage;
    }

    //Simple handler functions to make it nicer to work with.

    @Override
    public void setTransactionId(int transactionId) {
        transactionMessage.setTransactionId(transactionId);
    }

    @Override
    public int getTag() {
        return transactionMessage.getTag();
    }

    @Override
    public void setTag(int newTag) {
        transactionMessage.setTag(newTag);
    }

    @Override
    public byte messageType() {
        return transactionMessage.messageType();
    }

    @Override
    public Message toMessage() {
        return transactionMessage.toMessage();
    }

    @Override
    public synchronized boolean hasNext() {
        if(messageType() != P9Protocol.RREAD) {
            throw new RuntimeException("Invalid operation on Non READ message type");
        }
        return(location < responseMessage.size());
    }

    @Override
    public synchronized R next() {
        if(messageType() != P9Protocol.RREAD) {
            throw new RuntimeException("Invalid operation on Non READ message type");
        }
        int ptr = location;
        location++;
        return responseMessage.get(ptr);
    }

    @Override
    public Iterator iterator() {
        if(messageType() != P9Protocol.RREAD) {
            throw new RuntimeException("Invalid operation on Non READ message type");
        }
        takeMessage();
        location = 0;
        return this;
    }
}
