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

package gelato.client.file;

import gelato.client.file.GelatoFile;
import protocol.P9Protocol;

import java.io.IOException;
import java.io.InputStream;

public class GelatoInputStream extends InputStream {

  private GelatoFile file;
  private int currentLocation = 0;
  private byte[] buffer = new byte[P9Protocol.DEFAULT_MSG_SIZE];

  @Override
  public int read() throws IOException {
    return 0;
  }
}
