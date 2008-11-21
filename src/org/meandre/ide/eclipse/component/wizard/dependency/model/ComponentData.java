/*
 * @(#) ComponentData.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.wizard.dependency.model;

import org.eclipse.jdt.core.ICompilationUnit;

/**This object is created in the wizard and store in the model.
 * 
 * @author Amit Kumar
 * Created on Jul 12, 2008 1:22:19 PM
 *
 */
public class ComponentData {
	private String className;
	private boolean selected;
	private boolean installed;
	private String installMessage;
	private String type;
	private String name;
	private ICompilationUnit compilationUnit;
	/**
	 * @return the path
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @param path the path to set
	 */
	public void setClassName(String path) {
		this.className = path;
	}
	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}
	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	public void setName(String name) {
		this.name = name;
		
	}
	
	public String getName(){
		return this.name;
	}
	
	/**
	 * @param compilationUnit the compilationUnit to set
	 */
	public void setCompilationUnit(ICompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}


	
	public ICompilationUnit getCompilationUnit() {
		return compilationUnit;
	}
	/**
	 * @return the installed
	 */
	public boolean isInstalled() {
		return installed;
	}
	/**
	 * @param installed the installed to set
	 */
	public void setInstalled(boolean installed) {
		this.installed = installed;
	}
	/**
	 * @return the installMessage
	 */
	public String getInstallMessage() {
		return installMessage;
	}
	/**
	 * @param installMessage the installMessage to set
	 */
	public void setInstallMessage(String installMessage) {
		this.installMessage = installMessage;
	}

}
