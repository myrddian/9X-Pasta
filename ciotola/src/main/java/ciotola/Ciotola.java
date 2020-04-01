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

package ciotola;

import ciotola.implementation.DefaultCiotolaContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Ciotola implements CiotolaContext{

    private final Logger logger = LoggerFactory.getLogger(Ciotola.class);
    private CiotolaContext serviceContainer = new DefaultCiotolaContainer();


    public CiotolaContext getServiceContainer() {
        return serviceContainer;
    }


    public void start() {
        serviceContainer.startContainer();
    }

    public void setServiceContainer(CiotolaContext serviceContainer) {
        this.serviceContainer = serviceContainer;
    }


    private static Ciotola SINGLE_INSTANCE = null;
    private Ciotola() {}
    public static Ciotola getInstance() {
        if (SINGLE_INSTANCE == null) {
            synchronized (Ciotola.class) {
                if (SINGLE_INSTANCE == null) {
                    SINGLE_INSTANCE = new Ciotola();
                }
            }
        }
        return SINGLE_INSTANCE;
    }

    //Program Strings
    public static final String GENERIC_SERVICE_START_ERROR = "Unable to start service";
    public static final String SERVICE_METHOD_ACCESS_ERROR = "Service Name Method not Accessible";
    public static final String SERVICE_INVOCATION_ERROR =  "Service Name invocation error";
    public static final String SERVICE_INIT_METHOD_ERROR = "Service specification is invalid, missing method decorators";
    public static final String CLASS_LOADING_ERROR = "Unable to load class";
    public static final String GENERAL_IO_ERROR = "An exception has occurred, IO Exception";
    public static final String CONNECTION_TIMEOUT = "The connection has timed out";
    public static final String MISSING_CONFIG_FILE_ERROR = "Configuration file is missing or not specified";
    public static final String UKNONWN_PARAMETERS = "Unable to parse the parameters specified";
    public static final String URL_CLASS_LOADER = "The URL is malformed or invalid while scanning";
    public static final String CLASS_LOADER_ERROR = "An error has been raised while loading classes";
    public static final String UNKNOWN_COMPONENT_SPECIFIED = "While injecting, a component was specified that cannot be found";
    public static final String FIELD_INJECTION_ERROR = "Unable to inject into the specified field";
    public static final String LOGGER_FIELD_INJECTION_ERROR = "While injecting the logger there has been an error";
    public static final String INVALID_CLASS_LOADER = "No class loader specified while starting container";
    public static final String CLASS_SCANNER_MISSING = "No JavaClassScanner specified - aborting";

    @Override
    public Collection<CiotolaServiceInterface> getServices() {
        return serviceContainer.getServices();
    }

    @Override
    public void addService(CiotolaServiceInterface newService) {
        serviceContainer.addService(newService);
    }

    @Override
    public void addService(Object newService) {
        serviceContainer.addService(newService);
    }

    @Override
    public void injectService(CiotolaServiceInterface newService) {
        serviceContainer.injectService(newService);
    }

    @Override
    public void injectService(Object newService) {
        serviceContainer.injectService(newService);
    }

    @Override
    public boolean startContainer() {
        return serviceContainer.startContainer();
    }

    @Override
    public void setPathToJarDelegate(CiotolaPathDelegateHelper pathToJarDelegate) {
        serviceContainer.setPathToJarDelegate(pathToJarDelegate);
    }

    @Override
    public CiotolaPathDelegateHelper getPathToJarDelegate() {
        return serviceContainer.getPathToJarDelegate();
    }

    @Override
    public List<String> getLoadedJars() {
        return serviceContainer.getLoadedJars();
    }

    @Override
    public void addAnnotation(Class annotation) {
        serviceContainer.addAnnotation(annotation);
    }

    @Override
    public List<String> getAnnotations() {
        return serviceContainer.getAnnotations();
    }

    @Override
    public void submitJob(Runnable job) {
        serviceContainer.submitJob(job);
    }

    @Override
    public ExecutorService getExecutorService() {
        return serviceContainer.getExecutorService();
    }

    @Override
    public void setExecutorService(ExecutorService executorService) {
        serviceContainer.setExecutorService(executorService);
    }
}