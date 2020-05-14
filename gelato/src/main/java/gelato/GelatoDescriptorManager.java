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

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class GelatoDescriptorManager {
  private Map<Long, Boolean> usedFid = new ConcurrentHashMap<>();
  private Map<Long, GelatoFileDescriptor> qidMap = new ConcurrentHashMap<>();

  private Random generator = new Random();

  public void mapQID(GelatoFileDescriptor descriptor, GelatoFileDescriptor serverResource) {
    qidMap.put(descriptor.getDescriptorId(), serverResource);
    usedFid.put(descriptor.getDescriptorId(), true);
  }

  public GelatoFileDescriptor getServerDescriptor(GelatoFileDescriptor clientDescriptor) {
    return qidMap.get(clientDescriptor.getDescriptorId());
  }

  public void removeServerResourceMap(GelatoFileDescriptor clientDescriptor) {
    qidMap.remove(clientDescriptor.getDescriptorId());
  }

  public GelatoFileDescriptor generateDescriptor() {
    GelatoFileDescriptor newDescriptor = new GelatoFileDescriptor();
    int val = generator.nextInt();
    if (usedFid.containsKey(val)) {
      return generateDescriptor();
    }
    newDescriptor.setRawFileDescriptor(val);
    newDescriptor.getQid().setLongFileId(newDescriptor.getDescriptorId());
    usedFid.put(newDescriptor.getDescriptorId(), true);

    return newDescriptor;
  }

  public boolean validDescriptor(GelatoFileDescriptor descriptor) {
    if (usedFid.containsKey(descriptor.getDescriptorId())) {
      return true;
    }
    return false;
  }

  public int size() {
    return usedFid.size();
  }

  public boolean registerDescriptor(GelatoFileDescriptor newDescriptor) {
    if (usedFid.containsKey(newDescriptor.getDescriptorId())) {
      return false;
    }
    usedFid.put(newDescriptor.getDescriptorId(), true);
    return true;
  }

  public void removeDescriptor(GelatoFileDescriptor descriptor) {
    usedFid.remove(descriptor.getDescriptorId());
  }
}
