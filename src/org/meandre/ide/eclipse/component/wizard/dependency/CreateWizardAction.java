/*
 * @(#) CreateWizardAction.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.wizard.dependency;

import org.eclipse.core.runtime.Preferences;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.preferences.PreferenceConstants;
import org.meandre.server.MeandreEngineServicesConstants;

public class CreateWizardAction implements IObjectActionDelegate {
	
	private ISelection targetSelection;
	private IWorkbenchPart part;

	public void setActivePart(IAction action, IWorkbenchPart part) {
		this.part = part;
		
	}

	public void run(IAction action) {
		IProject iproject=null;
		IJavaProject project=null;
		boolean isJavaProject = Boolean.TRUE;
		Object object = ((IStructuredSelection) targetSelection).getFirstElement();
		if(object instanceof IFolder){
			iproject=((IFolder)object).getProject();
		}
		boolean hasJavaNature = Boolean.TRUE;
		try {
			if(iproject.isOpen()){
				hasJavaNature=iproject.hasNature("org.eclipse.jdt.core.javanature");
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if(!hasJavaNature){
			isJavaProject = Boolean.FALSE;
			project= null;
			showMessage("The project is not a valid java project.");
			return;
		}
		if(isJavaProject){
		project = JavaCore.create(iproject);
		}
	
        
		String complianceLevel=	project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		if(!complianceLevel.equalsIgnoreCase("1.5")){
			showMessage("The plugin only supports JDK 1.5. Please change the project's compiler settings.");
			return;
		}
       
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		String url = prefs.getString(PreferenceConstants.P_SERVER);
		int port = prefs.getInt(PreferenceConstants.P_PORT);
	
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		if(Activator.getServerVersion().startsWith("1.3")){
			url = url
					+ ":"
					+ port + "/"
					+ MeandreEngineServicesConstants.ADD_REPOSITORY_URL_1_3;
			}else{
				url = url
				+ ":"
				+ port+ "/"
				+ MeandreEngineServicesConstants.ADD_REPOSITORY_URL_1_4;
		}

		String port_s = prefs.getString(PreferenceConstants.P_PORT);
		String username = prefs.getString(PreferenceConstants.P_LOGIN);
		String password = prefs.getString(PreferenceConstants.P_PASSWORD);
		
		
		if(url==null || port ==0  || username==null || password==null){
			showMessage("The Meandre Server preferences are not setup correctly. Please update the Meandre Component Preferences.");
			return;
		}
		
	
	
		
		
		MeandreDependencyComponentWizard wizard = new MeandreDependencyComponentWizard(project);
		wizard.init(part.getSite().getWorkbenchWindow().getWorkbench(),
		            (IStructuredSelection)targetSelection);
		//Instantiates the wizard container with the wizard and opens it
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().
		            getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
		dialog.open();
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.targetSelection = selection;
		
	}


	private void showMessage(String message) {
		Shell shell =PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialog.openInformation(
				shell,
				"Meandre Install Component",
				message);
		shell.dispose();
	}

}
