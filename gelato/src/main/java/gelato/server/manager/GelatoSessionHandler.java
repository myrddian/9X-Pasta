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
import protocol.messages.request.*;
import protocol.messages.response.*;

public class GelatoSessionHandler {

    final Logger logger = LoggerFactory.getLogger(GelatoSessionHandler.class);
    private GelatoQIDManager qidManager;
    private GenericRequestHandler handler;

    public GelatoSessionHandler(GelatoQIDManager gelatoQIDManager) {
        qidManager = gelatoQIDManager;
    }

    public void handleSession(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message message) {
       GelatoFileDescriptor auth = session.getAuthorisationDescriptor();

       //validate tag
       if (session.getTags().isRecycled(message.tag)  != false) {
           logger.error("Invalid Tags, Expected: " + Integer.toString(session.getTags().getTagCount()) + " Got: " + Integer.toString(message.tag));
       }
       session.getTags().registerTag(message.tag);
       if(auth == null) {
           requestAuth(connection, descriptor, session, message);
       } else {
           if (handler == null) {
               logger.error("Request Handler currently not setup ");
               return;
           }
           if (handler.processRequest(connection, descriptor, session, message) == false) {
               logger.error("Unable to process request");
           }
       }
    }

    private void requestAuth(GelatoConnection connection, GelatoFileDescriptor descriptor, GelatoSession session, Message message) {
        if(message.messageType != P9Protocol.TAUTH) {
            logger.error("Invalid Session Creation - Expected AUTH Message");
            return;
        }
        //Do a null handle - no proper auth just pass through
        AuthRequest authRequest = Decoder.decodeAuthRequest(message);
        session.setUserName(authRequest.getUserName());

        //setup session stuff
        GelatoDescriptorManager newDescriptorSessionManager = new GelatoDescriptorManager();
        GelatoFileDescriptor newAuth = new GelatoFileDescriptor();
        session.setManager(newDescriptorSessionManager);

        QID authQID = qidManager.generateAuthQID();

        newAuth.setQid(authQID);
        newAuth.setFileId(authRequest.getAuthFileID());
        newDescriptorSessionManager.registerDescriptor(newAuth);


        logger.info("User: " + authRequest.getUserName() + " authenticated against FID: " + Integer.toString(authRequest.getAuthFileID())
                + " Connection: " + Integer.toString(descriptor.getDescriptorId()));
        //send response
        AuthRequestResponse response = new AuthRequestResponse();
        response.setTag(message.tag);
        response.setQid(authQID);
        connection.sendMessage(descriptor,response.toMessage());

    }

}
