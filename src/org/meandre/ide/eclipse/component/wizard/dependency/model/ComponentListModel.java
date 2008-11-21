/*
 * @(#) ComponentListModel.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.wizard.dependency.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class ComponentListModel {
	private HashMap<String,ComponentData> componentList = new HashMap<String,ComponentData>(10);
	
	public void addData(String path, ComponentData cdata){
		this.componentList.put(path, cdata);
	}
	
	public ComponentData getData(String path){
		return this.componentList.get(path);
	}
	
	public Set<String> getkeySet(){
		return this.componentList.keySet();
	}

	public int getNumSelected() {
		int numSelected=0;
		Iterator<String>  its = getkeySet().iterator();
		while(its.hasNext()){
			ComponentData cdata = getData(its.next());
			if(cdata.isSelected()){
				numSelected++;
			}
		}
		return numSelected;
	}
	
	

	public void cleanup() {
		 componentList = new HashMap<String,ComponentData>(10);
	}
	
	
	public String toString(){
		Iterator<String> it=  this.getkeySet().iterator();
		StringBuffer sbuffer = new StringBuffer();
		sbuffer.append("The number of component found: " + this.componentList.size()+"\n");
		sbuffer.append("The number of component selected to be installed: " + this.getNumSelected()+"\n");
		sbuffer.append("The number of component installed: "+ this.getNumInstalled()+"\n");
		if(this.getNumInstalled()>0){
		sbuffer.append("The installed component are:\n");
		}
		while(it.hasNext()){
		ComponentData cdata = this.getData(it.next());
		if(cdata.isInstalled()){
			sbuffer.append(cdata.getName()+"\n");
		}
		}
		it=  this.getkeySet().iterator();
		if(this.getNumSelected()- this.getNumInstalled()!=0){
		sbuffer.append("The components that could not be installed are: ");
		ComponentData cdata = this.getData(it.next());
		if(!cdata.isInstalled()){
			sbuffer.append(cdata.getName()+"\n");
		}	
		}
		
		return sbuffer.toString();
	}

	private int getNumInstalled() {
		int numInstalled=0;
		Iterator<String>  its = getkeySet().iterator();
		while(its.hasNext()){
			ComponentData cdata = getData(its.next());
			if(cdata.isInstalled()){
				numInstalled++;
			}
		}
		return numInstalled;
	}

}
