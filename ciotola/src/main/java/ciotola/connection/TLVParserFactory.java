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
import ciotola.annotations.AnnotatorHandler;
import ciotola.annotations.TLVDataField;
import ciotola.annotations.TLVHeaderField;
import ciotola.annotations.TLVMessage;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TLVParserFactory<TYPE> implements AnnotatorHandler, ParserFactory<TYPE> {

  private Map<Integer, FieldDescriptor> fieldDescriptorMap = new ConcurrentHashMap<>();
  private Ciotola.ByteOrder byteOrder;
  private Class classType;
  private int headerSize = 0;

  private void parseFields(Class typeFound) {
    Field[] fields = typeFound.getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      if (field.isAnnotationPresent(TLVHeaderField.class)) {
        TLVHeaderField headerField = field.getAnnotation(TLVHeaderField.class);
        FieldDescriptor newDescriptor = new FieldDescriptor();
        newDescriptor.setDataField(false);
        newDescriptor.setField(field);
        newDescriptor.setPosition(headerField.position());
        newDescriptor.setParseType(headerField.parseType());
        newDescriptor.setSize(headerField.size());
        fieldDescriptorMap.put(headerField.position(), newDescriptor);
        headerSize += headerField.size();
      } else if (field.isAnnotationPresent(TLVDataField.class)) {
        TLVDataField dataField = field.getAnnotation(TLVDataField.class);
        FieldDescriptor newDescriptor = new FieldDescriptor();
        newDescriptor.setField(field);
        newDescriptor.setDataField(true);
        newDescriptor.setDataFieldSizeDescriptor(dataField.specifier());
        newDescriptor.setPosition(dataField.position());
        newDescriptor.setSize(dataField.fieldSize());
        fieldDescriptorMap.put(dataField.position(), newDescriptor);
      }
    }
  }

  @Override
  public void handle(Class typeFound) {
    if (typeFound.isAnnotationPresent(TLVMessage.class)) {
      TLVMessage desc = (TLVMessage) typeFound.getAnnotation(TLVMessage.class);
      this.byteOrder = desc.byteOrder();
      parseFields(typeFound);
      this.classType = typeFound;
    }
  }

  @Override
  public TYPE getParser() {
    return null;
  }
}
