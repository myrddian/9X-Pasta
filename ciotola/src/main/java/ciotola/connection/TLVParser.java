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

import ciotola.Ciotola;
import java.util.HashMap;
import java.util.Map;

public final class TLVParser<TYPE> implements BufferHandler<byte[]> {

  private Map<Integer, FieldDescriptor> propertyMap;
  private Class type;
  private int headerSize;
  private Ciotola.ByteOrder byteOrder;
  private StagingBuffer stagingBuffer;
  private byte[] allocBuffer;
  private STAGE currentStage = STAGE.HEADER;
  private int headerPosition = 0;
  private int bufferPosition = 0;
  private Map<Integer, Object> fieldValues = new HashMap<>();

  public TLVParser(
      Map<Integer, FieldDescriptor> propertyMap,
      Class type,
      int headerSize,
      Ciotola.ByteOrder byteOrder,
      StagingBuffer previousBuffer) {
    this.propertyMap = propertyMap;
    this.headerSize = headerSize;
    this.byteOrder = byteOrder;
    this.type = type;
    this.stagingBuffer = previousBuffer;
    this.stagingBuffer.setTriggerLevel(headerSize);
  }

  private void readHeader(byte[] data) {
    ByteHandler.copyBytesTo(data, allocBuffer, 0, data.length);
    int numFields = propertyMap.size();
    int decorderPosition = 0;
    for (int index = 0; index < numFields; ++index) {
      FieldDescriptor descriptor = propertyMap.get(index);
      switch (descriptor.getParseType()) {
        case SIGNED_INT:
          int value = ByteHandler.decodeInt(allocBuffer, decorderPosition);
          decorderPosition += ByteHandler.MSG_INT_SIZE;
          fieldValues.put(index, value);
          break;
        case UNSIGNED_INT_TO_LONG:
          long unsignedInt = ByteHandler.decodeUnsignedInt(allocBuffer, decorderPosition);
          decorderPosition += ByteHandler.MSG_INT_SIZE;
          fieldValues.put(index, unsignedInt);
          break;
      }
    }
  }

  @Override
  public void read(ChannelBuffer<byte[]> channelBuffer) {
    if (currentStage == STAGE.HEADER) {
      readHeader(channelBuffer.read());
    } else {

    }
  }

  private enum STAGE {
    HEADER,
    DATA
  }
}
