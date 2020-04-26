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

import gelato.GelatoFileDescriptor;
import gelato.client.GelatoMessage;
import gelato.client.GelatoMessaging;
import gelato.client.file.GelatoFile;
import gelato.client.file.GelatoInputStream;
import gelato.client.file.GelatoOutputStream;
import protocol.P9Protocol;
import protocol.StatStruct;
import protocol.messages.request.OpenRequest;
import protocol.messages.response.OpenResponse;

import java.io.InputStream;
import java.io.OutputStream;

public class GelatoFileImpl  extends GelatoResourceImpl implements GelatoFile {

  private String filePath="";

  public GelatoFileImpl(GelatoMessaging messaging, GelatoFileDescriptor descriptor) {
    super(messaging, descriptor);
  }

  public void setFilePath(String newPath) {
    filePath = newPath;
  }

  @Override
  public InputStream getFileInputStream() {
    StatStruct localStruct = getStatStruct();
    GelatoMessage<OpenRequest, OpenResponse> openRequest = getMessaging().createOpenTransaction();
    openRequest.getMessage().setFileDescriptor(getFileDescriptor().getRawFileDescriptor());
    openRequest.getMessage().setMode((byte) P9Protocol.OPEN_MODE_OREAD);
    getMessaging().submitAndClose(openRequest);
    return new GelatoInputStream(getMessaging(),getFileDescriptor(),P9Protocol.MAX_MSG_CONTENT_SIZE,localStruct.getLength());
  }

  @Override
  public OutputStream getFileOutputStream() {
    return getFileOutputStream((byte) P9Protocol.OPEN_MODE_OWRITE);
  }

  @Override
  public OutputStream getFileOutputStream(int MODE) {
    GelatoMessage<OpenRequest, OpenResponse> openRequest = getMessaging().createOpenTransaction();
    openRequest.getMessage().setFileDescriptor(getFileDescriptor().getRawFileDescriptor());
    openRequest.getMessage().setMode((byte) MODE);
    getMessaging().submitAndClose(openRequest);
    return new GelatoOutputStream(getMessaging(),getFileDescriptor(),P9Protocol.MAX_MSG_CONTENT_SIZE);
  }

  @Override
  public String getPath() {
    return filePath;
  }

  @Override
  public String getFullName() {
    return filePath + "/" + getName();
  }


}
