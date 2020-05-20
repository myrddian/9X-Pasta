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

import common.api.fettuccine.FettuccineConstants;
import common.api.fettuccine.Mount;
import fettuccine.drivers.mount.MountPoint;
import gelato.server.GelatoServerManager;
import gelato.server.manager.controllers.GelatoDirectoryController;
import gelato.server.manager.controllers.impl.GelatoDirectoryControllerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MountService implements Mount {

  private final Logger logger = LoggerFactory.getLogger(MountService.class);
  private ConcurrentMap<String, MountPoint> mountPoints = new ConcurrentHashMap<>();
  private GelatoDirectoryController mountDirectory;
  private GelatoServerManager serverManager;

  public MountService(GelatoServerManager manager) {
    serverManager = manager;
    mountDirectory = new GelatoDirectoryControllerImpl(manager);
    mountDirectory.setDirectoryName(FettuccineConstants.MOUNT_DIR);
    mountDirectory
        .getQID()
        .setLongFileId(manager.getDescriptorManager().generateDescriptor().getDescriptorId());
    serverManager.getRoot().addDirectory(mountDirectory);
  }

  public GelatoServerManager getServerManager() {
    return serverManager;
  }

  public GelatoDirectoryController getMountDirectory() {
    return mountDirectory;
  }

  @Override
  public boolean mount(String server, int port, String userName, String point) {
    try {
      MountPoint mountPoint = new MountPoint(server, port, userName, point, this);
      mountPoints.put(point, mountPoint);
      mountDirectory.addDirectory(mountPoint.getDirectory());
      return true;
    } catch (IOException e) {
      logger.error("Failed to mount : " + point);
      logger.error("Exception ", e);
    }
    return false;
  }

  @Override
  public List<String> listMounts() {
    List<String> retlist = new ArrayList<>();

    for (MountPoint mount : mountPoints.values()) {
      retlist.add(mount.getMountDetail());
    }
    return retlist;
  }
}
