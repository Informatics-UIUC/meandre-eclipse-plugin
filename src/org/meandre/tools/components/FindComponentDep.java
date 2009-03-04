/*
 * @(#) FindComponentDep.java @VERSION@
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
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

import com.tonicsystems.jarjar.DepFind;
import com.tonicsystems.jarjar.DepHandler;

/**This class reads the component byte code and determines the component package
 * dependency. It gathers the list of class references and then attempts to find
 * those classes in the list of jar files.
 *
 * @author Amit Kumar Created on Dec 15, 2007 5:12:43 PM
 *
 */
//somewhere between DCA and Dallas
public class FindComponentDep {

	private ArrayList<String> jarList;
	private  ArrayList<String> processedList;
	private ArrayList<String> dependencyList;

	private ArrayList<String> dependencyNotFoundList;

	private MeandreComponentDepHandler handler,handler2;

	private String appClasspath;

	private int levelFlag;

    private String curDir = new File(System.getProperty("user.dir")).getPath();


    /**populate the jarList based on the the library folder
     *
     * @param libFolder
     */
	public FindComponentDep(String libFolder){
		jarList = new ArrayList<String>(30);
		processedList = new ArrayList<String>(30);
		dependencyList = new ArrayList<String>(30);
		makeApplicationClasspath(new File(libFolder));
		appClasspath= getApplicationClasspath();
		dependencyNotFoundList = new ArrayList<String>(4);
	}


	/**The jar list is filled externally
	 *
	 * @param lib
	 */
	public FindComponentDep(ArrayList<String> lib){
		jarList = new ArrayList<String>(30);
		processedList = new ArrayList<String>(30);
		dependencyList = new ArrayList<String>(30);
		jarList = lib;
		appClasspath= getApplicationClasspath();
		dependencyNotFoundList = new ArrayList<String>(4);
	}





	public void reset(){
		jarList.clear();
		processedList.clear();
		dependencyList.clear();
		dependencyNotFoundList.clear();
	}

	public static void main(String args[]){
		if (args.length != 2) {
			System.out.println("Usage: FindComponentDep componentPath libFolderWithJarFiles");
			System.exit(0);
		}
		System.out.println(System.getProperty("java.class.path").split(":").length);
		System.out.println("Class File: " + args[0]);

		String className = args[0];
		String lib = args[1];
		String packagePath = className;
		if(className.endsWith(".class")){
			//int i= className.lastIndexOf(".");
			String classString=className; //=className.substring(0, i-1);

			//i = classString.lastIndexOf(System.getProperty("file.separator"));
			packagePath =classString;//= classString.substring(0,i);


		}

		FindComponentDep fcd = new FindComponentDep(lib);
		fcd.makeApplicationClasspath(new File(lib));
		System.out.println("--->"+fcd.getApplicationClasspath());
		fcd.execute(packagePath, new String[]{"mysql-connector-java-5.0.5.jar"});


		System.out.println("number of dependencies: " + fcd.dependencyList.size());
		System.out.println("Class File: " + packagePath);
		for (int i = 0; i < fcd.dependencyList.size(); i++) {
			System.out.println(fcd.dependencyList.get(i));
		}

	}




	/**
	 * @deprecated use execute(String componentPath, class componentClass)
	 * @param componentPath
	 */
	public void execute(String componentPath) {
	  execute(componentPath, null) ;
	}


