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

package gelato.server.manager.implementation;

import gelato.*;
import gelato.server.manager.requests.*;
import protocol.messages.*;
import protocol.messages.response.*;

public class GenericUnknownHandler implements GenericRequestHandler {

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message request) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setTag(request.tag);
        errorMessage.setErrorMessage("Unknown Request - Error");
        connection.sendMessage(descriptor, errorMessage.toMessage());
        return true;
    }
}
