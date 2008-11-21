/*
 * @(#) FindAllComponentDep.java @VERSION@
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
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.meandre.annotations.Component;


/**This class creates the package-info.java source for each package
 * with information about the components and the component Dependencies.
 *
 * @author Amit Kumar
 * Created on Dec 23, 2007 4:41:45 PM
 *
 */
public class FindAllComponentDep {

    static String sourceDir;
    static String binDir;
    static ArrayList<String> alist = new ArrayList<String>(50);
    static ArrayList<String> packageList = new ArrayList<String>(10);

    private static String EXECUTABLE_COMPONENT_INTERFACE
            = "org.meandre.core.ExecutableComponent";


    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java FindAllComponentDep source_directory " +
            "bin_directory dependency_directory lib_directory");
            System.exit(0);
        }

        sourceDir = args[0];
        binDir = args[1];
        String dependencyFolder = args[2];
        String libFolder = args[3];

        if (!(new File(dependencyFolder)).exists()) {
            System.out.println("Cannot continue... " +
                               (new File(dependencyFolder)).getAbsolutePath() +
                               " does not exist.");
            System.exit(0);
        }

        if (!(new File(libFolder)).exists() ||
            !(new File(libFolder)).isDirectory()) {
            System.out.println("Cannot continue expected libFolder directory.");
            System.exit(0);
        }

        if (!(new File(sourceDir)).exists() ||
            !(new File(sourceDir)).isDirectory()) {
            System.out.println("Cannot continue expected source directory.");
            System.exit(0);
        }

        if (!(new File(binDir)).exists() || !(new File(binDir)).isDirectory()) {
            System.out.println("Cannot continue expected class directory.");
            System.exit(0);
        }

        getFiles(sourceDir);
        System.out.println("Number of Java Files: " + alist.size());
        filterClasses(EXECUTABLE_COMPONENT_INTERFACE);
        getPackageList();

        System.out.println("Number of Components: " + alist.size());

        FindComponentDep fcd = new FindComponentDep(libFolder);
        Iterator<String> itPackage = packageList.iterator();
        while (itPackage.hasNext()) {
            String packagePath = itPackage.next();
            String packageClasspath = getPackageClasspath(packagePath.
                    replaceAll("\\.", "/"));
            //sourceDir.replace("src", "bin")+File.separator+packagePath.replaceAll("\\.", "/");
            System.out.println("Processing: " + packagePath + "  -- " +
                               packageClasspath);
            fcd.execute(packageClasspath);
            ArrayList<String> alist = fcd.getDependencyList();
            ArrayList<Component>
            componentList = getComponentAnnotations(packagePath);
            createPackageInfoFile(packagePath, alist, componentList, dependencyFolder);
            componentList.clear();
            fcd.reset();
        }


    }

    private static String getPackageClasspath(String packagePath) {
        return binDir + "/" + packagePath;
    }

    private static ArrayList<Component> getComponentAnnotations(
            String packagePath) {
        ArrayList<Class> classList = getComponentList(packagePath);
        ArrayList<Component> componentList = new ArrayList<Component>(10);
        Iterator<Class> itclass = classList.iterator();
        Class thisclass = null;
        while (itclass.hasNext()) {
            thisclass = itclass.next();
            Annotation an = thisclass.getAnnotation(org.meandre.annotations.Component.class);
                    

            if (an != null) {
                componentList.add((Component) an);
            }

        }
        return componentList;
    }

    private static void createPackageInfoFile(String packagePath,
                                              ArrayList<String> alist2,
                                              ArrayList<Component> alist3,
                                              String dependencyFolder) {
        //System.out.println("Package Path: " + packagePath);
        //System.out.println("dependencyFolder: " + dependencyFolder);
        //System.out.println("ArrayList: " + alist2.size());
        String packageFolderPath = packagePath.replaceAll("\\.", "/");
        File file = new File(dependencyFolder, packageFolderPath);
        file.mkdirs();
        StringBuilder sbuilder = new StringBuilder();

        sbuilder.append(getComponentComments(packagePath, alist2,alist3));
        sbuilder.append("@PackageDependency(dependencyList={" +
                        getJarList(alist2,alist3) + "}," +
                        "\ncomponentList={" + getComponentListAnnotation(alist3) +
                        "})\n");
        sbuilder.append("package " + packagePath + ";\n");
        sbuilder.append(
                "import org.monkproject.meandre.annotations.PackageDependency;\n");
        sbuilder.append("import org.monkproject.meandre.annotations.Component;");
        //System.out.println(sbuilder.toString());
        try {
            BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(
                    file, "package-info.java")));
            bwriter.write(sbuilder.toString());
            bwriter.flush();
            bwriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static String getComponentListAnnotation(ArrayList<Component>
            alist3) {
        StringBuilder sbuilder = new StringBuilder();
        if (alist3.size() == 0) {
            return "";
        }
        Iterator<Component> itcomponent = alist3.iterator();
        Component thisComponent = null;
        int count = 0;
        while (itcomponent.hasNext()) {
            thisComponent = itcomponent.next();

            if (count == 0) {
                //System.out.println("here... " + thisComponent.toString());
                sbuilder.append(getComponentAnnotation(thisComponent));
            } else {
                sbuilder.append("," + getComponentAnnotation(thisComponent));
            }
            count++;
        }
        return sbuilder.toString();
    }


    private static String getComponentAnnotation(Component thisComponent) {
       String thisVal = "@Component(creator=\"" + filter(thisComponent.creator()) +
                         "\",";
        thisVal = thisVal + " description=" + "\"" +
                  filter(thisComponent.description()) + "\",";
        thisVal = thisVal + " tags=" + "\"" + filter(thisComponent.tags()) +
                  "\",";
        thisVal = thisVal + " name=" + "\"" + filter(thisComponent.name()) +
                  "\")\n";
        return thisVal;
    }


    private static String filter(String val) {
        String EOL = System.getProperty("line.separator");
        if (val == null) {
            return "";
        }
        if (val.length() == 0) {
            return "";
        }
        val = val.replace('\"', '\'');
        val = val.replaceAll("\\\\", "\\\\\\\\");
        val = val.replace(EOL, "");
        return val;
    }

    private static String getComponentComments(String packagePath,
                                               ArrayList<String> alist2,
                                               ArrayList<Component> alist3) {
        ArrayList<Class> classList = getComponentList(packagePath);
        ReadComponentDescriptor rcd = new ReadComponentDescriptor();
        Iterator<Class> itClass = classList.iterator();
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("/**\n");
        sbuilder.append("<b>Detected Dependencies: </b><br/> " +
                        getJarList(alist2,alist3) + "<br/>");
        sbuilder.append("<b>Number of Components in this package: " +
                        classList.size() + "</b><br/>\n");

        while (itClass.hasNext()) {
            rcd.init(itClass.next());
            rcd.process();
            sbuilder.append(rcd.toString());
            sbuilder.append("<br/><br/>\n");
        }

        sbuilder.append("**/\n");

        return sbuilder.toString();
    }

    private static ArrayList<Class> getComponentList(String packagePath) {
        ArrayList<Class> classList = new ArrayList<Class>(10);
        Iterator<String> it = alist.iterator();
        String className = null;
        String packageName = null;
        int i = -1;
        while (it.hasNext()) {
            className = it.next();
            i = className.lastIndexOf('.');
            if (i != -1) {
                packageName = className.substring(0, i);
                if (packageName.equals(packagePath)) {
                    System.out.println("Get class for: " + className);
                    try {
                        Class klazz = Class.forName(className);
                        if (klazz != null) {
                            classList.add(klazz);
                        }
                    } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return classList;
    }

    private static String getJarList(ArrayList<String> alist2,
    								ArrayList<Component> componentList) {
    	// get all the dependency that have been explicitly mentioned in the component
        Iterator <Component> itComponent= componentList.iterator();
        HashMap<String,String> componentDependencyList = new HashMap<String,String>(10);

        // get all the dependencies explicitly defined.
        while(itComponent.hasNext()){
        String[] dlist=itComponent.next().dependency();
        if(dlist==null|| dlist.length==0){
        	continue;
        }
        for(int i=0; i < dlist.length; i++){
        	// make sure it is not the default which is null
        	if(dlist[i].length()>0){
        	componentDependencyList.put(dlist[i],"dummy");
        	}
        }
        }




        // add all the dependencies detected
        Iterator<String> it = alist2.iterator();
        int count = 0;
        String jarFile = null;
        while (it.hasNext()) {
        	componentDependencyList.put(it.next(), "dummy");
        }


        String list = "";
      	Set<String> set=componentDependencyList.keySet();
      	ArrayList<String> alist3 = new ArrayList<String>(set);

        Collections.sort(alist3);
        Iterator<String> itKey = alist3.iterator();
        while (itKey.hasNext()) {
        jarFile = itKey.next();
        int i = jarFile.lastIndexOf(File.separator);
        jarFile = jarFile.substring(i + 1);
        if (count == 0) {
            list = "\"" + jarFile + "\"";
        } else {
            list = list + ",\"" + jarFile + "\"";
        }
        count++;
        }

        // return the jar list as a string
        return list;
    }

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

    /**Get java files in the source code
     *
     * @param srcFolder
     */
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
        if (index == -1){
            absolutePath = absolutePath.replaceAll("\\\\", "/");
            index = absolutePath.indexOf(sourceDir);
        }

        index = index + sourceDir.length() + 1;
        absolutePath = absolutePath.substring(index);
        absolutePath = absolutePath.replaceAll("/", ".");
        //System.out.println(absolutePath);
        return absolutePath;
    }


    private static void filterClasses(String keepInterface) {
        ArrayList<String> removeList = new ArrayList<String>(20);
        Iterator<String> it = alist.iterator();
        ClassLoader cl = FindAllComponentDep.class.getClassLoader();
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
                            EXECUTABLE_COMPONENT_INTERFACE)) {
                        doesImplements = true;
                        break;
                    }
                }
                if (!doesImplements) {
                    removeList.add(name);
                }

            } catch (ClassNotFoundException e) {
                System.out.println("Not found class: " + name);
                e.printStackTrace();
            }

        }

        if (removeList.size() > 0) {
            alist.removeAll(removeList);
        }

    }


}
