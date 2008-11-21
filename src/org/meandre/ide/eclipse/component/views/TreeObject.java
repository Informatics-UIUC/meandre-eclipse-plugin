/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */

package org.meandre.ide.eclipse.component.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;

public class TreeObject implements IAdaptable {
	
	private ArrayList children;
	
	private String componentName;
	
	private String name;
	
	private boolean hasSource = Boolean.FALSE;
	private boolean isComponent = Boolean.FALSE;
	
	private String checksum;
	private boolean isProperty = Boolean.FALSE;
	private boolean isInput = Boolean.FALSE;
	private boolean isOutput = Boolean.FALSE;
	private String dataType = null;
	private String description=null;
	
	
	private TreeParent parent;

	public TreeObject(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setParent(TreeParent parent) {
		this.parent = parent;
	}

	public TreeParent getParent() {
		return parent;
	}

	public String toString() {
		int i = this.getComponentName().lastIndexOf("/");
		if(i!=-1){
			return this.getComponentName().substring(i+1);
		}
		return this.getComponentName(); /*+ " hasSource: " + isHasSource() 
		+" hasComponentClass: " + isComponent() + " checksum: "+
		getChecksum();*/
	}

	public Object getAdapter(Class key) {
		return null;
	}

	/**
	 * @return the componentName
	 */
	public String getComponentName() {
		return componentName;
	}

	/**
	 * @param componentName the componentName to set
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
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
	 * @return the checksum
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * @param checksum the checksum to set
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

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
	 * @return the isProperty
	 */
	public boolean isProperty() {
		return isProperty;
	}

	/**
	 * @param isProperty the isProperty to set
	 */
	public void setProperty(boolean isProperty) {
		this.isProperty = isProperty;
	}

	/**
	 * @return the isInput
	 */
	public boolean isInput() {
		return isInput;
	}

	/**
	 * @param isInput the isInput to set
	 */
	public void setInput(boolean isInput) {
		this.isInput = isInput;
	}

	/**
	 * @return the isOutput
	 */
	public boolean isOutput() {
		return isOutput;
	}

	/**
	 * @param isOutput the isOutput to set
	 */
	public void setOutput(boolean isOutput) {
		this.isOutput = isOutput;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription(){
		return description;
	}
}