/*
 * @(#) ClasspathContainerUtils.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.meandre.ide.eclipse.classpathcontainer.ClasspathContainerMetadata;
import org.meandre.ide.eclipse.component.logger.MeandreLogger;

/**Reads the server/lib path from the plugin install directory
 * and exposes the classpath containers made available.
 * 
 * @author Amit Kumar
 * Created on Jun 4, 2008 4:08:03 PM
 *
 */
public class ClasspathContainerUtils {
	boolean isValid;
	String libDirectory;
	ArrayList<ClasspathContainerMetadata> classpathContainers = new ArrayList<ClasspathContainerMetadata>(10);
	
	public ClasspathContainerUtils(String libDirectory){
		this.libDirectory = libDirectory;
	}
	
	/**this function populates the classpathContainers variable.
	 * 
	 * @return
	 */
	public boolean init(){
		File file = new File(libDirectory);
		if(!file.exists()){
			MeandreLogger.logError("The classpath container dirctory: " + libDirectory + "  does not exist.");
			isValid = Boolean.FALSE;
			return Boolean.FALSE;
		}
		String[] fileList=file.list();
		
		for(int i=0; i < fileList.length; i++){
			if(new File(file, fileList[i]).isDirectory()){
				updateContainerList(file, fileList[i]);
			}
		}
		
		if(classpathContainers.size()==0){
			isValid = Boolean.FALSE;
			return Boolean.FALSE;
		}
		isValid = Boolean.TRUE;
		return Boolean.TRUE;
	}

	
	/**Reads the jar file containers
	 * 
	 * @param file
	 * @param filename
	 */
	private void updateContainerList(File file, String filename) {
		File libDir = new File(file, filename);
		File classpathPropertyFile = new File(libDir,"classcontainer.properties");
		Properties properties = new Properties();
		try {
	        properties.load(new FileInputStream(classpathPropertyFile));
	    } catch (IOException e) {
	    	
	    }
	   
	    if(properties.size()!=0){
	    ClasspathContainerMetadata ccm = new ClasspathContainerMetadata();	
	    ccm.setDescription(properties.get("description").toString());
	    ccm.setName(properties.get("name").toString());
	    ccm.setReleaseDate(properties.getProperty("release-date").toString());
	    
	    ArrayList<String> supportedVersions = getSupportedVersions(properties.get("compatiable").toString());
	    ArrayList<String> unsupportedVersions = getUnSupportedVersions(properties.get("compatiable").toString());
	  
	    Iterator<String> it=null;
	    if(supportedVersions!=null){
	    it = supportedVersions.iterator();
	    while(it.hasNext())
	    ccm.addCompatiableVersion(it.next());
	    }
	    
	    if(unsupportedVersions!=null){
	    it = unsupportedVersions.iterator();
	    while(it.hasNext())
	    ccm.addUnCompatiableVersion(it.next());
	    }
	    
	    
	    File jarDir = new File(libDir,"jar");
	    if(jarDir.exists()){
	    String[] list = jarDir.list();
	    
	    for(int i =0 ; i < list.length; i++){
	    	File jarfile = new File(jarDir,list[i]);
	    	if(jarfile.exists() && jarfile.isFile()){
	    		ccm.addJar(jarfile.getAbsolutePath());
	    	}
	    	
	    }
	    }else{
	    	MeandreLogger.logError("[Plugin Error] Error: the classcontainer " + jarDir.getAbsolutePath() + " does not exist.");
	    }
	    
	    if(ccm.getJarList().size()>0){
	    String version = getVersionInfo(ccm.getName());	
	    if(version.equalsIgnoreCase("1.3")){
	    	ccm.setDefaultContainer(Boolean.TRUE);
	    }
	   
	    this.classpathContainers.add(ccm);
	    }else{
	    MeandreLogger.logError("[Plugin Error] No Jar files were found in the " + jarDir.getAbsolutePath());	
	    }
	    }else{
	    MeandreLogger.logWarning("[Plugin warning] The classpath property file " + classpathPropertyFile.getAbsolutePath() + " is invalid");	
	    }
		
	}

	private String getVersionInfo(String name) {
		StringTokenizer stok = new StringTokenizer(name);
		if(stok.countTokens()<1){
			MeandreLogger.logError("Error -container name invalid: "+ name);
			return "undefined";
		}
		String version = stok.nextToken();
		return version;
	}

