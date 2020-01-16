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

package gelato;

import java.util.*;

public class GelatoTags {
    private int currentTagClient = 0;
    private Map<Integer, Boolean> inFlightClient = new HashMap<>();

    public int generateTag() {
        if(currentTagClient > 65000) {
            currentTagClient = 1;
        } else {
            ++currentTagClient;
        }
        inFlightClient.put(currentTagClient,true);
        return currentTagClient;
    }

    public void closeTag(int tag) {
        inFlightClient.remove(tag);
    }

    public boolean validTag(int tag) {
        return inFlightClient.containsKey(tag);
    }

    //server functionality
    private int tagCount = 0;


    public boolean isRecycled(int tag) {
        if(tag >= tagCount) {
            return false;
        }
        return true;
    }

    public void registerTag(int tag) {
        tagCount = tag;
    }

    public int getTagCount() {
        return tagCount;
    }

}
