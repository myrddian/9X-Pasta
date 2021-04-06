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

import gelato.client.file.GelatoDirectory;
import gelato.client.file.GelatoFile;
import gelato.client.file.GelatoFileManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import protocol.StatStruct;

@ShellComponent
public class NagivationCommands {
  @Autowired ShellConnection shellConnection;

  @Autowired FettuccineShellHelper shellHelper;
  private GelatoDirectory currentDirectory;
  private boolean isInitialised = false;
  private GelatoFileManager fileManager;

  @ShellMethod("List Directory")
  public void ls() {
    if (shellConnection.isConnected()) {
      if (!isInitialised) {
        init();
      }
      List<GelatoDirectory> directoryList = currentDirectory.getDirectories();
      List<GelatoFile> fileList = currentDirectory.getFiles();
      if (directoryList == null
          || directoryList.size() == 0 && (fileList.size() == 0 || fileList == null)) {
        shellHelper.print("Nothing here");
      } else {
        for (GelatoDirectory directory : directoryList) {
          shellHelper.print(directory.getName(), PromptColor.GREEN);
        }
        for (GelatoFile file : fileList) {
          shellHelper.print(file.getName(), PromptColor.MAGENTA);
        }
      }
    } else {
      shellHelper.printError("Not Currently connected");
    }
  }

  @ShellMethod("Display contents of file")
  public void cat(@ShellOption({"-f", "--file"}) String file) {
    if (!shellConnection.isConnected()) {
      shellHelper.print("NOT CONNECTED", PromptColor.RED);
      return;
    }

    GelatoFile targetFile = currentDirectory.getFile(file);
    if (targetFile == null) {
      shellHelper.print("File Not found", PromptColor.RED);
      return;
    }

    InputStream fileStream = targetFile.getFileInputStream();
    Scanner scan = new Scanner(fileStream);
    while (scan.hasNextLine()) {
      String line = scan.nextLine();
      shellHelper.print(line, PromptColor.BRIGHT);
    }
    try {
      fileStream.close();
    } catch (IOException e) {
      shellHelper.print(e.toString(), PromptColor.RED);
      e.printStackTrace();
    }
  }

  @ShellMethod("Write to file")
  public void write(
      @ShellOption({"-f", "--file"}) String file,
      @ShellOption({"-m", "--message"}) String message) {
    if (!shellConnection.isConnected()) {
      shellHelper.print("NOT CONNECTED", PromptColor.RED);
      return;
    }

    GelatoFile targetFile = currentDirectory.getFile(file);
    if (targetFile == null) {
      shellHelper.print("File Not found", PromptColor.RED);
      return;
    }

    OutputStream fileStream = targetFile.getFileOutputStream();
    try {
      fileStream.write(message.getBytes());
      fileStream.close();
    } catch (IOException e) {
      shellHelper.print(e.toString(), PromptColor.RED);
      e.printStackTrace();
    }
  }

  @ShellMethod("Query File Structure")
  public void stat(@ShellOption({"-f", "--file"}) String file) {
    if (!shellConnection.isConnected()) {
      shellHelper.print("NOT CONNECTED", PromptColor.RED);
      return;
    }

    StatStruct fileInfo;
    String path = "";
    GelatoFile targetFile = currentDirectory.getFile(file);
    if (targetFile == null) {
      GelatoDirectory targetDir = currentDirectory.getDirectory(file);
      if (targetDir == null) {
        shellHelper.print("File/Directory Not found", PromptColor.RED);
        return;
      }
      fileInfo = targetDir.getStatStruct();
      path = targetDir.getPath();
    } else {
      fileInfo = targetFile.getStatStruct();
      path = targetFile.getPath();
    }

    shellHelper.print("File Name: " + fileInfo.getName(), PromptColor.YELLOW);
    shellHelper.print("File Path: " + path, PromptColor.YELLOW);
    shellHelper.print(
        "File Size: " + Long.toString(fileInfo.getLength()) + " bytes", PromptColor.YELLOW);
    shellHelper.print("File Type: " + fileInfo.getQid().getType(), PromptColor.YELLOW);
    shellHelper.print("Owned by: " + fileInfo.getUid(), PromptColor.YELLOW);
    shellHelper.print("File Mode: " + fileInfo.getMode(), PromptColor.YELLOW);
    shellHelper.print("File Structure " + fileInfo.getStatSize() + " bytes", PromptColor.YELLOW);
    shellHelper.print("File ID: " + fileInfo.getQid().getLongFileId(), PromptColor.YELLOW);
  }

  @ShellMethod("Change Directory")
  public void cd(@ShellOption({"-D", "--directory"}) String directory) {
    if (shellConnection.isConnected()) {
      if (!isInitialised) {
        init();
      }
      GelatoDirectory dir = currentDirectory.getDirectory(directory);
      if (dir == null) {
        shellHelper.printError("Invalid Name - Resource not found");
      } else {
        currentDirectory = dir;
      }
    } else {
      shellHelper.printError("Not Currently connected");
    }
  }

  @ShellMethod("Show current Path")
  public void pwd() {
    if (shellConnection.isConnected()) {
      if (!isInitialised) {
        init();
      }
      shellHelper.print(currentDirectory.getFullName(), PromptColor.BRIGHT);
    }
  }

  private GelatoDirectory findDir(String dirName, List<GelatoDirectory> gelatoDirectoryList) {
    for (GelatoDirectory dir : gelatoDirectoryList) {
      if (dir.getName().equals(dirName)) {
        return dir;
      }
    }
    return null;
  }

  private void init() {
    fileManager = shellConnection.getFileManager();
    currentDirectory = fileManager.getRoot();
    isInitialised = true;
  }
}
