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

package ciotola;

import ciotola.annotations.TLVHeaderField;
import ciotola.annotations.TLVMessage;

@TLVMessage(byteOrder = Ciotola.ByteOrder.LITTLE_ENDIAN)
public class TestTLVMesg {

  @TLVHeaderField(
      position = Ciotola.INITIAL_POSITION,
      size = Ciotola.DEFAULT_INT_SIZE,
      parseType = Ciotola.ParseType.SIGNED_INT)
  private int intValue;

  @TLVHeaderField(
      position = 1,
      size = Ciotola.DEFAULT_INT_SIZE,
      parseType = Ciotola.ParseType.UNSIGNED_SHORT_TO_INT)
  private int secondInt;
}
