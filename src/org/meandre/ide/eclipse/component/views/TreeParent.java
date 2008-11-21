/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class TreeParent  extends TreeObject implements PropertyChangeListener{
	private ArrayList children;
	private String name;
	
	
	private String componentClass;
	private String componentUri;
	private String componentRunnable;
	private String componentFormat;
	boolean server = Boolean.FALSE;
	boolean isWebUI = Boolean.FALSE;
	boolean localServer = Boolean.FALSE;

	
	
	

	public TreeParent(String name) {
		super(name);
		this.name = name;
		children = new ArrayList();
	}

	public void addChild(TreeObject child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(TreeObject child) {
		children.remove(child);
		child.setParent(null);
	}

	public TreeObject[] getChildren() {
		return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		 String propertyName = evt.getPropertyName();
	        if ("available".equals(propertyName)) {
	            
	        } else if ("add".equals(propertyName)) {
	           
	        }
	    }
	
	public String toString(){
		return name;
	}

	public String getComponentClass() {
		return componentClass;
	}
		
	
	public void setComponentClass(String className) {
		this.componentClass= className;
	}

	/**
	 * @return the server
	 */
	public boolean isServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(boolean server) {
		this.server = server;
	}

	/**
	 * @return the localServer
	 */
	public boolean isLocalServer() {
		return localServer;
	}

	/**
	 * @param localServer the localServer to set
	 */
	public void setLocalServer(boolean localServer) {
		this.localServer = localServer;
	}

	/**Return the component identifier
	 * 
	 * @return
	 */
	public String getComponentUri() {
		return componentUri;
	}

	/**
	 * @param componentUri the componentUri to set
	 */
	public void setComponentUri(String componentUri) {
		this.componentUri = componentUri;
	}

	/**
	 * @return the componentRunnable
	 */
	public String getComponentRunnable() {
		return componentRunnable;
	}

	/**
	 * @param componentRunnable the componentRunnable to set
	 */
	public void setComponentRunnable(String componentRunnable) {
		this.componentRunnable = componentRunnable;
	}

	/**
	 * @return the componentFormat
	 */
	public String getComponentFormat() {
		return componentFormat;
	}

	/**
	 * @param componentFormat the componentFormat to set
	 */
	public void setComponentFormat(String componentFormat) {
		this.componentFormat = componentFormat;
	}

	/**sets the property if the component is a web component
	 * 
	 * @param webui
	 */
	public void setWebUI(boolean webui) {
		isWebUI=webui;
	}

	



}
