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

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

final class ChannelAttributesImpl implements ChannelAttributes {

  private SocketChannel channel;
  private SelectionKey selectionKey;
  private boolean isCallBackHandler = false;
  private Long timeSinceLastUpdate = 0L;
  private CiotolaConnectionHandler callback;
  private Boolean isInError = false;

  public ChannelAttributesImpl(SocketChannel channel, SelectionKey selectionKey) {
    this.channel = channel;
    this.selectionKey = selectionKey;
  }

  public boolean isCallBackHandler() {
    return isCallBackHandler;
  }

  public void setCallBackHandler(CiotolaConnectionHandler handler) {
    isCallBackHandler = true;
    callback = handler;
  }

  @Override
  public SocketChannel getChannel() {
    return channel;
  }

  @Override
  public SelectionKey getSelectionKey() {
    return selectionKey;
  }

  @Override
  public Long getTimeSinceLastUpdate() {
    return timeSinceLastUpdate;
  }

  public void setTimeSinceLastUpdate(Long newTime) {
    this.timeSinceLastUpdate = newTime;
  }

  @Override
  public Boolean noErrors() {
    return isInError;
  }

  @Override
  public void setError() {
    isInError = true;
  }

  @Override
  public CiotolaConnectionHandler getCallback() {
    return this.callback;
  }
}
