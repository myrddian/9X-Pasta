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

package sorbet;

import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.GelatoServerManager;
import gelato.server.manager.controllers.impl.GelatoDirectoryControllerImpl;
import protocol.P9Protocol;
import protocol.StatStruct;

import java.util.List;

public class SorbetDirectory extends GelatoDirectoryControllerImpl {

    public SorbetDirectory(GelatoServerManager gelatoServerManager) {
        super(gelatoServerManager);
    }

    public void putResource(SorbetResourceSession session, StatStruct resource) {
        session.put(Sorbet.RESOURCE, resource);
    }

    public void putWrite(SorbetResourceSession session, List<byte[]> byteBuffer) {
        session.put(Sorbet.WRITE_BYTES, byteBuffer);
    }

    public StatStruct getResource(SorbetResourceSession session) {
        return (StatStruct)session.get(Sorbet.RESOURCE);
    }

    public SorbetResourceSession sessionVar() {
        SorbetResourceSession sess  =  new SorbetResourceSession();
        putMode(sess, P9Protocol.OPEN_MODE_ORCLOSE);
        return sess;
    }

    public List<byte[]> getWriteRequestData(SorbetResourceSession sessionMap) {
        return (List)sessionMap.get(Sorbet.WRITE_BYTES);
    }

    public String descriptoString(GelatoFileDescriptor descriptorId) {
        return  Long.toString(descriptorId.getDescriptorId());
    }

    public SorbetResourceSession getSessionMap(GelatoSession clientSession, GelatoFileDescriptor descriptorId) {
        String desriptorIdStr = Long.toString(descriptorId.getDescriptorId());
        return getSessionMap(clientSession, desriptorIdStr);
    }

    public SorbetResourceSession getSessionMap(GelatoSession clientSession,String descriptorId) {
        return (SorbetResourceSession) clientSession.getSessionVar(descriptorId);
    }

    public int getMode(SorbetResourceSession sessionMap) {
        return (int)sessionMap.get(Sorbet.MODE);
    }

    public void putMode(SorbetResourceSession sessionMap, int mode) {
        sessionMap.put(Sorbet.MODE, mode);
    }

    public void setFileName(String newName) {
        getResourceController().getStat().setName(newName);
        getResourceController().getStat().updateSize();
    }

    public void setFileSize(long newFileSize) {
        getResourceController().getStat().setLength(newFileSize);
    }

    public void setFileGroupId(String newGroupId) {
        getResourceController().getStat().setGid(newGroupId);
        getResourceController().getStat().updateSize();
    }

    public void setFileOwnerId(String ownerId) {
        getResourceController().getStat().setUid(ownerId);
        getResourceController().getStat().updateSize();
    }

    public void setModifierUid(String modifierUid) {
        getResourceController().getStat().setMuid(modifierUid);
        getResourceController().getStat().updateSize();
    }

    public void setFileDescriptor(GelatoFileDescriptor descriptor) {
        getQID().setLongFileId(descriptor.getDescriptorId());
    }

    public void setFileAccessTime(long accessTime) {
        getResourceController().getStat().setAccessTime(accessTime);
        getResourceController().getStat().updateSize();
    }

    public void setModifiefTime(long modifiedTime) {
        getResourceController().getStat().setModifiedTime(modifiedTime);
        getResourceController().getStat().updateSize();
    }

}
