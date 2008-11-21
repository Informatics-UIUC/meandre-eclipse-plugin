/**
 * @(#) CreateComponentRDFDescriptor.java @VERSION@
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.tools.components;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;

import org.meandre.annotations.Component;
import org.meandre.core.repository.CorruptedDescriptionException;



/**This class creates component descriptor for all the source code
 *
 * @author Amit Kumar
 * Created on Dec 6, 2007 5:05:01 AM
 *
 */
public class CreateAllComponentDescriptor {

    static ArrayList<String> alist = new ArrayList<String>(50);
    static String sourceDir;


    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println(
                    "Usage: java CreateAllComponentDescriptor source_directory descriptor_directory");
            System.exit(0);
        }

        sourceDir = args[0];
        String componentDescriptorFolder = args[1];
        if (!(new File(componentDescriptorFolder)).exists()) {
            System.out.println("Cannot continue... " +
                               (new File(componentDescriptorFolder)).
                               getAbsolutePath() + " does not exist.");
            System.exit(0);
        }

        if (!(new File(sourceDir)).exists() ||
            !(new File(sourceDir)).isDirectory()) {
            System.out.println("Cannot continue expect source directory.");
            System.exit(0);
        }

        getFiles(sourceDir);
        System.out.println("Number of Java Files: " + alist.size());

        Iterator<String> it = alist.iterator();
        Class testClass = null;
        Annotation anon = null;
        ClassLoader cl = CreateAllComponentDescriptor.class.getClassLoader();
        String name = null;
        while (it.hasNext()) {
            name = it.next();
            try {
                System.out.println(name);
                testClass = cl.loadClass(name);
                anon = testClass.getAnnotation(Component.class);
                if (anon != null) {
                    System.out.println("Found: " + name);
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        CreateComponentDescriptor ccd = new CreateComponentDescriptor(
                componentDescriptorFolder);
        Iterator<String> itClass = alist.iterator();
        while (itClass.hasNext()) {
            String className = itClass.next();
            //ystem.out.println("Processing: " + className);
            try {
                ccd.init(className);
                ccd.process();
            } catch (ClassNotFoundException e) {
                System.out.println("Could not process: " + className +
                                   " class could not be found.");
            } catch (CorruptedDescriptionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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


}
