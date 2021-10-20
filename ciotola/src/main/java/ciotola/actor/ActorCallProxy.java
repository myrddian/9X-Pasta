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

import java.util.ArrayList;

final class ActorCallProxy implements ActorCall{

  private RoleImpl role;
  private String methodName;

  public ActorCallProxy(RoleImpl role, String methodName) {
    this.role = role;
    this.methodName = methodName;
  }

  @Override
  public CiotolaFuture call(Object... values) {
    ArrayList<Object> varArg = new ArrayList<>();

    for(Object val:values) {
      varArg.add(val);
    }
    MethodParamStack val = new MethodParamStack();
    val.parameters = varArg.toArray();
    val.methodName = methodName;
    return role.send(val);
  }
}
