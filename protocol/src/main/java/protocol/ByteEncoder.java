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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteEncoder {

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

  public static void encodeQID(QID value, byte[] buffer, int position) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(P9Protocol.MSG_QID_SIZE);
    byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    byteBuffer.put(value.getType());
    byteBuffer.putInt(value.getVersionRaw());
    byteBuffer.putLong(value.getLongFileId());
    copyBytesTo(byteBuffer.array(), buffer, position, P9Protocol.MSG_QID_SIZE);
  }

  public static QID decodeQID(byte[] buffer, int position) {

    long fileId = 0;
    byte type = 0;
    int version = 0;

    type = buffer[position];
    version = decodeInt(buffer, position + 1);
    fileId = decodeInt(buffer, position + 3);
    QID retVal = new QID();
    retVal.setLongFileId(fileId);
    retVal.setVersion(version);
    retVal.setType(type);
    return retVal;
  }

  public static void encodeLong(long value, byte[] buffer, int position) {
    ByteBuffer bytesBuffer = ByteBuffer.allocate(P9Protocol.MSG_LONG_SIZE);
    bytesBuffer = bytesBuffer.order(ByteOrder.LITTLE_ENDIAN);
    bytesBuffer.putLong(value);
    copyBytesTo(bytesBuffer.array(), buffer, position, P9Protocol.MSG_LONG_SIZE);
  }

  public static long decodeLong(byte[] buffer, int position) {
    byte[] numBuffer = new byte[P9Protocol.MSG_LONG_SIZE];
    copyBytesFrom(buffer, numBuffer, position, P9Protocol.MSG_LONG_SIZE);
    ByteBuffer bytesBuffer = ByteBuffer.allocate(P9Protocol.MSG_LONG_SIZE);
    bytesBuffer = bytesBuffer.order(ByteOrder.LITTLE_ENDIAN);
    bytesBuffer.put(numBuffer);
    bytesBuffer.flip();
    return bytesBuffer.getLong();
  }

  public static void encodeInt(int value, byte[] buffer, int position) {
    ByteBuffer bytesBuffer = ByteBuffer.allocate(P9Protocol.MSG_FID_SIZE);
    bytesBuffer = bytesBuffer.order(ByteOrder.LITTLE_ENDIAN);
    bytesBuffer.putInt(value);
    copyBytesTo(bytesBuffer.array(), buffer, position, P9Protocol.MSG_FID_SIZE);
  }

  public static int decodeInt(byte[] buffer, int position) {
    byte[] numBuffer = new byte[P9Protocol.MSG_FID_SIZE];
    copyBytesFrom(buffer, numBuffer, position, P9Protocol.MSG_FID_SIZE);
    ByteBuffer bytesBuffer = ByteBuffer.allocate(P9Protocol.MSG_FID_SIZE);
    bytesBuffer = bytesBuffer.order(ByteOrder.LITTLE_ENDIAN);
    bytesBuffer.put(numBuffer);
    bytesBuffer.flip();
    return bytesBuffer.getInt();
  }

  public static int decodeShort(byte[] buffer, int position) {
    byte[] numBuffer = new byte[P9Protocol.MSG_TAG_SIZE];
    copyBytesFrom(buffer, numBuffer, position, P9Protocol.MSG_TAG_SIZE);
    ByteBuffer bytesBuffer = ByteBuffer.allocate(P9Protocol.MSG_TAG_SIZE);
    bytesBuffer = bytesBuffer.order(ByteOrder.LITTLE_ENDIAN);
    bytesBuffer.put(numBuffer);
    bytesBuffer.flip();
    short val = bytesBuffer.getShort();
    return (int) val;
  }

  public static void encodeShort(int value, byte[] buffer, int position) {
    short shorted = (short) value;
    byte[] shortBuffer = new byte[P9Protocol.MSG_TAG_SIZE];
    ByteBuffer bytesBuffer = ByteBuffer.allocate(P9Protocol.MSG_TAG_SIZE);
    bytesBuffer = bytesBuffer.order(ByteOrder.LITTLE_ENDIAN);
    bytesBuffer.putShort(shorted);
    bytesBuffer.flip();
    copyBytesTo(bytesBuffer.array(), buffer, position, P9Protocol.MSG_TAG_SIZE);
  }

  public static byte[] encodeStringToBuffer(String value) {
    byte[] stringBytes = encodeStringUTF8(value);
    int totalSegmentSize = P9Protocol.MSG_TAG_SIZE + stringBytes.length;
    byte[] buffer = new byte[totalSegmentSize];
    encodeShort(stringBytes.length, buffer, 0);
    copyBytesTo(stringBytes, buffer, P9Protocol.MSG_TAG_SIZE, stringBytes.length);
    return buffer;
  }

  public static int encodeString(byte[] buffer, int position, String value) {
    byte[] byteString = encodeStringUTF8(value);
    encodeShort(byteString.length, buffer, position);
    copyBytesTo(byteString, buffer, position + P9Protocol.MSG_TAG_SIZE, byteString.length);
    return P9Protocol.MSG_TAG_SIZE + byteString.length;
  }

  public static int stringLength(String value) {
    return P9Protocol.MSG_TAG_SIZE + encodeStringUTF8(value).length;
  }

  public static byte[] encodeStringUTF8(String value) {
    return value.getBytes(StandardCharsets.UTF_8);
  }

  public static String decodeString(byte[] buffer, int position) {
    int start = position + P9Protocol.MSG_TAG_SIZE;
    int end = decodeShort(buffer, position) + start;
    return new String(Arrays.copyOfRange(buffer, start, end));
  }

  public static int stringLength(byte[] buffer, int position) {
    int end = decodeShort(buffer, position);
    return end;
  }

  public static long getUnsigned(int signed) {
    return signed >= 0 ? signed : 2 * (long) Integer.MAX_VALUE + 2 + signed;
  }

  public static int toUnsigned(long value) {
    String lngString = Long.toString(value);
    return Integer.parseUnsignedInt(lngString);
  }
}
