/*
 * @(#) GetRepositoryJob.java @VERSION@
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.jobs;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.progress.UIJob;

import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.logger.MeandreLogger;
import org.meandre.ide.eclipse.component.views.MeandreTreeViewer;

import org.meandre.webapp.proxy.client.MeandrePluginProxy;


import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class GetRepositoryJob extends UIJob {
	
	private MeandrePluginProxy meandreProxy;
	private MeandreTreeViewer viewer;
	// check every 5 seconds
	static final int SLEEP_TIME=5000;
	
	
	public GetRepositoryJob(String name) {
		super(name);
	}

	
	public void setProxy(MeandrePluginProxy meandreProxy2) {
		this.meandreProxy = meandreProxy2;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		monitor.beginTask("Get Repository ", 1);
		boolean isConnected = Boolean.FALSE;
		isConnected=meandreProxy.ping();
		//MeandreLogger.logInfo("Getting Repository "+ isConnected);
		//System.out.println("Getting Repository "+ isConnected);
		Activator.isConnected = isConnected;
		if(!isConnected){
		MeandreLogger.logInfo("Can ping server: "+ isConnected);	
		}
		if(isConnected){
			MeandreLogger.logInfo("calling update server version");
			meandreProxy.getServerVersion();
		
			MeandreLogger.logInfo("calling update server plugins");
			meandreProxy.updateServerPlugins();
			
			meandreProxy.flushRepository();	
			QueryableRepository qr=  meandreProxy.getRepository(); 
			
			if(qr!=null){
			Activator.setRepository(qr);
			}
			MeandreLogger.logInfo("Updating MeandreTree viewer in the job...");
			getComponentList();
			if(viewer!=null){
			ISelection selection=viewer.getSelection();
			TreePath[] tp=viewer.getExpandedTreePaths();
			viewer.refresh();
			if(selection!=null){
				viewer.setSelection(selection);
			}
			viewer.expandToLevel(2);
			}
		}else{
			MeandreLogger.logInfo("Getting Repository "+ isConnected + " cannot get repository " + meandreProxy.getServerUrl());
			showMessage(" Cannot get repository " + meandreProxy.getServerUrl() + " the server is not running.");
		}
		
		monitor.done();
		
		
		
		//schedule(SLEEP_TIME);
		
		return Status.OK_STATUS;
	}


	public void setViewer(MeandreTreeViewer viewer) {
		this.viewer = viewer;
	}

	

	
	private void getComponentList(){
		Activator.resetComponentInfo();
		Set<ExecutableComponentDescription> compList= Activator.getQueryableRepository().getAvailableExecutableComponentDescriptions();
		Iterator<ExecutableComponentDescription> it = compList.iterator();
		ExecutableComponentDescription ecd=null;
		while(it.hasNext()){
		ecd = it.next();
	
		for ( RDFNode rdfnodeContext:ecd.getContext() ){
			if ( rdfnodeContext.isResource() ){
				String jarLocation=((Resource)rdfnodeContext).getURI().trim();
				int last=jarLocation.lastIndexOf("/");
				String jarFile = jarLocation.substring(last+1);
				System.out.println("\t"+jarFile);
				if(jarFile != null){
				jarFile=jarFile.trim();
				if(jarFile.length()>0){
					//if(Activator.getComponentInfo(jarLocation)==null){
						Activator.putComponentInfo(jarLocation,meandreProxy.getComponentJarInfo(jarFile));
					//}else{
					//	System.out.println("Already have  " + jarLocation);
					//}
				}
				}
			}
		}
		
		}
	}
		

	private void showMessage(String message) {
		MessageDialog.openInformation(
				viewer.getControl().getShell(),
				"Meandre Component View",
				message);
	}
	
	

}
