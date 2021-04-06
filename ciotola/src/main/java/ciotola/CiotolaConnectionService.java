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

package ciotola;

import java.io.IOException;

public interface CiotolaConnectionService {

  static final int NO_PROXY_TERMINATION = -1;

  int bytesToProcessInbound() throws IOException;

  int messagesToProcessOutbound();

  long getConnectionId();

  void processInbound() throws IOException, InterruptedException;

  void processOutbound() throws IOException, InterruptedException;

  void notifyClose();

  boolean isRunning();

  long getProcessedTime();

  void setProcessedTime(long time);

  boolean isClosed();

  int getProxyId();
}
