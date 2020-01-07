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

package fettuccine;

public class FettuccineVersion {
    public static final int VERSION_MAJOR = 1;
    public static final int VERSION_MINOR = 0;
    public static final String VERSION_NOTES = "(alpha)";
    public static final String COPYRIGHT = "Enzo Reyes";

    public static String getVersion() {
        String ver =  "Fettuccine version: "+Integer.toString(FettuccineVersion.VERSION_MAJOR) + "." + Integer.toString(FettuccineVersion.VERSION_MINOR) + " "+ VERSION_NOTES+ " Copyright: "+COPYRIGHT;
        return ver;
    }
}
