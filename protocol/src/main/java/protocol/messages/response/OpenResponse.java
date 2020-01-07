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

package protocol.messages.response;

import protocol.*;

public class OpenResponse {
    private int tag;
    private QID fileQID;
    private int sizeIO;

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public QID getFileQID() {
        return fileQID;
    }

    public void setFileQID(QID fileQID) {
        this.fileQID = fileQID;
    }

    public int getSizeIO() {
        return sizeIO;
    }

    public void setSizeIO(int sizeIO) {
        this.sizeIO = sizeIO;
    }
}
