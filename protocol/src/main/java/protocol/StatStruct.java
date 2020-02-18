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

package protocol;

import jdk.jfr.*;

import javax.annotation.*;

public class StatStruct {

    private int statSize;
    private int type;
    private int dev;
    private QID qid;
    private int mode;
    private int atime;
    private int mtime;
    private long length;

    private String name;
    private String uid;
    private String gid;
    private String muid;


    public StatStruct duplicate() {
        StatStruct newStatStruct = new StatStruct();
        newStatStruct.statSize = statSize;
        newStatStruct.type = type;
        newStatStruct.dev = dev;
        newStatStruct.qid = qid;
        newStatStruct.mode = mode;
        newStatStruct.atime = atime;
        newStatStruct.mtime = mtime;
        newStatStruct.length = length;
        newStatStruct.name = name;
        newStatStruct.uid = uid;
        newStatStruct.gid = gid;
        newStatStruct.muid = muid;

        return newStatStruct;
    }

    public long getAccessTime() {
        return ByteEncoder.getUnsigned(atime);
    }

    public long getModifiedTime() {
        return ByteEncoder.getUnsigned(mtime);
    }

    public void setAccessTime(long newAccessTime) {
        atime = ByteEncoder.toUnsigned(newAccessTime);
    }

    public int updateSize() {
        int nameSize = ByteEncoder.stringLength(name);
        int uidSize = ByteEncoder.stringLength(uid);
        int guiSize = ByteEncoder.stringLength(gid);
        int muidSzie = ByteEncoder.stringLength(muid);

        int statSize = P9Protocol.MSG_SHORT_SIZE;
        statSize += P9Protocol.MSG_SHORT_SIZE;
        statSize += P9Protocol.MSG_INT_SIZE;
        statSize += P9Protocol.MSG_QID_SIZE;
        statSize += P9Protocol.MSG_INT_SIZE;
        statSize += P9Protocol.MSG_INT_SIZE;
        statSize += P9Protocol.MSG_INT_SIZE;
        statSize += P9Protocol.MSG_LONG_SIZE;
        statSize += nameSize;
        statSize += uidSize;
        statSize += guiSize;
        statSize += muidSzie;

        this.statSize = statSize;

        return statSize;
    }

    public void setModifiedTime(long newModifiedTime) {
        mtime = ByteEncoder.toUnsigned(newModifiedTime);
    }

    public byte [] EncodeStat() {
        byte [] nameByte = ByteEncoder.encodeStringToBuffer(name);
        byte [] uidByte =  ByteEncoder.encodeStringToBuffer(uid);
        byte [] guiByte = ByteEncoder.encodeStringToBuffer(gid);
        byte [] muidByte = ByteEncoder.encodeStringToBuffer(muid);

        int statSize = P9Protocol.MSG_SHORT_SIZE;
        statSize += P9Protocol.MSG_SHORT_SIZE;
        statSize += P9Protocol.MSG_INT_SIZE;
        statSize += P9Protocol.MSG_QID_SIZE;
        statSize += P9Protocol.MSG_INT_SIZE;
        statSize += P9Protocol.MSG_INT_SIZE;
        statSize += P9Protocol.MSG_INT_SIZE;
        statSize += P9Protocol.MSG_LONG_SIZE;
        statSize += nameByte.length;
        statSize += uidByte.length;
        statSize += guiByte.length;
        statSize += muidByte.length;

        byte [] statRet = new byte[statSize];
        int ptr = 0;
        ByteEncoder.encodeShort(this.statSize, statRet, 0);
        ptr += P9Protocol.MSG_SHORT_SIZE;
        ByteEncoder.encodeShort(type, statRet, ptr);
        ptr += P9Protocol.MSG_SHORT_SIZE;
        ByteEncoder.encodeInt(dev, statRet, ptr);
        ptr += P9Protocol.MSG_INT_SIZE;
        ByteEncoder.encodeQID(qid,statRet, ptr);
        ptr += P9Protocol.MSG_QID_SIZE;
        ByteEncoder.encodeInt(mode,statRet,ptr);
        ptr += P9Protocol.MSG_INT_SIZE;
        ByteEncoder.encodeInt(atime, statRet, ptr);
        ptr += P9Protocol.MSG_INT_SIZE;
        ByteEncoder.encodeInt(mtime, statRet, ptr);
        ptr += P9Protocol.MSG_INT_SIZE;
        ByteEncoder.encodeLong(length, statRet, ptr);
        ptr += P9Protocol.MSG_LONG_SIZE;
        ByteEncoder.copyBytesTo(nameByte, statRet, ptr, nameByte.length);
        ptr += nameByte.length;
        ByteEncoder.copyBytesTo(uidByte, statRet, ptr, uidByte.length);
        ptr += uidByte.length;
        ByteEncoder.copyBytesTo(guiByte, statRet, ptr, guiByte.length);
        ptr += guiByte.length;
        ByteEncoder.copyBytesTo(muidByte, statRet, ptr, muidByte.length);
        return statRet;
    }

    public StatStruct DecodeStat(byte[] buffer, int position) {
        int ptr = position;
        statSize = ByteEncoder.decodeShort(buffer,ptr);
        ptr += P9Protocol.MSG_SHORT_SIZE;
        type = ByteEncoder.decodeShort(buffer,ptr);
        ptr += P9Protocol.MSG_SHORT_SIZE;
        dev = ByteEncoder.decodeInt(buffer, ptr);
        ptr += P9Protocol.MSG_INT_SIZE;
        qid = ByteEncoder.decodeQID(buffer, ptr);
        ptr += P9Protocol.MSG_QID_SIZE;
        mode = ByteEncoder.decodeInt(buffer, ptr);
        ptr += P9Protocol.MSG_INT_SIZE;
        atime = ByteEncoder.decodeInt(buffer, ptr);
        ptr += P9Protocol.MSG_INT_SIZE;
        mtime = ByteEncoder.decodeInt(buffer, ptr);
        ptr += P9Protocol.MSG_INT_SIZE;
        this.length = ByteEncoder.decodeLong(buffer, ptr);
        ptr += P9Protocol.MSG_LONG_SIZE;
        name = ByteEncoder.decodeString(buffer, ptr);
        ptr += ByteEncoder.stringLength(name);
        uid = ByteEncoder.decodeString(buffer, ptr);
        ptr += ByteEncoder.stringLength(uid);
        gid = ByteEncoder.decodeString(buffer, ptr);
        ptr += ByteEncoder.stringLength(gid);
        muid = ByteEncoder.decodeString(buffer, ptr);
        updateSize();
        return this;
    }

    public int getStatSize() {
        return statSize;
    }

    public void setStatSize(int statSize) {
        this.statSize = statSize;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDev() {
        return dev;
    }

    public void setDev(int dev) {
        this.dev = dev;
    }


    public QID getQid() {
        return qid;
    }

    public void setQid(QID qid) {
        this.qid = qid;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }


    public int getRawAtime() {
        return atime;
    }

    public void setRawAtime(int atime) {
        this.atime = atime;
    }

    public int getRawMtime() {
        return mtime;
    }

    public void setRawMtime(int mtime) {
        this.mtime = mtime;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getMuid() {
        return muid;
    }

    public void setMuid(String muid) {
        this.muid = muid;
    }
}


