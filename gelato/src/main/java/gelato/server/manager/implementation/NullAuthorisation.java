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

package gelato.server.manager.implementation;

import gelato.GelatoConnection;
import gelato.GelatoFileDescriptor;
import gelato.GelatoSession;
import gelato.server.manager.GelatoAuthorisationManager;
import protocol.QID;
import protocol.messages.request.AuthRequest;

public class NullAuthorisation implements GelatoAuthorisationManager {

  @Override
  public boolean requireAuth() {
    return false;
  }

  @Override
  public boolean processAuthRequest(
      GelatoConnection connection,
      GelatoFileDescriptor descriptor,
      GelatoSession session,
      AuthRequest request) {
    return false;
  }

  @Override
  public QID authoriseQID(GelatoFileDescriptor descriptor) {
    return null;
  }

  @Override
  public GelatoFileDescriptor getAuthorisedDescriptor(
      GelatoConnection connection, GelatoFileDescriptor descriptor) {
    return null;
  }
}
