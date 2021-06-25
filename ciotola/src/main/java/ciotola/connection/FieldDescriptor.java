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
import java.lang.reflect.Field;

class FieldDescriptor {

  private int position;
  private int size;
  private Ciotola.ParseType parseType;
  private boolean isDataField;
  private int dataFieldSizeDescriptor;
  private Field field;

  public Field getField() {
    return field;
  }

  public void setField(Field field) {
    this.field = field;
  }

  public String getFieldName() {
    return field.getName();
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public Ciotola.ParseType getParseType() {
    return parseType;
  }

  public void setParseType(Ciotola.ParseType parseType) {
    this.parseType = parseType;
  }

  public boolean isDataField() {
    return isDataField;
  }

  public void setDataField(boolean dataField) {
    isDataField = dataField;
  }

  public int getDataFieldSizeDescriptor() {
    return dataFieldSizeDescriptor;
  }

  public void setDataFieldSizeDescriptor(int dataFieldSizeDescriptor) {
    this.dataFieldSizeDescriptor = dataFieldSizeDescriptor;
  }
}
