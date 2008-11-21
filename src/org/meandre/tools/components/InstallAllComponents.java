/*
 * @(#) InstallAllComponents.java @VERSION@
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.tools.components;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.meandre.annotations.PackageDependency;

/**This class creates a script to upload all the components
 * and the dependencies to the meandre server.
 *
 *
 *
 * @author Amit Kumar
 * Created on Dec 28, 2007 10:29:56 PM
 *
 */
public class InstallAllComponents {


    private final static String PROPERTY_FILE = "meandre-server.properties";
    private static String sourceDir = "src/monkproject";
    private static ArrayList<String> alist = new ArrayList<String>(100);
    static ArrayList<String> packageList = new ArrayList<String>(10);
    static HashMap<String,
            String[]> dependencyList = new HashMap<String, String[]>(20);
    private static String EXECUTABLE_COMPONTONENT_INTERFACE
            = "org.meandre.core.ExecutableComponent";
    private static String PACKAGE_DEPENDNCY_INTERFACE
            = "org.monkproject.meandre.annotations.PackageDependency";
    private static String SCRIPT_FOLDER = "scripts";
    private static String UPLOAD_SCRIPT_FILE = "upload.sh";


    public static void main(String[] args) {

        URL resource = InstallAllComponents.class.getResource(PROPERTY_FILE);
        if (resource == null) {
            System.out.println("Missing property file cannot continue. Check for meandre-server.properties file." +
                               "It needs to be in the classpath.");
            System.exit(0);
        } else {
            System.out.println("resource is: " + resource.toString());
        }

        InputStream is = InstallAllComponents.class.getResourceAsStream(
                PROPERTY_FILE);

        /*	FileInputStream pin=null;
         try {
          pin = new FileInputStream(is);
         } catch (FileNotFoundException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
         }
         */
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (args.length != 4) {
            System.out.println("Missing arguments.");
            System.exit(0);
        }
        String libraryResourcesDir = args[0];
        sourceDir = args[1];
        String descriptorDir = args[2];
        String componentJarFiles = args[3];

        String[] componentJarList = componentJarFiles.split(",");

        if (!(new File(libraryResourcesDir)).exists()) {
            System.out.println("The library resource directory: " +
                               libraryResourcesDir + " does not exist.");
            System.exit(0);
        }

        if (!(new File(sourceDir)).exists()) {
            System.out.println("The source directory: " + sourceDir +
                               " does not exist.");
            System.exit(0);
        }

        if (!(new File(descriptorDir)).exists()) {
            System.out.println("The descriptor directory: " + descriptorDir +
                               " does not exist.");
            System.exit(0);
        }

        File scriptDir = new File(SCRIPT_FOLDER);
        if (!scriptDir.exists()) {
            scriptDir.mkdir();
        }

        File[] componentJarFileList = new File[componentJarList.length]; //new File(componentJar);

        for (int k = 0; k < componentJarList.length; k++) {
            componentJarFileList[k] = new File(componentJarList[k]);
            if (!(componentJarFileList[k]).exists()) {
                System.out.println("The component jar file does not exist. " +
                                   componentJarFileList[k].getAbsolutePath());
                System.exit(0);
            }

        }

        Class packageDependencyClass = null;
        try {
            packageDependencyClass = Class.forName(PACKAGE_DEPENDNCY_INTERFACE);
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // get java source files
        getFiles(sourceDir);
        // filter the classes out that do not have the interface
        filterClasses(EXECUTABLE_COMPONTONENT_INTERFACE);
        // get the package list
        getPackageList();
        System.out.println("package list: " + packageList.size());
        Iterator<String> itPackage = packageList.iterator();
        String packagename = null;
        while (itPackage.hasNext()) {
            packagename = itPackage.next();
            System.out.println(packagename);
            try {
                Class klass = Class.forName(packagename + ".package-info");
                Annotation annon = klass.getAnnotation(packageDependencyClass);

                if (annon != null) {
                    dependencyList.put(packagename,
                                       ((PackageDependency) annon).dependencyList());
                }

            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //String packageClasses[] = getPackageClasses(packagename);
        }

        String serverurl = props.getProperty("serverurl");
        String serverlogin = props.getProperty("serverlogin");
        String serverpassword = props.getProperty("serverpassword");
        String serverport = props.getProperty("serverport", "1714");
        String curlUrl = null;

        int protocolLoc = serverurl.indexOf("http://");
        int protocolLen = "http://".length();
        if (protocolLoc == -1) {
            protocolLoc = serverurl.indexOf("https://");
            protocolLen = "https://".length();
        }

        curlUrl = serverurl.substring(0, protocolLen) + serverlogin + ":" +
                  serverpassword + "@" +
                  serverurl.substring(protocolLoc + protocolLen);

        int serverport_i = Integer.parseInt(serverport);

        InstallComponent icomponent = new InstallComponent(serverurl,
                serverport_i, serverlogin, serverpassword);
        icomponent.init(libraryResourcesDir);

        itPackage = packageList.iterator();
        String packageName = null;
        String descDir = null;
        BufferedWriter bwriter = null;
        try {
            bwriter = new BufferedWriter(new FileWriter(new File(scriptDir, UPLOAD_SCRIPT_FILE)));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        while (itPackage.hasNext()) {
            packageName = itPackage.next();
            System.out.println("Package name: " + packageName);
            descDir = packageName.replaceAll("\\.", "/");
            File file = new File(descriptorDir, descDir);
            File[] files = file.listFiles(new DescriptorFilenameFilter());
            if (files != null) {
                System.out.println(file.listFiles().length);
            } else {
                System.out.println("Error: " + file.getAbsolutePath());
                continue;
            }

            String[] jarDependency = dependencyList.get(packageName);
            if (jarDependency == null) {
               System.out.println("Error: dependency not found... "  +packageName);
                continue;
            }

            for (int i = 0; i < files.length; i++) {
                System.out.println("Component: " + files[i].getName());
                try {
                    bwriter.append("\ncurl -F overwrite=true -F repository=@" +
                                   files[i].getAbsolutePath().replaceAll("\\\\","/")  + " ");
                    for (int k = 0; k < jarDependency.length; k++) {
                        File jarFile = InstallComponent.appJarList.get(
                                jarDependency[k]);
                        System.out.println("-->" +jarDependency[k]);
                        bwriter.append("-F  context=@" + jarFile.getAbsolutePath().replaceAll("\\\\","/") +
                                       " ");
                    }

                    for (int k = 0; k < componentJarFileList.length; k++) {
                        bwriter.append("-F context=@" +
                                       componentJarFileList[k].getAbsolutePath().replaceAll("\\\\","/")  +
                                       " ");
                    }

                    bwriter.append(curlUrl);
                    bwriter.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //icomponent.uploadComponent(files[i], jarDependency);
            }

        }

        try {
            bwriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println(
                "The script for uploading the components has been created. ");
    }

    // get all the classes that belong a particular package
    private static String[] getPackageClasses(String packagename) {
        Iterator<String> itclassname = alist.iterator();
        // classname with package
        String classname_p = null;
        // simple classname
        String classname = null;
        // package name of the class under consideration
        String classpackage = null;
        ArrayList<String> classlist = new ArrayList<String>(10);

        while (itclassname.hasNext()) {
            classname_p = itclassname.next();
            int i = classname_p.lastIndexOf('.');
            if (i != -1) {
                classname = classname_p.substring(i + 1);
                classpackage = classname_p.substring(0, i);
                if (classpackage.equals(packagename)) {
                    classlist.add(classname_p);
                }
            }
        }
        return classlist.toArray(new String[classlist.size()]);
    }

    // get all the java source code files
    private static void getFiles(String srcFolder) {
        File file = new File(srcFolder);
        File[] listFiles = file.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            if (listFiles[i].getAbsolutePath().endsWith(".java")) {
                alist.add(getClassName(listFiles[i].getAbsolutePath()));
            } else if (listFiles[i].isDirectory()) {
                getFiles(listFiles[i].getAbsolutePath());
            }
        }

    }

    /**Returns the name of the class
     *
     * @param absolutePath
     * @return
     */
    private static String getClassName(String absolutePath) {
        absolutePath = absolutePath.substring(0,
                                              absolutePath.length() -
                                              ".java".length());
        int index = absolutePath.indexOf(sourceDir);
        if (index == -1) {
            absolutePath = absolutePath.replaceAll("\\\\", "/");
            index = absolutePath.indexOf(sourceDir);
        }

        index = index + sourceDir.length() + 1;
        absolutePath = absolutePath.substring(index);
        absolutePath = absolutePath.replaceAll("/", ".");
        //System.out.println(absolutePath);
        return absolutePath;
    }

    /**Keep only those classes that have the interface "keepInterface"
     *
     * @param keepInterface
     */
    private static void filterClasses(String keepInterface) {
        ArrayList<String> removeList = new ArrayList<String>(20);
        Iterator<String> it = alist.iterator();
        ClassLoader cl = InstallAllComponents.class.getClassLoader();
        String name = null;
        Class testClass;
        while (it.hasNext()) {
            name = it.next();
            try {
                testClass = cl.loadClass(name);
                Class testInterface = null;
                boolean doesImplements = false;
                for (int i = 0; i < testClass.getInterfaces().length; i++) {
                    testInterface = testClass.getInterfaces()[i];
                    if (testInterface.getName().equals(
                            EXECUTABLE_COMPONTONENT_INTERFACE)) {
                        doesImplements = true;
                        break;
                    }
                }
                if (!doesImplements) {
                    removeList.add(name);
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

        if (removeList.size() > 0) {
            alist.removeAll(removeList);
        }

    }

    // get the list of packages
    private static void getPackageList() {
        Iterator<String> it = alist.iterator();
        String className = null;
        String packageName = null;
        int startClass = -1;
        while (it.hasNext()) {
            className = it.next();
            startClass = className.lastIndexOf(".");

            packageName = className.substring(0, startClass);
            if (!packageList.contains(packageName)) {
                packageList.add(packageName);
            }

        }
    }

}
