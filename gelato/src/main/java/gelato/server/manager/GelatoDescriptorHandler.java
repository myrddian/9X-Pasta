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
import gelato.client.file.*;
import org.slf4j.*;
import protocol.*;
import protocol.messages.*;

import java.util.*;

public class GelatoDescriptorHandler {

    final Logger logger = LoggerFactory.getLogger(GelatoDescriptorHandler.class);

    private GelatoConnection serverConnection;
    private Map<GelatoFileDescriptor, GelatoSession> descriptorGelatoSessionMap = new HashMap<>();
    private GelatoSessionHandler sessionHandler;
    private Gelato library;

    public GelatoDescriptorHandler(Gelato library, GelatoConnection server, GelatoSessionHandler sessionHandler1) {
        serverConnection = server;
        this.library = library;
        sessionHandler = sessionHandler1;
    }

    public void processDescriptorMessages() {
        for(GelatoFileDescriptor connection: serverConnection.getConnections()) {
            if(serverConnection.getMessageCount(connection) > 0 ) {
                Message msg = serverConnection.getMessage(connection);
                GelatoSession session = descriptorGelatoSessionMap.get(connection);
                if(session == null) {
                    createDescriptorSession(connection, msg);
                } else {
                    sessionHandler.handleSession(serverConnection,connection,session, msg);
                }
            }
        }
    }

    private void createDescriptorSession(GelatoFileDescriptor descriptor, Message msg) {
        if(msg.messageType != P9Protocol.TVERSION) {
            logger.error("Descriptor has not started a valid session " + Integer.toString(descriptor.getDescriptorId()) + " " + Byte.toString(msg.messageType));
            return;
        }
        library.getTagManager().createTagHandler(descriptor);
        GelatoSession newSession = new GelatoSession();
        newSession.setManager(new GelatoDescriptorManager());
        newSession.setConnection(serverConnection);
        newSession.setTags(library.getTagManager().getManager(descriptor));
        VersionRequest response = new VersionRequest();
        Message rspVersion = response.toMessage();
        rspVersion.messageType = P9Protocol.RVERSION;
        rspVersion.tag = msg.tag;
        newSession.getTags().registerTag(msg.tag);
        serverConnection.sendMessage(descriptor, rspVersion);
        descriptorGelatoSessionMap.put(descriptor, newSession);
        logger.info("Started Session for Descriptor " + Integer.toString(descriptor.getDescriptorId()));
    }

}
