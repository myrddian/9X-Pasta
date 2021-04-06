/*
 * Copyright (c) 2021.  Enzo Reyes Licensed under the Apache License, Version 2.0 (the "License");   you may
 * not use this file except in compliance with the License.   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package gelato.client;

import ciotola.Ciotola;
import ciotola.annotations.CiotolaServiceRun;
import ciotola.annotations.CiotolaServiceStart;
import ciotola.annotations.CiotolaServiceStop;
import gelato.client.transport.ClientSideOutTcpWrite;
import gelato.client.transport.ClientSideTcpInputReader;
import gelato.client.transport.MessageCompletion;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.messages.Message;
import protocol.messages.VersionRequest;
import protocol.messages.request.AttachRequest;
import protocol.messages.request.AuthRequest;
import protocol.messages.request.CloseRequest;
import protocol.messages.request.CreateRequest;
import protocol.messages.request.FlushRequest;
import protocol.messages.request.OpenRequest;
import protocol.messages.request.ReadRequest;
import protocol.messages.request.RemoveRequest;
import protocol.messages.request.StatRequest;
import protocol.messages.request.WalkRequest;
import protocol.messages.request.WriteRequest;
import protocol.messages.request.WriteStatRequest;
import protocol.messages.response.AttachResponse;
import protocol.messages.response.AuthResponse;
import protocol.messages.response.CloseResponse;
import protocol.messages.response.CreateResponse;
import protocol.messages.response.ErrorMessage;
import protocol.messages.response.FlushResponse;
import protocol.messages.response.OpenResponse;
import protocol.messages.response.ReadResponse;
import protocol.messages.response.RemoveResponse;
import protocol.messages.response.StatResponse;
import protocol.messages.response.WalkResponse;
import protocol.messages.response.WriteResponse;
import protocol.messages.response.WriteStatResponse;

public class GelatoMessaging {

  private final Logger logger = LoggerFactory.getLogger(GelatoMessaging.class);
  private boolean shutdown = false;
  private ClientSideTcpInputReader inputReader;
  private ClientSideOutTcpWrite outputWriter;

  private int ioSize = P9Protocol.DEFAULT_MSG_SIZE;
  private Socket clientSocket;
  private Map<Integer, GelatoMessage> replyBucket = new ConcurrentHashMap<>();

  private BlockingQueue<Message> incomingMessages = new LinkedBlockingQueue<>();

  public GelatoMessaging(String hostName, int portNumber) throws IOException {
    clientSocket = new Socket(hostName, portNumber);
    inputReader = new ClientSideTcpInputReader(clientSocket.getInputStream(), this);
    outputWriter = new ClientSideOutTcpWrite(clientSocket.getOutputStream(), this);
    Ciotola.getInstance().injectService(inputReader);
    Ciotola.getInstance().injectService(outputWriter);
    Ciotola.getInstance().injectService(this);
  }

  private boolean isErrorAndHandle(MessageCompletion newMessage) {
    if (newMessage.getMessage().messageType == P9Protocol.RERROR) {
      String error = Decoder.decodeError(newMessage.getMessage()).getErrorMessage();
      newMessage.getFuture().setError();
      newMessage.getFuture().setErrorMessage(error);
      newMessage.getFuture().setCompleted();
      logger.error("Error in response " + error);
      return true;
    }
    return false;
  }

  private void handleAttach(GelatoMessage future, Message message) {
    AttachResponse response = Decoder.decodeAttachResponse(message);
    future.setFuture(response);
  }

  private void handleRead(GelatoMessage future, Message message, boolean init) {
    ReadResponse response = Decoder.decodeReadResponse(message);
    if (init) {
      future.setFuture(response);
    } else {
      future.setResponseMessage(response);
    }
  }

  private void processFuture(GelatoMessage future, Message message, boolean init) {
    switch (future.messageType()) {
      case P9Protocol.TATTACH:
        handleAttach(future, message);
        break;
      case P9Protocol.TAUTH:
        future.setFuture(Decoder.decodeAuthResponse(message));
        break;
      case P9Protocol.TCLOSE:
        future.setFuture(Decoder.decodeCloseResponse(message));
        break;
      case P9Protocol.TCREATE:
        future.setFuture(Decoder.decodeCreateResponse(message));
        break;
      case P9Protocol.TFLUSH:
        future.setFuture(Decoder.decodeFlushResponse(message));
        break;
      case P9Protocol.TOPEN:
        future.setFuture(Decoder.decodeOpenResponse(message));
        break;
      case P9Protocol.TREAD:
        handleRead(future, message, init);
        break;
      case P9Protocol.TREMOVE:
        future.setFuture(Decoder.decodeRemoveResponse(message));
        break;
      case P9Protocol.TSTAT:
        future.setFuture(Decoder.decodeStatResponse(message));
        break;
      case P9Protocol.TWALK:
        future.setFuture(Decoder.decodeWalkResponse(message));
        break;
      case P9Protocol.TWRITE:
        future.setFuture(Decoder.decodeWriteResponse(message));
        break;
      case P9Protocol.TWSTAT:
        future.setFuture(Decoder.decodeStatWriteResponse(message));
        break;
      case P9Protocol.TVERSION:
        future.setFuture(Decoder.decodeVersionRequest(message));
    }
  }

  private void normalProcess(MessageCompletion futureCompletion) throws InterruptedException {
    if (!isErrorAndHandle(futureCompletion)) {
      if (!futureCompletion.getFuture().isComplete()) {
        futureCompletion.getFuture().setCompleted();
        processFuture(futureCompletion.getFuture(), futureCompletion.getMessage(), true);
      } else {
        processFuture(futureCompletion.getFuture(), futureCompletion.getMessage(), false);
      }
    }
  }

  public void determineError(Message message) {
    if (message.messageType == P9Protocol.RERROR) {
      ErrorMessage errorMessage = Decoder.decodeError(message);
      logger.error(
          "Reply from server was an Error message but the transaction is completed - reply from server below");
      logger.error(errorMessage.getErrorMessage());
      throw new RuntimeException("Future Transaction not completed - Error not Handled");
    }
  }

  private void messageCompletion(Message message) throws InterruptedException {
    GelatoMessage future = replyBucket.get(message.tag);
    if (future == null) {
      logger.error("No future found for message");
      return;
    }

    if (future.isProxy()) {
      future.getProxy().processMessage(future, message, this);
      return;
    }

    if (future.isComplete()) {
      replyBucket.remove(message.tag);
      determineError(message);
    } else {
      MessageCompletion completion = new MessageCompletion();
      completion.setFuture(future);
      completion.setMessage(message);
      normalProcess(completion);
    }

    future.incrementCount();
    if (future.packetsFinalised()) {
      replyBucket.remove(message.tag);
    }
  }

  private void flushAndClose() {
    logger.error("An error has occurred in the connection");
    logger.error("Unable to complete transactions - Connection reset all T-id's are invalid");
    replyBucket.clear();
  }

  private void processMessages() throws InterruptedException {
    if (inputReader.isShutdown() || outputWriter.isShutdown()) {
      flushAndClose();
    }
    messageCompletion(incomingMessages.take());
  }

  public void addFuture(GelatoMessage future) {
    replyBucket.put(future.getTag(), future);
  }

  public void closeFuture(GelatoMessage future) {
    replyBucket.remove(future.getTag());
  }

  public void addMessageToProcess(Message newMessage) {
    incomingMessages.add(newMessage);
  }

  public void close(GelatoMessage message) {
    closeFuture(message);
  }

  public int getTag() {
    return outputWriter.generateTag();
  }

  public void submitMessage(GelatoMessage message) {
    if (message.messageType() == P9Protocol.TREAD) {
      // Calulate number of Packets
      ReadRequest readRequest = Decoder.decodeReadRequest(message.toMessage());
      long total = (long) Math.ceil(readRequest.getBytesToRead() / P9Protocol.MAX_MSG_CONTENT_SIZE);
      message.setExpectedPackets(total);
    }
    outputWriter.sendMessage(message);
  }

  public void submitAndClose(GelatoMessage message) {
    if (inputReader.isShutdown() || outputWriter.isShutdown()) {
      throw new RuntimeException("Sockets Closed");
    }
    message.setCompleted();
    outputWriter.sendMessage(message);
  }

  public int getIoSize() {
    return ioSize;
  }

  public GelatoMessage<AttachRequest, AttachResponse> createAttachTransaction() {
    return new GelatoMessage<>(new AttachRequest());
  }

  public GelatoMessage<AuthRequest, AuthResponse> createAuthTransaction() {
    return new GelatoMessage<>(new AuthRequest());
  }

  public GelatoMessage<CloseRequest, CloseResponse> createCloseTransaction() {
    return new GelatoMessage<>(new CloseRequest());
  }

  public GelatoMessage<FlushRequest, FlushResponse> createFlushTransaction() {
    return new GelatoMessage<>(new FlushRequest());
  }

  public GelatoMessage<OpenRequest, OpenResponse> createOpenTransaction() {
    return new GelatoMessage<>(new OpenRequest());
  }

  public GelatoMessage<CreateRequest, CreateResponse> createCreateTransaction() {
    return new GelatoMessage<>(new CreateRequest());
  }

  public GelatoMessage<ReadRequest, ReadResponse> createReadTransaction() {
    return new GelatoMessage<>(new ReadRequest());
  }

  public GelatoMessage<StatRequest, StatResponse> createStatTransaction() {
    return new GelatoMessage<>(new StatRequest());
  }

  public GelatoMessage<RemoveRequest, RemoveResponse> createRemoveTransaction() {
    return new GelatoMessage<>(new RemoveRequest());
  }

  public GelatoMessage<WalkRequest, WalkResponse> createWalkTransaction() {
    return new GelatoMessage<>(new WalkRequest());
  }

  public GelatoMessage<WriteRequest, WriteResponse> createWriteTransaction() {
    return new GelatoMessage<>(new WriteRequest());
  }

  public GelatoMessage<WriteStatRequest, WriteStatResponse> createWriteStatRequest() {
    return new GelatoMessage<>(new WriteStatRequest());
  }

  public GelatoMessage<VersionRequest, VersionRequest> createVersionRequest() {
    return new GelatoMessage<>(new VersionRequest());
  }

  @CiotolaServiceStop
  public synchronized void shutdown() {
    shutdown = true;
  }

  @CiotolaServiceStart
  public synchronized void start() {
    shutdown = false;
  }

  @CiotolaServiceRun
  public void process() throws InterruptedException {
    while (!isShutdown()) {
      processMessages();
    }
  }

  public synchronized boolean isShutdown() {
    return shutdown;
  }
}
