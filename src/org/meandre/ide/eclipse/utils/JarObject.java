/*
 * @(#) JarObject.java @VERSION@
 * 
/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.utils;

import java.util.ArrayList;
import java.util.HashMap;

/**A bean that represents the jar object
 * 
 * @author Amit Kumar
 * Created on Apr 15, 2008 11:20:38 PM
 *
 */
public class JarObject {
	Long lastModified;
	Long size;
	String md5;
	String name;
	// properties and datatypes are null by default.
	ArrayList<String> properties;
	ArrayList<String> interfaceList = new ArrayList<String>(4);
	HashMap<String,String> inputDataType;
	HashMap<String,String> outputDataType;
	
	
	
	boolean isComponent;
	boolean hasSource;
	/**
	 * @return the isComponent
	 */
	public boolean isComponent() {
		return isComponent;
	}
	/**
	 * @param isComponent the isComponent to set
	 */
	public void setComponent(boolean isComponent) {
		this.isComponent = isComponent;
	}
	/**
	 * @return the hasSource
	 */
	public boolean isHasSource() {
		return hasSource;
	}
	/**
	 * @param hasSource the hasSource to set
	 */
	public void setHasSource(boolean hasSource) {
		this.hasSource = hasSource;
	}
	/**
	 * @return the size
	 */
	public Long getSize() {
		return size;
	}
	/**
	 * @param size the size to set
	 */
	public void setSize(Long size) {
		this.size = size;
	}
	/**
	 * @return the lastModified
	 */
	public Long getLastModified() {
		return lastModified;
	}
	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}
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
	 * @return the md5
	 */
	public String getMd5() {
		return md5;
	}
	/**
	 * @param md5 the md5 to set
	 */
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	/**
	 * @return the inputDataType
	 */
	public HashMap<String, String> getInputDataType() {
		return inputDataType;
	}
	/**
	 * @return the outputDataType
	 */
	public HashMap<String, String> getOutputDataType() {
		return outputDataType;
	}
	
	
	public void addInputDataType(String input, String dataType){
		if(this.inputDataType==null){
			this.inputDataType= new HashMap<String,String>(4);
		}
		
		inputDataType.put(input, dataType);
	}

	public void addOutputDataType(String output, String dataType){
		if(this.outputDataType==null){
			this.outputDataType= new HashMap<String,String>(4);
		}
		outputDataType.put(output, dataType);
	}
	/**
	 * @return the properties
	 */
	public ArrayList<String> getProperties() {
		return properties;
	}
	
	
	public void addProperty(String property){
		if(this.properties==null){
			this.properties = new ArrayList<String>(3);
		}
		this.properties.add(property);
	}
	
	public void addInterface(String interfaceName) {
		interfaceList.add(interfaceName);
	}
	public boolean hasInterface(String interfaceName) {
		return this.interfaceList.contains(interfaceName);
	}
	

	
}
