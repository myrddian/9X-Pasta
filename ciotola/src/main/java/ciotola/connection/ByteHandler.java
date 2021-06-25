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

package ciotola.connection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ByteHandler {

  public static final int MSG_INT_SIZE = 4;
  public static final int MSG_LONG_SIZE = 8;
  public static final int MSG_SHORT_SIZE = 2;

  public static void copyBytesTo(byte[] input, byte[] out, int start, int size) {
    for (int counter = 0; counter < size; ++counter) {
      out[start + counter] = input[counter];
    }
  }

  public static void copyBytesFrom(byte[] input, byte[] out, int start, int size) {
    for (int counter = 0; counter < size; ++counter) {
      out[counter] = input[start + counter];
    }
  }

  public static int decodeInt(byte[] buffer, int position) {
    byte[] numBuffer = new byte[MSG_INT_SIZE];
    copyBytesFrom(buffer, numBuffer, position, MSG_INT_SIZE);
    ByteBuffer bytesBuffer = ByteBuffer.allocate(MSG_INT_SIZE);
    bytesBuffer = bytesBuffer.order(ByteOrder.LITTLE_ENDIAN);
    bytesBuffer.put(numBuffer);
    bytesBuffer.flip();
    return bytesBuffer.getInt();
  }

  public static long decodeUnsignedInt(byte[] buffer, int position) {
    return getUnsigned(decodeInt(buffer, position));
  }

  public static long getUnsigned(int signed) {
    return signed >= 0 ? signed : 2 * (long) Integer.MAX_VALUE + 2 + signed;
  }

  public static int toUnsigned(long value) {
    String lngString = Long.toString(value);
    return Integer.parseUnsignedInt(lngString);
  }

}
