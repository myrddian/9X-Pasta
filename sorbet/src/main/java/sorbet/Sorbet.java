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

package sorbet;

import ciotola.Ciotola;
import ciotola.CiotolaContext;
import gelato.server.GelatoServerManager;
import sorbet.annotations.SorbetFileController;

public class Sorbet {

  public static final String WRITE_BYTES = "writes";
  public static final String RESOURCE = "resource";
  public static final String MODE = "mode";
  public static final String RETURN_BYTE = "returnValue";
  public static final int DEFAULT_PORT = 9090;

  private GelatoServerManager serverManager;

  public Sorbet() {
    serverManager = new GelatoServerManager(DEFAULT_PORT);
  }

  public Sorbet(GelatoServerManager manager) {
    serverManager = manager;
  }

  private void setupCiotola() {
    CiotolaContext ciotolaContext = Ciotola.getInstance();
    ciotolaContext.addAnnotation(SorbetFileController.class);
  }

  public GelatoServerManager getServerManager() {
    return serverManager;
  }
}
