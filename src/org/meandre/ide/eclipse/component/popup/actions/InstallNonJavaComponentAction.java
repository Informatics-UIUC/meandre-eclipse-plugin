/*
 * @(#) InstallNonJavaComponentAction.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.popup.actions;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.preferences.PreferenceConstants;
import org.meandre.server.MeandreEngineServicesConstants;
import org.meandre.tools.components.InstallComponent;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**Install a non java component.
 * 
 * @author Amit Kumar
 * Created on Jul 13, 2008 5:41:34 PM
 *
 */
public class InstallNonJavaComponentAction implements IObjectActionDelegate {
	
	private  ISelection targetSelection;
	
	public void setActivePart(IAction action, IWorkbenchPart wbp) {
		// TODO Auto-generated method stub
		
	}

	public void run(IAction action) {
		// TODO Auto-generated method stub
		System.out.println("Run... " + this.getClass().getName());
		Model m = ModelFactory.createDefaultModel();
		
		  	Preferences prefs = Activator.getDefault().getPluginPreferences();
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			if (targetSelection instanceof IStructuredSelection) {
				Object object = ((IStructuredSelection) targetSelection)
						.getFirstElement();
				String workspacePath = workspace.getRoot().getLocation().toOSString();
				System.out.println(object.getClass() + "  ---  "+ object);
				
				if(object instanceof org.eclipse.core.internal.resources.File){
					org.eclipse.core.internal.resources.File file = (org.eclipse.core.internal.resources.File)object;
					InputStream is=null;
					String charencoding=null;
					try {
					is=	file.getContents();
					charencoding = file.getCharset();
					m.read(is, charencoding);
					RepositoryImpl repository = new RepositoryImpl(m);
			        // Get all the component descriptions
			        Set<org.meandre.core.repository.ExecutableComponentDescription> componentDescriptions =
			         repository.getAvailableExecutableComponentDescriptions();
			        System.out.println("Number of components found: "+ componentDescriptions.size());
			        if(componentDescriptions.size()==0){
			        showMessage("Error: The component descriptor is invalid.");	
			        return;
			        }
					} catch (CoreException e) {
						e.printStackTrace();
					}
					
					IPath location=file.getFullPath();
					
					System.out.println("File Path is: " + location.toOSString());
					
					
			String url = prefs.getString(PreferenceConstants.P_SERVER);
			int port = prefs.getInt(PreferenceConstants.P_PORT);
			
			if(!url.startsWith("http://")){
				url = "http://"+url;
			}
			if (url.endsWith("/")) {
				url = url.substring(0, url.length() - 1);
			}
			String jarInfoUrl;
			if(Activator.getServerVersion().startsWith("1.3")){
			jarInfoUrl = url+":"+port+"/"+MeandreEngineServicesConstants.JAR_INFO_URL;
			url = url
					+ ":"
					+ port + "/"
					+ MeandreEngineServicesConstants.ADD_REPOSITORY_URL_1_3;
			}else{
				jarInfoUrl = url+":"+port+"/"+MeandreEngineServicesConstants.JAR_INFO_URL;
				url = url
				+ ":"
				+ port +"/"
				+ MeandreEngineServicesConstants.ADD_REPOSITORY_URL_1_4;
			}
			String port_s = prefs.getString(PreferenceConstants.P_PORT);
			String username = prefs.getString(PreferenceConstants.P_LOGIN);
			String password = prefs.getString(PreferenceConstants.P_PASSWORD);
			boolean embed = prefs.getBoolean(PreferenceConstants.P_EMBED);
			boolean overwrite = prefs.getBoolean(PreferenceConstants.P_OVERWRITE);
			boolean dump = Boolean.FALSE;
			
			
			InstallComponent ic = new InstallComponent(url, jarInfoUrl,port,username, password);
			boolean success=ic.uploadComponent(new File( workspacePath +File.separator +location.toOSString()),
					overwrite, dump, embed, null);
			
				if(success){
					showMessage("Component Uploaded.");
				}else{
					showMessage("Error uploading component. ");
				}
					
			}else{
				showMessage("Select the rdf from the package explorer view.");
			}
				
			}
		
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		System.out.println("selectionChanged: " + action +  "   " + selection);
		this.targetSelection = selection;
	}
	

	private void showMessage(String message) {
		Shell shell = new Shell();
		MessageDialog.openInformation(
				shell,
				"Meandre Install Component",
				message);
		shell.dispose();
	}
	

}
