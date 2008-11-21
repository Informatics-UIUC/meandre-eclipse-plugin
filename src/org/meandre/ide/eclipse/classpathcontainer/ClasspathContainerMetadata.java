/**
 * @(#) ClasspathContainerMetadata.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.classpathcontainer;

import java.util.ArrayList;


/**This object is populated with the information about supported
 * MeandreInfrastructure classpath containers
 * 
 * @author Amit Kumar
 * Created on Jun 4, 2008 3:44:06 PM
 *
 */
public class ClasspathContainerMetadata {
	String name;
	String description;
	String releaseDate;
	ArrayList<String> compatiableVersions = new ArrayList<String>(2);
	ArrayList<String> unCompatiableVersions = new ArrayList<String>(2);
	// absolute paths in the jarList
	ArrayList<String> jarList = new ArrayList<String>(5);
	Boolean defaultContainer = Boolean.FALSE;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the releaseDate
	 */
	public String getReleaseDate() {
		return releaseDate;
	}
	/**
	 * @param releaseDate the releaseDate to set
	 */
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}
	/**
	 * @return the compatiableVersions
	 */
	public ArrayList<String> getCompatiableVersions() {
		return compatiableVersions;
	}
	
	/**
	 * @return the unCompatiableVersions
	 */
	public ArrayList<String> getUnCompatiableVersions() {
		return unCompatiableVersions;
	}
	
	public void addCompatiableVersion(String version){
		this.compatiableVersions.add(version);
	}
	
	public void addUnCompatiableVersion(String version){
		this.unCompatiableVersions.add(version);
	}
	/**
	 * @return the jarList
	 */
	public ArrayList<String> getJarList() {
		return jarList;
	}
	/**
	 * @param jarList the jarList to set
	 */
	public void setJarList(ArrayList<String> jarList) {
		this.jarList = jarList;
	}
	
	
	/**Add jar file to the list
	 * 
	 * @param jarFile
	 */
	public void addJar(String jarFile){
		this.jarList.add(jarFile);
	}
	public void setDefaultContainer(Boolean defaultContainer) {
		this.defaultContainer = defaultContainer;
		
	}
	
	
	public boolean isDefaultContainer(){
		return this.defaultContainer;
	}
	
	

}
