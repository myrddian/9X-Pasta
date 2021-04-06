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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ConnectCommand {
  @Autowired ShellConnection shellConnection;

  @Autowired FettuccineShellHelper shellHelper;

  @ShellMethod("Connect to a target Service")
  public void connect(
      @ShellOption({"-S", "--server"}) String server,
      @ShellOption({"-P", "--port"}) int port,
      @ShellOption({"-U", "--user"}) String user) {

    if (!shellConnection.connect(server, port, user)) {
      shellHelper.printError("Unable to connect");
    }
    shellHelper.printSuccess("Connected!");
  }
}