	private ArrayList<String> getSupportedVersions(String list) {
		if(list==null)
		return null;
		
		StringTokenizer stok = new StringTokenizer(list,",");
		ArrayList<String> supportedVersions = new ArrayList<String>(2);
		String val=null;
		while(stok.hasMoreTokens()){
		val = stok.nextToken();
		if(val!=null){
			val = val.trim();
			if(val.charAt(0)!= '!'){
				supportedVersions.add(val);
			}
		}
		}
		return supportedVersions;
	}

	

	private ArrayList<String> getUnSupportedVersions(String list) {
		if(list==null)
		return null;
		StringTokenizer stok = new StringTokenizer(list,",");
		ArrayList<String> unSupportedVersions = new ArrayList<String>(2);
		String val=null;
		while(stok.hasMoreTokens()){
		val = stok.nextToken();
		if(val!=null){
			val = val.trim();
			if(val.charAt(0)== '!'){
				unSupportedVersions.add(val);
			}
		}
		}
		return unSupportedVersions;
	}

	/**
	 * @return the isValid
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * @param isValid the isValid to set
	 */
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	/**
	 * @return the classpathContainers
	 */
	public ArrayList<ClasspathContainerMetadata> getClasspathContainers() {
		return classpathContainers;
	}

	/**
	 * @return the libDirectory
	 */
	public String getLibDirectory() {
		return libDirectory;
	}

	/**
	 * @param libDirectory the libDirectory to set
	 */
	public void setLibDirectory(String libDirectory) {
		this.libDirectory = libDirectory;
	}

	/**
	 * @param classpathContainers the classpathContainers to set
	 */
	public void setClasspathContainers(
			ArrayList<ClasspathContainerMetadata> classpathContainers) {
		this.classpathContainers = classpathContainers;
	}

	public ClasspathContainerMetadata getDetafaultContainer() {
		Iterator<ClasspathContainerMetadata> it = this.classpathContainers.iterator();
		ClasspathContainerMetadata ccm =null;
		ClasspathContainerMetadata thisccm = null;
		
		while(it.hasNext()){
			thisccm = it.next();
			if(thisccm.isDefaultContainer()){
				ccm = thisccm;
			}
		}
		
		if(ccm==null){
			if(this.classpathContainers.size()>0){
				this.classpathContainers.get(this.classpathContainers.size()-1);
			}
		}
		return ccm;
	}

	public boolean hasClasspathContainer(String name) {
		Iterator<ClasspathContainerMetadata> it = this.classpathContainers.iterator();
		ClasspathContainerMetadata thisccm = null;
		while(it.hasNext()){
			thisccm = it.next();
			if(thisccm.getName().equals(name)){
				return true;
			}
		}
		return false;
	}

	public ClasspathContainerMetadata getClasspathContainerForVersion(
			String _version) {
		
		Iterator<ClasspathContainerMetadata> it = this.classpathContainers.iterator();
		ClasspathContainerMetadata thisccm = null;
		ClasspathContainerMetadata ccm = null;
		while(it.hasNext()){
			thisccm = it.next();
			String version = getVersion(thisccm.getName());
			if(version.equalsIgnoreCase(_version)){
				ccm = thisccm;
			}
		}
		
		return ccm;
	}
	
	public ClasspathContainerMetadata getClasspathContainerForName(
			String _name) {
		
		Iterator<ClasspathContainerMetadata> it = this.classpathContainers.iterator();
		ClasspathContainerMetadata thisccm = null;
		ClasspathContainerMetadata ccm = null;
		while(it.hasNext()){
			thisccm = it.next();
			String name = thisccm.getName();
			if(name.equalsIgnoreCase(_name)){
				ccm = thisccm;
			}
		}
		
		return ccm;
	}
	
	
	


	public String getVersion(String name) {
		StringTokenizer stok = new StringTokenizer(name);
		if(stok.countTokens()>0){
			return stok.nextToken();
		}
		return name;
	}

	public String[] getSupportedClasspathContainerNames() {
		String[] classpathContainerNameList = new String[this.classpathContainers.size()];
	        Iterator<ClasspathContainerMetadata> it = this.classpathContainers.iterator();
	        int i=0;
	        ClasspathContainerMetadata thisccm = null;
	        while(it.hasNext()){
	        	thisccm = it.next();
	        	classpathContainerNameList[i] = new String();
	        	classpathContainerNameList[i]= thisccm.getName();
	        	i++;
	        }
		return classpathContainerNameList;
	}
	
	



}
