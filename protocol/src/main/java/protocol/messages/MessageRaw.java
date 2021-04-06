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

package protocol.messages;

import protocol.Decoder;
import protocol.P9Protocol;

public class MessageRaw {
  public static final int minSize =
      P9Protocol.MSG_SIZE_HEADER + P9Protocol.MSG_TYPE_SIZE + P9Protocol.MSG_TAG_SIZE;
  public static final int tagLocation = P9Protocol.MSG_SIZE_HEADER + P9Protocol.MSG_TYPE_SIZE;
  public byte[] size = new byte[P9Protocol.MSG_SIZE_HEADER];
  public byte type;
  public byte[] tag = new byte[P9Protocol.MSG_TAG_SIZE];
  public byte[] data;

  public Message toMessage() {
    return Decoder.decodeToMessage(this);
  }
}
