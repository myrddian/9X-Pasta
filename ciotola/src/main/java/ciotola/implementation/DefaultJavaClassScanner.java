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
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DefaultJavaClassScanner implements CiotolaJavaClassScanner {
    public final static Logger logger = LoggerFactory.getLogger(DefaultJavaClassScanner.class);
    private ClassLoader classLoader;

    @Override
    public List<Class> scanJar(String pathToJar) {
        List<Class> retList = new ArrayList<>();
        if(pathToJar.length() < 3) {
            return retList;
        }
        String isJar = pathToJar.toLowerCase().substring(pathToJar.length()-3, pathToJar.length());
        if(!isJar.equals("jar")) {
            logger.info("Not a JAR: " + pathToJar );
            logger.info("Possible directory -> Attempting to scan for classes at specified path: " + pathToJar);
            return loadClassesInDirectory(pathToJar);
        }
        logger.info("Scanning JAR: " + pathToJar);

        try {
            JarFile jarFile = new JarFile(pathToJar);
            Enumeration<JarEntry> entries = jarFile.entries();
            URL[] urls = {new URL("jar:file:" + pathToJar + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls);

            while (entries.hasMoreElements()) {
                JarEntry je = entries.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                Class loadClass = cl.loadClass(className);
                retList.add(loadClass);
            }

        }catch (MalformedURLException e) {
            logger.error(Ciotola.URL_CLASS_LOADER,e);
        } catch (IOException e) {
            logger.error(Ciotola.GENERAL_IO_ERROR,e);
        } catch (ClassNotFoundException e) {
            logger.error(Ciotola.CLASS_LOADER_ERROR,e);
        }
        return retList;
    }

    @Override
    public List<Class> scanJar(String... jars) {
        List<Class> reList = new ArrayList<>();
        String firstPath = jars[0];
        String isJar = firstPath.toLowerCase().substring(firstPath.length()-3, firstPath.length());

        if(isJar.equals("r!/")) {
            reList.addAll(scanAndLoadAll());
        } else {
            for(String jar: jars) {
                reList.addAll(scanJar(jar));
            }

        }

        return reList;
    }

    @Override
    public void setClassLoader(ClassLoader loader) {
        classLoader = loader;
    }

    @Override
    public ClassLoader getLoader() {
        return classLoader;
    }

    private  List<Class> loadClassesInDirectory(String directory) {
        List<Class> returnValue = new ArrayList<>();
        List<String> currentPackages = findLoadedPackagesInCurrentLoader();
        for(String loadedPackage: currentPackages) {
            try {
                returnValue.addAll(getClasses(loadedPackage));
            } catch (IOException e) {
                logger.error(Ciotola.GENERAL_IO_ERROR, e);
            } catch (ClassNotFoundException e) {
                logger.error(Ciotola.CLASS_LOADING_ERROR, e);
            }
        }
        return returnValue;
    }

    private List<String> findLoadedPackagesInCurrentLoader() {
        Package [] pkgArray = Package.getPackages();
        ArrayList<String> packages = new ArrayList<>();

        for(Package pkg: pkgArray) {
            packages.add(pkg.getName().replaceAll("[.]", "/"));
        }

        return packages;
    }

    private List<Class> getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList();
        while (resources.hasMoreElements()) {
            URL resource = (URL) resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        for(File file: dirs) {
            logger.trace("Directory Inspection " + file.getName());
        }
        ArrayList<Class> classes = new ArrayList();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            logger.trace("No Classes - :(");
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName .replace('/', '.')+ '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    private List<Class> scanAndLoadAll() {
        List<Class> retList = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().verbose().enableClassInfo().scan()) {
            retList.addAll(scanResult.getAllClasses().loadClasses());
        }

        return retList;
    }

}
