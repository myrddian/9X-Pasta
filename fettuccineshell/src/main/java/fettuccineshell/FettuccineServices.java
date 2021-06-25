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

import agnolotti.client.RemoteClient;
import common.api.fettuccine.FettuccineConstants;
import common.api.fettuccine.FettuccineNameSpace;
import common.api.fettuccine.FettuccineVersion;
import common.api.fettuccine.Mount;
import gelato.GelatoVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import protocol.P9Protocol;

@ShellComponent
public class FettuccineServices {

  @Autowired
  ShellConnection shellConnection;
  @Autowired
  FettuccineShellHelper shellHelper;

  private boolean initComplete = false;
  private RemoteClient remoteClient;
  private FettuccineVersion versionService;
  private Mount mountService;
  private FettuccineNameSpace nameSpaces;

  private boolean initRpc() {
    if (shellConnection.isConnected()) {
      if (!initComplete) {
        remoteClient =
            new RemoteClient(
                shellConnection.getFileManager(),
                FettuccineConstants.SYS_DIR,
                FettuccineConstants.FETTUCCINE_RPC_VERSION);
        versionService = (FettuccineVersion) remoteClient.getRemoteService(FettuccineVersion.class);
        mountService = (Mount) remoteClient.getRemoteService(Mount.class);
        nameSpaces = (FettuccineNameSpace) remoteClient.getRemoteService(FettuccineNameSpace.class);
        initComplete = true;
      }
      return true;
    }
    return false;
  }

  @ShellMethod("Get Fettuccine Version")
  public void version() {
    if (initRpc()) {
      shellHelper.print("Fettuccine Version reported as: ");
      shellHelper.print(versionService.getVersion(), PromptColor.BRIGHT);
    } else {
      shellHelper.print("No Connection");
    }
    shellHelper.print("Shell built with Gelato: ");
    shellHelper.print(" - " + GelatoVersion.getVersion(), PromptColor.BRIGHT);
    shellHelper.print("Protocol library: ");
    shellHelper.print(" - " + P9Protocol.protocolVersion, PromptColor.BRIGHT);
  }

  @ShellMethod("Mount a remote 9X Service to Fettuccine")
  public void mount(
      @ShellOption({"-S", "--server"}) String server,
      @ShellOption({"-P", "--port"}) int port,
      @ShellOption({"-U", "--user"}) String user,
      @ShellOption({"-M", "--mount"}) String mountPoint) {
    if (initRpc()) {
      mountService.mount(server, port, user, mountPoint);
      shellConnection.getFileManager().getRoot().refreshSelf();
    } else {
      shellHelper.print("No Connection");
    }
  }

  @ShellMethod("List curent mounted servers")
  public void listMounts() {
    if (initRpc()) {
      shellHelper.print("Current mounted servers are: ");
      for (String detail : mountService.listMounts()) {
        shellHelper.print(detail, PromptColor.CYAN);
      }
    }
  }

  @ShellMethod("List name spaces")
  public void listNs() {
    if (initRpc()) {
      shellHelper.print("Namespaces: ");
      for (String detail : nameSpaces.getNameSpaces()) {
        shellHelper.print(detail, PromptColor.CYAN);
      }
    }
  }

  @ShellMethod("Crete name space")
  public void createNs(@ShellOption({"-N", "--name"}) String nsname) {
    if (initRpc()) {
      nameSpaces.createNameSpace(nsname);
    }
  }
}
