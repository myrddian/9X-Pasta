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
import gelato.server.manager.controllers.impl.GelatoFileControllerImpl;
import protocol.P9Protocol;
import protocol.StatStruct;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SorbetFile extends GelatoFileControllerImpl {

    private OutputStream outputStream = null;

    public SorbetFile(String fileName, GelatoFileDescriptor descriptor) {
        super(fileName, null , 0, descriptor);
    }

    public SorbetFile(String fileName, InputStream inputStream, long resourceSize, GelatoFileDescriptor descriptor) {
        super(fileName, inputStream, resourceSize, descriptor);
    }


    public SorbetFile(String fileName, InputStream inputStream, OutputStream outputStream,
                      long resourceSize, GelatoFileDescriptor descriptor) {
        super(fileName, inputStream, resourceSize, descriptor);
        this.outputStream = outputStream;
    }

    public void setOutputStream(OutputStream newStream) {
        outputStream = newStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void putResource(Map<String,Object> session, StatStruct resource) {
        session.put(Sorbet.RESOURCE, resource);
    }

    public void putWrite(Map<String,Object> session, List<byte[]> byteBuffer) {
        session.put(Sorbet.WRITE_BYTES, byteBuffer);
    }

    public StatStruct getResource(Map<String,Object> session) {
        return (StatStruct)session.get(Sorbet.RESOURCE);
    }

    public Map<String, Object> sessionVar() {
        Map<String, Object> sess  =  new ConcurrentHashMap<>();
        putMode(sess, P9Protocol.OPEN_MODE_ORCLOSE);
        return sess;
    }

    public List<byte[]> getWriteRequestData(Map<String,Object> sessionMap) {
        return (List)sessionMap.get(Sorbet.WRITE_BYTES);
    }

    public String descriptoString(GelatoFileDescriptor descriptorId) {
        return  Long.toString(descriptorId.getDescriptorId());
    }

    public Map<String, Object> getSessionMap(GelatoSession clientSession, GelatoFileDescriptor descriptorId) {
        String desriptorIdStr = Long.toString(descriptorId.getDescriptorId());
        return getSessionMap(clientSession, desriptorIdStr);
    }

    public Map<String, Object> getSessionMap(GelatoSession clientSession,String descriptorId) {
        return (Map) clientSession.getSessionVar(descriptorId);
    }

    public int getMode(Map<String,Object> sessionMap) {
        return (int)sessionMap.get(Sorbet.MODE);
    }

    public void putMode(Map<String,Object> sessionMap, int mode) {
        sessionMap.put(Sorbet.MODE, mode);
    }

    public void setFileName(String newName) {
        getResource().getStat().setName(newName);
        getResource().getStat().updateSize();
    }

    public void setFileSize(long newFileSize) {
        getResource().getStat().setLength(newFileSize);
    }

    public void setFileGroupId(String newGroupId) {
        getResource().getStat().setGid(newGroupId);
        getResource().getStat().updateSize();
    }

    public void setFileOwnerId(String ownerId) {
        getResource().getStat().setUid(ownerId);
        getResource().getStat().updateSize();
    }

    public void setModifierUid(String modifierUid) {
        getResource().getStat().setMuid(modifierUid);
        getResource().getStat().updateSize();
    }

    public void setFileDescriptor(GelatoFileDescriptor descriptor) {
        getQID().setLongFileId(descriptor.getDescriptorId());
    }

    public void setFileAccessTime(long accessTime) {
        getResource().getStat().setAccessTime(accessTime);
        getResource().getStat().updateSize();
    }

    public void setModifiefTime(long modifiedTime) {
        getResource().getStat().setModifiedTime(modifiedTime);
        getResource().getStat().updateSize();
    }

}
