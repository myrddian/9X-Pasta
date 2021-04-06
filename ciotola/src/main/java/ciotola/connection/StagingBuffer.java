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

package ciotola.connection;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class StagingBuffer
    implements CiotolaConnectionHandler, ChannelProcess, ChannelBuffer<byte[]> {

  private ByteBuffer byteBuffer;
  private int notificationLevel;
  private int bufferLevel;
  private ChannelProcess channelProcessChain;
  private BufferHandler<byte[]> nextBufferHandler;

  public StagingBuffer(ChannelProcess next, int capacity, int initialNotification) {
    this.notificationLevel = initialNotification;
    this.channelProcessChain = next;
    byteBuffer = ByteBuffer.allocate(capacity);
  }

  public StagingBuffer(int capacity, int initialNotification) {
    this.notificationLevel = initialNotification;
    byteBuffer = ByteBuffer.allocate(capacity);
    this.channelProcessChain = null;
  }

  private void notifyHandler() {
    if (bufferLevel >= notificationLevel) {
      if (channelProcessChain != null) {
        byteBuffer.flip();
        channelProcessChain.process(byteBuffer);
        bufferLevel = 0;
        byteBuffer.flip();
        byteBuffer.clear();
      }
    }
  }

  @Override
  public void process(ChannelAttributes activeChannel) {
    try {
      int readByte = activeChannel.getChannel().read(byteBuffer);
      bufferLevel += readByte;
      notifyHandler();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void process(ByteBuffer dataIn) {
    byteBuffer.put(dataIn);
    bufferLevel += dataIn.limit();
    notifyHandler();
  }

  @Override
  public void setTriggerLevel(int level) {
    this.notificationLevel = level;
  }

  @Override
  public void setNextStep(ChannelProcess nextStep) {
    channelProcessChain = nextStep;
  }

  @Override
  public boolean canSetNotification() {
    return true;
  }

  @Override
  public int getSize() {
    return bufferLevel;
  }

  @Override
  public byte[] read() {
    byteBuffer.flip();
    byte[] retVal = new byte[byteBuffer.array().length];
    byte[] retValues = byteBuffer.array();
    // Copy the buffer prevent access
    for (int i = 0; i < retValues.length; ++i) {
      retVal[i] = retValues[i];
    }
    byteBuffer.flip();
    byteBuffer.clear();
    return retVal;
  }

  @Override
  public void setNotifier(BufferHandler<byte[]> handler) {
    this.nextBufferHandler = handler;
  }
}
