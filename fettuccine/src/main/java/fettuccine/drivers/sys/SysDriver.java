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

import agnolotti.server.ServiceManager;
import common.api.fettuccine.FettuccineConstants;
import common.api.fettuccine.FettuccineNameSpace;
import common.api.fettuccine.FettuccineVersion;
import common.api.fettuccine.Mount;
import gelato.server.GelatoServerManager;

public class SysDriver {


    private ServiceManager remoteServices;


    public SysDriver(GelatoServerManager manager) {
        remoteServices = new ServiceManager(manager, FettuccineConstants.SYS_DIR,
                FettuccineConstants.FETTUCCINE_SVC_GRP,FettuccineVersionService.versionValue());
        remoteServices.addRemoteService(FettuccineVersion.class, new FettuccineVersionService());
        remoteServices.addRemoteService(Mount.class, new MountService(manager));
        remoteServices.addRemoteService(FettuccineNameSpace.class, new NameSpace(manager));
    }




}
