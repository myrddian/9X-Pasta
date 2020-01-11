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

package gelato.server.manager;

import gelato.*;
import gelato.server.manager.implementation.*;
import gelato.server.manager.requests.*;
import protocol.*;

public abstract class GelatoAbstractFileServelet extends IgnoreFlushRequests{

    public String getFileName() { return this.resourceName();}
    public void setFileName(String fileName) { this.setResourceName(fileName);}

    @Override
    public void walkRequest(RequestConnection connection, String fileName, GelatoFileDescriptor newDescriptor) {
        sendErrorMessage(connection,"Cannot Process Walk on File Resource, only directories");
    }

    @Override
    public void createRequest(RequestConnection connection, String fileName, int permission, byte mode) {
        sendErrorMessage(connection, "Create Requests can only be handled in a directory resource");
    }

}

