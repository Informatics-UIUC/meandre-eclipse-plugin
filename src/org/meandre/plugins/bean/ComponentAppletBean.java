/*
 * @(#) ComponentAppletBean.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.plugins.bean;

import java.util.ArrayList;

/**This class is used by the ComponentNatureHandler to offload the
 * applet code handling from the FindComponentDependencyRunnable
 * and InstallComponentRunnable
 * 
 * @author Amit Kumar
 * Created on Jun 10, 2008 7:10:56 PM
 *
 */
public class ComponentAppletBean {
	String mainClass;
	String appletJarName;
	boolean createdJar = Boolean.FALSE;
	String appletClassPath;
	ArrayList<String> resources = new ArrayList<String>(3);
	ArrayList<String> dependency = new ArrayList<String>(3);
	/**
	 * @return the mainClass
	 */
	public String getMainClass() {
		return mainClass;
	}
	/**
	 * @param mainClass the mainClass to set
	 */
	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}
	/**
	 * @return the appletJarName
	 */
	public String getAppletJarName() {
		return appletJarName;
	}
	/**
	 * @param appletJarName the appletJarName to set
	 */
	public void setAppletJarName(String appletJarName) {
		this.appletJarName = appletJarName;
	}
	
	
	public void addDependency(String filepath){
		this.dependency.add(filepath);
	}
	
	
	public void addResources(String filepath){
		this.resources.add(filepath);
	}
	/**
	 * @return the resources
	 */
	public ArrayList<String> getResources() {
		return resources;
	}
	/**
	 * @return the dependency
	 */
	public ArrayList<String> getDependency() {
		return dependency;
	}
	
	public void createdJarFile(boolean createdJar) {
		this.createdJar = createdJar;
		
	}

	public boolean hasCreatedJarFile(){
		return this.createdJar;
	}
	public void setAppletClassPath(String appletClassPath) {
		this.appletClassPath = appletClassPath;
	}
	
	public String getAppletClassPath(){
		return this.appletClassPath;
	}
	
	
	/**check of a file is already included
	 * 
	 * @param filename
	 * @return
	 */
	public boolean hasResource(String filename) {
		return this.resources.contains(filename);
	}

}
