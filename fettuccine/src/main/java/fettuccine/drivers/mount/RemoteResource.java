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

package fettuccine.drivers.mount;

import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.client.file.GelatoResource;
import gelato.server.manager.RequestConnection;
import gelato.server.manager.controllers.impl.GelatoResourceControllerImpl;
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.messages.Message;
import protocol.messages.request.WalkRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteResource extends GelatoResourceControllerImpl {

    private MountPoint mountPoint;
    private Map<String, RemoteResource> artefacts = new ConcurrentHashMap<>();
    private GelatoResource resource;

    public RemoteResource(MountPoint originatingMount, GelatoResource proxy) {

        mountPoint = originatingMount;
        resource = proxy;
        resource.refreshSelf();
    }

    @Override
    public String resourceName() {
        return resource.getName();
    }

    public void addArtefact(RemoteResource newArtefact){
        artefacts.put(newArtefact.resourceName(),newArtefact);
    }

    @Override
    public boolean processRequest(
            GelatoConnection connection,
            GelatoFileDescriptor descriptor,
            GelatoSession session,
            Message request) {

        RequestConnection requestConnection = createConnection(connection,descriptor,session, request.tag);
        mountPoint.sendProxyMessage(request,requestConnection);

        if (request.messageType == P9Protocol.TWALK) {
            WalkRequest walk = Decoder.decodeWalkRequest(request);
            RemoteResource mappedSource = artefacts.get(walk.getTargetFile());
            GelatoFileDescriptor clientDescriptor =
                    generateDescriptor(getQID(), walk.getNewDecriptor());
            session.getManager().mapQID(clientDescriptor, mappedSource.getFileDescriptor());
        }

        return true;
    }

}
