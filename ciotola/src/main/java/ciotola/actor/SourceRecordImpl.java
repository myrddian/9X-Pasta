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

package ciotola.actor;

final class SourceRecordImpl<T> implements SourceRecord {

  private T recordValue;
  private long itemKey;
  private String source;


  public SourceRecordImpl(
      T value,
      long itemKey,
      String source
  ) {
    recordValue = value;
    this.itemKey = itemKey;
    this.source = source;
  }

  @Override
  public Object getValue() {
    return recordValue;
  }

  @Override
  public Long getKey() {
    return itemKey;
  }

  @Override
  public String getPort() {
    return source;
  }
}