    /**Finds the dependency list for a package and populates
	 * the dependencyList array
	 *
	 * @param componentPath
	 */
	public void execute(String componentPath, String[]jjlist) {
		String cp1 = componentPath;
	    levelFlag = DepHandler.LEVEL_JAR;
		handler = new MeandreComponentDepHandler(levelFlag, cp1);
		ArrayList<String> declaredComponentDependency=null;


		if(jjlist!=null){
			if(jjlist.length!=0){
				declaredComponentDependency= getDeclaredComponentDependency(jjlist);
			}

		}



		try {
			new DepFind().run(cp1,appClasspath, handler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		processedList.add(cp1);
		System.out.println("Number of jar dependencies: " + handler.alist.toString());
		for (int i = 0; i < handler.getJarList().size(); i++) {
			dependencyList.add(handler.getJarList().get(i));
		}

		if(declaredComponentDependency != null){
			for(int i=0; i < declaredComponentDependency.size();i++){
			if(!dependencyList.contains(declaredComponentDependency.get(i))){
				System.out.println("Adding declared: " + declaredComponentDependency.get(i)  + " to the dependency List");
				dependencyList.add(declaredComponentDependency.get(i));
			}
			}
		}

		Iterator<String> it = handler.getJarList().iterator();
		String jarFile = null;

		while (it.hasNext()) {

			handler2 = new MeandreComponentDepHandler(levelFlag, cp1);
			jarFile = it.next();
			//System.out.println(jarFile);
			if (!processedList.contains(jarFile)) {
				processedList.add(jarFile);
				try {

                                    int index = jarFile.indexOf(curDir);
                                    if (index != -1){
                                        index = index + curDir.length() + 1;
                                        jarFile = jarFile.substring(index);
                                    }

                   System.out.println("++++ " + jarFile +  "--appClasspath: " + appClasspath);

					new DepFind().run(jarFile, appClasspath,handler2);
				} catch (Exception e) {
					System.out.println("Error: could not parse the jar file: " + jarFile) ;
				}
				for (int i = 0; i < handler2.getJarList().size(); i++) {
					if(!dependencyList.contains(handler2.getJarList().get(i))){
						dependencyList.add(handler2.getJarList().get(i));
					}
				}

			}
		}



	}


	private ArrayList<String> getDeclaredComponentDependency(
			String[] dependencyList) {
		if(dependencyList==null){
			return null;
		}


		// lazy instantiate
		ArrayList<String> declaredDepList = new ArrayList<String>(2);
		for(int i=0; i < dependencyList.length;i++){
			if(dependencyList[i].length()>0){
				String depPath=findDependencyInAppPath(dependencyList[i]);
				if(depPath!=null){
					declaredDepList.add(depPath);
				}else{
					dependencyNotFoundList.add(dependencyList[i]);
				}
			}
		}
		return declaredDepList;
	}



	private String findDependencyInAppPath(String dependency) {
		if(dependency==null){
			return null;
		}

		Iterator<String> it = this.jarList.iterator();
		dependency = dependency.toLowerCase();
		String thisDep=null;
		while(it.hasNext()){
		thisDep = it.next().trim();
		if(thisDep.toLowerCase().endsWith(dependency)){
			return thisDep;
		}
		}
		return null;
	}


	/**
	 *
	 * @return
	 */
	private String getApplicationClasspath() {
		String classPath = "";
		Iterator<String> it = jarList.iterator();


		int count = 0;
		while (it.hasNext()) {

                    String nxt = it.next();

                    int index = nxt.indexOf(curDir);
                    if (index != -1){
                        index = index + curDir.length() + 1;
                        nxt = nxt.substring(index);
                    }

			if (count == 0) {
				classPath = nxt;
			} else {
				classPath = classPath + File.pathSeparator + nxt;
			}
			count++;
		}

		return classPath.replaceAll("\\\\", "/");
	}

	/**
	 * Recursively retrieve all the jar files in a directory
	 *
	 * @param string
	 * @return
	 */
	private  void makeApplicationClasspath(File jarDirectory) {
		if (!jarDirectory.canRead()) {
			return;
		}
		File[] files = jarDirectory.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!(files[i].isDirectory())) {
				if (files[i].getName().endsWith(".jar")) {
					if ((files[i].getAbsolutePath().contains("driver-1.0.5")) ||
							(files[i].getAbsolutePath().contains("meandre")) ||
							(files[i].getAbsolutePath().contains("servlet-api"))||
							(files[i].getAbsolutePath().contains("jarjar"))||
							(files[i].getAbsolutePath().contains("jetty"))||
							(files[i].getAbsolutePath().contains("jsp-api"))||
							(files[i].getAbsolutePath().contains("icu4j"))
					) {
					}else{
						jarList.add(files[i].getAbsolutePath());
					}
				}
			} else {
				makeApplicationClasspath(files[i]);
			}
		}
		return;
	}



	/**Return dependency list as an array
	 *
	 * @return
	 */
	public ArrayList<String> getDependencyList() {
		return this.dependencyList;
	}

	public ArrayList<String> getDependencyNotFoundList(){
		return this.dependencyNotFoundList;
	}




}
