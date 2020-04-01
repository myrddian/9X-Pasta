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

package fettuccine.drivers.proc;


import fettuccine.*;
import gelato.*;
import gelato.server.manager.*;
import org.slf4j.*;
import protocol.*;
import protocol.messages.response.*;

import java.time.*;

public class ProcDir extends GelatoGelatoAbstractDirectoryServelet {

    public static final long PROC_ID = 100l;
    public static final String PROC_NAME  = "proc";

    private final Logger logger = LoggerFactory.getLogger(ProcDir.class);

    public ProcDir() {
        setResourceName(PROC_NAME);
        StatStruct newStat = getStat();
        newStat.setAccessTime(Instant.now().getEpochSecond());
        newStat.setModifiedTime(newStat.getAccessTime());
        newStat.setUid(FettuccineService.FETTUCCINE_SVC_NAME);
        newStat.setGid(FettuccineService.FETTUCCINE_SVC_GRP);
        newStat.setMuid(FettuccineService.FETTUCCINE_SVC_NAME);
        QID qid  = getQID();
        qid.setType(P9Protocol.QID_DIR);
        qid.setVersion(0);
        qid.setLongFileId(PROC_ID);
        newStat.setQid(qid);
        newStat.updateSize();
        setQID(qid);
        setStat(newStat);
    }

    @Override
    public void openRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, byte mode) {
        if(mode == P9Protocol.OPEN_MODE_OREAD) {
            OpenResponse response = new OpenResponse();
            response.setFileQID(getQID());
            connection.reply(response);
            return;
        }
        sendErrorMessage(connection,"Only READ mode is allowed");
    }

    @Override
    public void createRequest(RequestConnection connection, String fileName, int permission, byte mode) {
        sendErrorMessage(connection, "This operation is not supported");
    }

    @Override
    public void removeRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor) {
        sendErrorMessage(connection, "This operation is not supported");
    }

    @Override
    public void writeStatRequest(RequestConnection connection, GelatoFileDescriptor clientFileDescriptor, StatStruct newStruct) {
        sendErrorMessage(connection, "This operation is not supported");
    }

}
