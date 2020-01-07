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
import gelato.server.manager.requests.*;
import org.slf4j.*;
import protocol.*;
import protocol.messages.*;

import java.util.*;

public class GelatoParallelRequestHandler implements GenericRequestHandler {

    private Gelato library;
    private GelatoQIDManager resources;
    private final Logger logger = LoggerFactory.getLogger(GelatoParallelRequestHandler.class);

    public GelatoParallelRequestHandler(Gelato library, GelatoQIDManager qidManager) {
        resources = qidManager;
        this.library = library;
    }

    @Override
    public boolean processRequest(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message request) {
        GelatoFileDescriptor requestedResource = new GelatoFileDescriptor();
        if(request.messageType == P9Protocol.TWALK) {
            requestedResource.setFileId(Decoder.decodeWalkRequest(request).getBaseDescriptor());
        } else if ( request.messageType == P9Protocol.TOPEN) {
            requestedResource.setFileId(Decoder.decodeOpenRequest(request).getFileDescriptor());
        } else {
            return false;
        }

        GelatoFileDescriptor serverResource = session.getManager().getServerDescriptor(requestedResource);
        GelatoResourceHandler handler = resources.getHandler(serverResource);

        int scheduleGroup = (int) (serverResource.getQid().getLongFileId() % library.threadCapacity());
        return handler.processRequest(connection,descriptor,session,request);

    }
}
