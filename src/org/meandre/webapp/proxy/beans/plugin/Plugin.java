/*
 * @(#) Plugin.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.webapp.proxy.beans.plugin;

/**This class stores the plugin information
 * 
 * @author Amit Kumar
 * Created on Jun 1, 2008 12:24:10 AM
 *
 */
public class Plugin {
	String key;
	String alias;
	boolean isServlet;
	String className;
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}
	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}
	/**
	 * @return the isServlet
	 */
	public boolean isServlet() {
		return isServlet;
	}
	/**
	 * @param isServlet the isServlet to set
	 */
	public void setServlet(boolean isServlet) {
		this.isServlet = isServlet;
	}
	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

}
