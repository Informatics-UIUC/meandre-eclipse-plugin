/*
 * @(#) TreeViewContentProvider.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.utils.JarObject;
import org.meandre.ide.eclipse.utils.TypeUtils;
import org.meandre.plugins.bean.Plugin;



import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;



public class TreeViewContentProvider  implements IStructuredContentProvider, 
ITreeContentProvider{

	private Session session;

	
	public TreeViewContentProvider(Session session){
		this.session = session;
	}
	
	
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		//System.out.println("inputChanged... "  + v.toString());
	}

	public void dispose() {
	}
	
	
	public Object[] getElements(Object parent) {
			initialize();
			return getChildren(session.getRoot());
	}
	
	
	public Object getParent(Object child) {
		if (child instanceof TreeObject) {
			return ((TreeObject)child).getParent();
		}
		return null;
	}
	
	
	public Object [] getChildren(Object parent) {
		if (parent instanceof TreeParent) {
			return ((TreeParent)parent).getChildren();
		}
		return new Object[0];
	}
	
	
	public boolean hasChildren(Object parent) {
		if (parent instanceof TreeParent)
			return ((TreeParent)parent).hasChildren();
		return false;
	}

	private void initialize() {
	
		if(Activator.getQueryableRepository() != null){
		System.out.println(Activator.getMeandreServer());
		TreeParent root = new TreeParent(Activator.getMeandreServer()+ " "+Activator.getMeandreProxy().getServerVersion() +" "+ " [Logged in as: " + Activator.getLogin() + "] "
				+  Activator.getQueryableRepository().getAvailableExecutableComponentDescriptions().size() 
				+ " components and "+ Activator.getMeandreProxy().getServerPlugins().size()+ " plugins " +
						" "+ getPluginList());
		root.setServer(Boolean.TRUE);
		int serverStart=Activator.getMeandreServer().indexOf("://");
		String serverName =null;
		if(serverStart ==-1){
		serverName = Activator.getMeandreServer();
		}else{	
		serverName = Activator.getMeandreServer().substring(serverStart+3);
		}
		
		if(Activator.getMeandreServer().startsWith("localhost") ||
			Activator.getMeandreServer().startsWith("127.0.0.1") ){
			root.setLocalServer(Boolean.TRUE);	
		}
		
		
		
		
		Set<ExecutableComponentDescription> compList= Activator.getQueryableRepository().getAvailableExecutableComponentDescriptions();
		Iterator<ExecutableComponentDescription> it = compList.iterator();
		ExecutableComponentDescription ecd=null;
		while(it.hasNext()){
		ecd = it.next();
		int i=ecd.getLocation().getURI().indexOf(ecd.getExecutableComponent().getURI());
		String componentClass ="";
		if(i!=-1){
		componentClass = ecd.getLocation().getURI().substring(i+(ecd.getExecutableComponent().getURI()+"/implementation/").length());
		}
		
		TreeParent p1 = new TreeParent(ecd.getName() +
				" " +ecd.getCreationDate() + " by " + 
				ecd.getCreator()+ 
				" ["+ /*+ "("+ ecd.getFormat()+") " +  ecd.getRunnable()+" "*/ componentClass+"]" );
		
		p1.setComponentUri(ecd.getExecutableComponent().getURI());
		p1.setComponentName(ecd.getExecutableComponent().getURI());
		p1.setComponentClass(componentClass);
		p1.setComponentFormat(ecd.getFormat());
		p1.setComponentRunnable(ecd.getRunnable());
		p1.setDescription(ecd.getDescription());
		
		for ( RDFNode rdfnodeContext:ecd.getContext() ){
			if ( rdfnodeContext.isResource() ){
				String resouceUri=((Resource)rdfnodeContext).getURI().trim();
				if(!resouceUri.endsWith("/")){
				JarObject jarObject=Activator.getComponentInfo(resouceUri);
				TreeObject tobject =new TreeObject("(Jar) " +resouceUri);
				tobject.setComponentName("(Jar) "+resouceUri);
			
				if(jarObject!=null){
					tobject.setChecksum(jarObject.getMd5());
					tobject.setComponent(jarObject.isComponent());
					tobject.setHasSource(jarObject.isHasSource());
					if(jarObject.hasInterface("org.meandre.webui.WebUIFragmentCallback")){
						p1.setWebUI(Boolean.TRUE);
					}
				}
				p1.addChild(tobject);
				
			if(jarObject!=null){
				if(jarObject.getOutputDataType()!=null){
					Iterator<String> its = jarObject.getOutputDataType().keySet().iterator();
					String key=null;
					while(its.hasNext()){
						key = its.next();
						String value =jarObject.getOutputDataType().get(key);
						if(value!=null){
						String dataType=TypeUtils.getDataType(value);
						
						TreeObject tob = new TreeObject("(Output) "+key + " " + dataType);
						System.out.println("(Output) " +key + " "+ dataType);
					
						tob.setComponentName("(Output) " +key + " " + dataType);
						tob.setOutput(Boolean.TRUE);
						tob.setDataType(value);
						p1.addChild(tob);
						}
					}
				}
				
				if(jarObject.getInputDataType()!=null){
					Iterator<String> its = jarObject.getInputDataType().keySet().iterator();
					String key=null;
					while(its.hasNext()){
						key = its.next();
							
						String value =jarObject.getInputDataType().get(key);
						
						if(value!=null){
						String dataType=TypeUtils.getDataType(value);
						TreeObject tob = new TreeObject("(Input) " +key+ " "+dataType);
						System.out.println("(Input) " +key+ " "+dataType);
						
						tob.setComponentName("(Input) " +key+ " "+dataType);
						tob.setInput(Boolean.TRUE);
						tob.setDataType(dataType);
						p1.addChild(tob);
						}
					}
				}
				
				if(jarObject.getProperties()!=null){
					Iterator<String> its = jarObject.getProperties().iterator();
					String key=null;
					while(its.hasNext()){
					key = its.next();
					TreeObject tob = new TreeObject(key);
					tob.setComponentName("(Property) "+key);
					tob.setProperty(Boolean.TRUE);
					p1.addChild(tob);
					}
				}
				
			}// jarObject!=null
				
				
				}
				
				
				
			}else{  //else
				
				if ( ecd.getRunnable().equals("java") && ecd.getFormat().equals("java/class")){
					System.out.println("--> "+(Literal)rdfnodeContext);
				}
				
			}
		}// for rdfnode iterator
		
	
		//System.out.println(ecd.getExecutableComponent().getModel().get);
		
		root.addChild(p1);
		}
		//p1.addChild(to1);
		//p1.addChild(to2);
		//p1.addChild(to3);
		
		//TreeObject to4 = new TreeObject("Leaf 4");
		//TreeParent p2 = new TreeParent("Parent 2");
		//p2.addChild(to4);
		
		
		//root.addChild(p1);
		//root.addChild(p2);
		TreeParent server = new TreeParent("");
		server.setServer(Boolean.TRUE);
		if(serverStart ==-1){
		serverName = Activator.getMeandreServer();
		}else{	
		serverName = Activator.getMeandreServer().substring(serverStart+3);
		}
		System.out.println(serverName);
		if(Activator.getMeandreServer().startsWith("localhost") ||
			Activator.getMeandreServer().startsWith("127.0.0.1") ){
			server.setLocalServer(Boolean.TRUE);	
		}
		
		
		session.setRoot(server);
		session.getRoot().addChild(root);
		}
	}


	private String getPluginList() {
		ArrayList<Plugin> plist=Activator.getMeandreProxy().getServerPlugins();
		if(plist==null || plist.size()==0){
			return "none found";
		}
		String pluginList=null;
		int count=0;
		Iterator<Plugin> it = plist.iterator();
		
		while(it.hasNext()){
			if(count==0)
			pluginList = it.next().getKey();
			else
			pluginList = pluginList + ", "+it.next().getKey();	
		count++;
		}
		
		return pluginList;
	}

}
