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

package fettuccineshell;

import gelato.*;
import gelato.client.file.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.*;

@ShellComponent
public class NagivationCommands {
    @Autowired
    ShellConnection shellConnection;

    @Autowired
    FettuccineShellHelper shellHelper;

    @ShellMethod("List Directory")
    public void ls() {
        if(shellConnection.isConnected()) {
            if(!isInitialised) {
               init();
            }
            List<GelatoDirectory> directoryList = currentDirectory.getDirectories();
            if(directoryList == null || directoryList.size()==0) {
                shellHelper.print("Nothing here");
            }
            else {
                for(GelatoDirectory directory: directoryList ) {
                    shellHelper.print(directory.getName(), PromptColor.GREEN);
                }
            }
        }
        else {
            shellHelper.printError("Not Currently connected");
        }
    }

    @ShellMethod("Change Directory")
    public void cd(@ShellOption({"-D", "--directory"}) String directory) {
        if(shellConnection.isConnected()) {
            if(!isInitialised) {
                init();
            }
            GelatoDirectory dir = currentDirectory.getDirectory(directory);
            if(dir==null) {
                shellHelper.printError("Invalid Name - Resource not found");
            }
            else {
                currentDirectory = dir;
            }
        }
        else {
            shellHelper.printError("Not Currently connected");
        }
    }

    @ShellMethod("Show current Path")
    public void pwd() {
        if(shellConnection.isConnected()) {
            if (!isInitialised) {
                init();
            }
            shellHelper.print(currentDirectory.getFullName(), PromptColor.BRIGHT);
        }
    }

    private GelatoDirectory findDir(String dirName, List<GelatoDirectory> gelatoDirectoryList) {
        for(GelatoDirectory dir: gelatoDirectoryList) {
            if(dir.getName().equals(dirName)) {
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

    private GelatoDirectory currentDirectory;
    private boolean isInitialised = false;
    private GelatoFileManager fileManager;
}

