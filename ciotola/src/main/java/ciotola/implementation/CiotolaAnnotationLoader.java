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

package ciotola.implementation;

import ciotola.Ciotola;
import ciotola.CiotolaJavaClassScanner;
import ciotola.CiotolaServiceInterface;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ciotola.annotations.CiotolaService;

public class CiotolaAnnotationLoader  {


    private ArrayList<CiotolaServiceInterface> serviceList = new ArrayList<>();
    private ArrayList<Object> loadedObjects = new ArrayList<>();
    private Map<String, Object> componentList = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(CiotolaAnnotationLoader.class);
    private CiotolaJavaClassScanner scanner = new DefaultJavaClassScanner();

    public boolean initialize(String ...loadJar) {
        ClassLoader mainThread = Thread.currentThread().getContextClassLoader();
        if(scanner == null) {
            logger.error(Ciotola.CLASS_SCANNER_MISSING);
            return false;
        }
        scanner.setClassLoader(mainThread);
        List<Class> classList = new ArrayList<>();
        classList.addAll(scanner.scanJar(loadJar));
        for(Class tmpClass: classList) {
            logger.trace("Scanning for Annotations " + tmpClass.getName());
            if(!processService(tmpClass)) {
                return false;
            }
        }
        return true;
    }




    private boolean processService(Class scanClass) {
        Annotation[] annotations = scanClass.getAnnotations();

        for(Annotation annotation: annotations) {
            if(annotation instanceof CiotolaService) {
                logger.trace("Found Service - " + scanClass.getName());
                try {
                    Constructor ctor = scanClass.getConstructor();
                    Object newInstance = ctor.newInstance();
                    AnnotatedJavaServiceRunner javaServiceRunner = new AnnotatedJavaServiceRunner(newInstance);
                    serviceList.add(javaServiceRunner);
                    loadedObjects.add(newInstance);
                    break;
                } catch (Exception ex) {
                    logger.error(Ciotola.CLASS_LOADING_ERROR, ex);
                    return false;
                }
            }

        }
        return true;
    }

}
