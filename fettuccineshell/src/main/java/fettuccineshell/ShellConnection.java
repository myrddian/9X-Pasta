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

package fettuccineshell;

import gelato.Gelato;
import gelato.GelatoConfigImpl;
import gelato.GelatoConnection;
import gelato.client.file.GelatoFileManager;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class ShellConnection {

  private Gelato gelato;
  private GelatoConnection client;
  private GelatoFileManager fileManager;
  private GelatoConfigImpl config;
  private boolean isConnected = false;

  public GelatoFileManager getFileManager() {
    return fileManager;
  }

  public boolean isConnected() {
    return isConnected;
  }

  public boolean connect(String hostName, int port, String userName) {
    try {
      fileManager = new GelatoFileManager(hostName, port, userName);
    } catch (IOException e) {
      e.printStackTrace();
    }
    isConnected = true;
    return true;
  }

  public String getHost() {
    return config.getHostName();
  }
}
