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

package agnolotti.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class ForName {

  private static final Map<String, Class<?>> PRIM =
      (Collections.unmodifiableMap(
          new HashMap<String, Class<?>>(16) {
            {
              for (Class<?> cls :
                  new Class<?>[]{
                      void.class,
                      boolean.class,
                      char.class,
                      byte.class,
                      short.class,
                      int.class,
                      long.class,
                      float.class,
                      double.class
                  }) {
                put(cls.getName(), cls);
              }
            }
          }));

  private ForName() {
  }

  public static Class<?> forName(final String name) throws ClassNotFoundException {
    final Class<?> prim = PRIM.get(name);

    if (prim != null) {
      return prim;
    }

    return Class.forName(name);
  }

  public static boolean isPrimitive(final String name) {
    return PRIM.containsKey(name);
  }
}
