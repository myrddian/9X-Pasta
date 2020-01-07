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

package gelato.client.file.impl;

import gelato.*;
import gelato.client.file.*;

import java.io.*;

public class GelatoFileImpl implements GelatoFile {

    private GelatoFileDescriptor descriptor;
    private String fileName;
    private String filePath;
    private long fileSize;
    private int ioUnitSize;

    public void setDescriptor(GelatoFileDescriptor newDescriptor) {descriptor = newDescriptor;}
    public void setFileName(String newName) { fileName = newName;}
    public void setIoSize(int newSize) {ioUnitSize = newSize;}
    public void setFileSize(int newSize) {fileSize = newSize;}
    public void setFilePath(String newPath) { filePath = newPath;}

    @Override
    public GelatoFileDescriptor getFileDescriptor() {
        return descriptor;
    }

    @Override
    public InputStream getFileInputStream() {
        return null;
    }

    @Override
    public OutputStream getFileOutputStream() {
        return null;
    }

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public String filePath() {
        return filePath;
    }

    @Override
    public String fullName() {
        return filePath +"/"+fileName;
    }

    @Override
    public long fileSize() {
        return fileSize;
    }


    @Override
    public int ioSize() {
        return ioUnitSize;
    }
}
