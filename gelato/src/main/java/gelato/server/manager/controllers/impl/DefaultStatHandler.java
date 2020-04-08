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

import gelato.GelatoFileDescriptor;
import gelato.server.manager.RequestConnection;
import gelato.server.manager.processchain.StatRequestHandler;
import protocol.StatStruct;
import protocol.messages.response.StatResponse;

public class DefaultStatHandler implements StatRequestHandler {
    @Override
    public boolean statRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
        StatStruct selfStat = connection.getResourceController().getStat();
        StatResponse response = new StatResponse();
        response.setStatStruct(selfStat);
        connection.reply(response);
        return true;
    }
}
