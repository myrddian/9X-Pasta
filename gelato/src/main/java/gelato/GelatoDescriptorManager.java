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

public class GelatoDescriptorManager {
    private Map<Integer, Boolean> usedFid = new HashMap<>();
    private Random generator = new Random();

    public GelatoFileDescriptor generateDescriptor () {
        GelatoFileDescriptor newDescriptor = new GelatoFileDescriptor();
        int val = generator.nextInt();
        if(usedFid.containsKey(val)) {
            return generateDescriptor();
        }
        usedFid.put(val,true);
        newDescriptor.setFileId(val);
        return newDescriptor;
    }

    public int size() {
        return usedFid.size();
    }

    public boolean registerDescriptor(GelatoFileDescriptor newDescriptor) {
        if(usedFid.containsKey(newDescriptor.getDescriptorId())) {
            return false;
        }
        usedFid.put(newDescriptor.getDescriptorId(),true);
        return true;
    }

    public void removeDescriptor(GelatoFileDescriptor descriptor) {
        usedFid.remove(descriptor.getDescriptorId());
    }
}
