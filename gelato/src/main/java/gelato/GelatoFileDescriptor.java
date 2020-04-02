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

package gelato;

import protocol.ByteEncoder;
import protocol.QID;

public class GelatoFileDescriptor {

  private int fileId;
  private QID qid = new QID();

  public long getDescriptorId() {
    return ByteEncoder.getUnsigned(fileId);
  }

  public void setDescriptorId(long newDescriptorId) {
    fileId = ByteEncoder.toUnsigned(newDescriptorId);
  }

  public QID getQid() {
    return qid;
  }

  public void setQid(QID qid) {
    this.qid = qid;
  }

  public int getRawFileDescriptor() {
    return fileId;
  }

  public void setRawFileDescriptor(int fileId) {
    this.fileId = fileId;
  }

  @Override
  public int hashCode() {
    return fileId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj.hashCode() == hashCode()) {
      return true;
    }
    return false;
  }
}
