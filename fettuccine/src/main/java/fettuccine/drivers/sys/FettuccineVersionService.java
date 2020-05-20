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

package fettuccine.drivers.sys;

import common.api.fettuccine.FettuccineVersion;
import gelato.GelatoVersion;
import protocol.P9Protocol;

public class FettuccineVersionService implements FettuccineVersion {
  public static final int VERSION_MAJOR = 1;
  public static final int VERSION_MINOR = 0;
  public static final String VERSION_NOTES = "(alpha)";
  public static final String COPYRIGHT = "Enzo Reyes";

  public static String version() {
    String ver =
        "Fettuccine version: "
            + Integer.toString(FettuccineVersionService.VERSION_MAJOR)
            + "."
            + Integer.toString(FettuccineVersionService.VERSION_MINOR)
            + " "
            + VERSION_NOTES
            + " Copyright: "
            + COPYRIGHT;
    String gelato = GelatoVersion.getVersion();
    String protocol = P9Protocol.protocolVersion;
    ver = ver + "\n - Using: " + gelato + "\n - Library: " + protocol;
    return ver;
  }

  public static String versionValue() {
    return FettuccineVersionService.VERSION_MAJOR + "." + FettuccineVersionService.VERSION_MINOR;
  }

  @Override
  public String getVersion() {
    return version();
  }
}
