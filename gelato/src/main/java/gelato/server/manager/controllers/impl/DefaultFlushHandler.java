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

package gelato.server.manager.controllers.impl;

import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.manager.implementation.requests.RequestFlushHandler;
import protocol.messages.request.FlushRequest;
import protocol.messages.response.FlushResponse;

public class DefaultFlushHandler implements RequestFlushHandler {
    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, FlushRequest request) {
        FlushResponse response = new FlushResponse();
        response.setTransactionId(request.getTag());
        response.setTag(request.getOldtag());
        connection.sendMessage(descriptor,response.toMessage());
        return true;
    }
}
