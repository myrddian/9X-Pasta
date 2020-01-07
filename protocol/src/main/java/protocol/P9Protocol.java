
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

import protocol.messages.*;

public interface P9Protocol {
    public static final String protocolVersion = "9P/X";
    public static final String currentPath = ".";
    public static final String parentPath = "..";
    public static final  long KILO_BYTE = 1024;
    public static final  long MEGA_BYTE = 1024 * KILO_BYTE;
    public static final  int DEFAULT_MSG_SIZE = 65535;
    public static final  int MAX_MSG_CONTENT_SIZE = DEFAULT_MSG_SIZE - MessageRaw.minSize;
    public static final  long DEFAULT_BUFF_SIZE = 4 * MEGA_BYTE;
    public static final  int MSG_TYPE_SIZE  = 1;
    public static final  int MSG_SIZE_HEADER = 4;
    public static final  int MSG_TAG_SIZE = 2;
    public static final  int MSG_FID_SIZE = 4;
    public static final  int MSG_QID_SIZE = 13;
    public static final  int MSG_INT_SIZE = 4;
    public static  enum OPERATIONS {
        VERSION,
        AUTH,
        ERROR,
        ATTACH,
        WALK,
        OPEN,
        READ,
        WRITE,
        CLUNK,
        REMOVE,
        STAT
    }
    public static final byte TVERSION = 100;
    public static final byte RVERSION = TVERSION +  1;
    public static final byte TAUTH = RVERSION + 1;
    public static final byte RAUTH = TAUTH + 1;
    public static final byte TATTACH = RAUTH + 1;
    public static final byte RATTACH = TATTACH + 1;
    public static final byte TERROR = RATTACH + 1;
    public static final byte RERROR = TERROR + 1;
    public static final byte TFLUSH = RERROR + 1;
    public static final byte RFLUSH = TFLUSH + 1;
    public static final byte TWALK = RFLUSH + 1;
    public static final byte RWALK = TWALK + 1;
    public static final byte TOPEN = RWALK + 1;
    public static final byte ROPEN = TOPEN + 1;
    public static final byte TCREATE = ROPEN + 1;
    public static final byte RCREATE = TCREATE + 1;
    public static final byte TREAD = RCREATE + 1;
    public static final byte RREAD = TREAD + 1;
    public static final byte TWRITE = RREAD + 1;
    public static final byte RWRITE = TWRITE + 1;
    public static final byte TCLUNK = RREAD + 1;
    public static final byte RCLUNK = TCLUNK + 1;
    public static final byte TREMOVE = RCLUNK + 1;
    public static final byte RREMOVE = TREMOVE + 1;
    public static final byte TSTAT = RREMOVE +1;
    public static final byte TWSTAT = TSTAT + 1;
    public static final byte RWSTAT = TWSTAT + 1;
}
