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
import gelato.server.GelatoServerManager;
import gelato.server.manager.RequestConnection;
import gelato.server.manager.controllers.impl.GelatoDirectoryControllerImpl;
import protocol.Decoder;
import protocol.P9Protocol;
import protocol.StatStruct;
import protocol.messages.Message;
import protocol.messages.request.OpenRequest;
import protocol.messages.request.ReadRequest;
import protocol.messages.request.StatRequest;
import protocol.messages.request.WalkRequest;
import protocol.messages.response.StatResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MountDirectory extends GelatoDirectoryControllerImpl {

  private Map<String, RemoteResource> artefacts = new ConcurrentHashMap<>();
  private MountPoint mountPoint;
  private GelatoFileDescriptor proxiedDescriptor;
  private GelatoResource resource;
  private String mountPointName;

  public MountDirectory(
      GelatoServerManager gelatoServerManager,
      MountPoint service,
      GelatoResource resource,
      String mountName) {
    super(gelatoServerManager);
    mountPoint = service;
    proxiedDescriptor = mountPoint.getMountPoint().getClientSession().getFileServiceRoot();
    this.resource = resource;
    mountPointName = mountName;
    getStat();
  }

  @Override
  public StatStruct getStat() {
    resource.refreshSelf();
    StatStruct background = resource.getStatStruct().duplicate();
    background.setName(mountPointName);
    background.updateSize();
    getResourceController().setStat(background);
    return background;
  }

  @Override
  public boolean processRequest(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      Message request) {

    if (request.messageType == P9Protocol.TSTAT) {
      StatRequest statRequest = Decoder.decodeStatRequest(request);
      StatResponse rsp = new StatResponse();
      rsp.setStatStruct(getStat());
      rsp.setTag(request.tag);
      connection.sendMessage(descriptor, rsp.toMessage());
      return true;
    }

    Message rewrite = request;

    if (request.messageType == P9Protocol.TOPEN) {
      OpenRequest decoded = Decoder.decodeOpenRequest(request);
      decoded.setFileDescriptor(proxiedDescriptor.getRawFileDescriptor());
      rewrite = decoded.toMessage();
    }

    if (request.messageType == P9Protocol.TREAD) {
      ReadRequest decoded = Decoder.decodeReadRequest(request);
      decoded.setFileDescriptor(proxiedDescriptor.getRawFileDescriptor());
      rewrite = decoded.toMessage();
    }

    if (request.messageType == P9Protocol.TWALK) {
      WalkRequest walk = Decoder.decodeWalkRequest(request);
      RemoteResource mappedSource = artefacts.get(walk.getTargetFile());
      GelatoFileDescriptor clientDescriptor = generateDescriptor(getQID(), walk.getNewDecriptor());
      session.getManager().mapQID(clientDescriptor, mappedSource.getFileDescriptor());
      walk.setBaseDescriptor(proxiedDescriptor.getRawFileDescriptor());
      rewrite = walk.toMessage();
    }

    RequestConnection requestConnection =
        createConnection(connection, descriptor, session, request.tag);
    mountPoint.sendProxyMessage(rewrite, requestConnection);

    return true;
  }

  public void addArtefact(RemoteResource newArtefact) {
    artefacts.put(newArtefact.resourceName(), newArtefact);
  }
}
