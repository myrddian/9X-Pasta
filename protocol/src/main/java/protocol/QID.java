/*
 * Copyright (c) 2021.  Enzo Reyes Licensed under the Apache License, Version 2.0 (the "License");   you may
 * not use this file except in compliance with the License.   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package protocol;

public class QID {

  public static int QID_DIR = 0x80;
  public static int QID_APPEND = 0x40;
  public static int QID_EXCLUSIVE = 0x20;
  public static int QID_MOUNT = 0x10;
  public static int QID_AUTH = 0x08;
  public static int QID_TMP = 0x04;
  public static int QID_FILE = 0x00;
  public static int QID_AUTH_NOT_REQUIRED = 0x18;

  private long longFileId;
  private int version;
  private byte type;

  public long getLongFileId() {
    return longFileId;
  }

  public void setLongFileId(long longFileId) {
    this.longFileId = longFileId;
  }

  public long getVersion() {
    return ByteEncoder.getUnsigned(version);
  }

  public void setVersion(long newVersion) {
    version = ByteEncoder.toUnsigned(newVersion);
  }

  public int getVersionRaw() {
    return version;
  }

  public byte getType() {
    return type;
  }

  public void setType(byte type) {
    this.type = type;
  }
}
