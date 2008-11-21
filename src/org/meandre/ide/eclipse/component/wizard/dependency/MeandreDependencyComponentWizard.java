/*
 * @(#) MeandreDependencyComponentWizard.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.wizard.dependency;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.preferences.PreferenceConstants;
import org.meandre.ide.eclipse.component.wizard.dependency.model.ComponentListModel;
import org.meandre.ide.eclipse.utils.JarUtils;
import org.meandre.ide.eclipse.utils.ProjectClassLoader;

public class MeandreDependencyComponentWizard extends Wizard implements INewWizard{

	ComponentListPage componentListPage;
	private IWorkbench workbench;
	private IStructuredSelection selection;
	public ComponentInstallationPage installPage;
	ComponentListModel model;
	private IJavaProject project;
	ArrayList<String> filterJarList = new ArrayList<String>(10);
	Preferences prefs = Activator.getDefault().getPluginPreferences();
	private URLClassLoader projectClassLoader;
	private URLClassLoader projectClassLoaderWithoutBin;
	private ArrayList<String> jarList;

	public MeandreDependencyComponentWizard(IJavaProject project){
		super();
		this.project = project;
		this.model = new ComponentListModel();
		// list of jar files that should be removed
		String filterJar = prefs.getString(PreferenceConstants.P_FILTERJAR);
		boolean hasAspectJ =  prefs.getBoolean(PreferenceConstants.P_HAS_ASPECT_J);
		projectClassLoader=ProjectClassLoader.getProjectClassLoader(getJavaProject(),hasAspectJ);
		projectClassLoaderWithoutBin = ProjectClassLoader.getProjectClassLoader(project,false, hasAspectJ);
		
		if (filterJar != null) {
			String[] list = filterJar.split(",");
			if (list != null) {
				for (int i = 0; i < list.length; i++) {
					list[i] = list[i].trim();
					if (list[i] != null)
						list[i] = list[i].toLowerCase();
					list[i] = list[i].replace('*', ' ');
					filterJarList.add(list[i].trim());
				}
			}
		}
		
		

		ArrayList<URL> alist = (ArrayList<URL>) ProjectClassLoader.
		getProjectClassPathURLs(getJavaProject(),hasAspectJ);

		Iterator<URL> it = alist.iterator();
		jarList = new ArrayList<String>(20);
		File jarfile = null;
		while (it.hasNext()) {
			jarfile = new File(it.next().getFile());
			if (jarfile.exists()) {

				if (JarUtils.notInFilter(jarfile.getName(),
						getFilterJarList())) {
					if (jarfile.isFile()) {
						jarList.add(jarfile.getAbsolutePath());
					}

				}

			}else{
				System.out.println("Error " + jarfile.getAbsolutePath() + " does not exist");
			}

		}
		
	}
	
	public void addPages(){
		componentListPage = new ComponentListPage(selection);
		addPage(componentListPage);		
		installPage = new ComponentInstallationPage();
		addPage(installPage);
	}
	
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	
	

	
	public boolean canFinish()
	{
		// cannot completr the wizard from the first page
		if (this.getContainer().getCurrentPage() == componentListPage) 
			return false;
		// based on the type of transport return the right flag			
		if (this.getContainer().getCurrentPage()== installPage)
			return true;
		return false;
	}
	
	public boolean performFinish() 
	{
		String summary = model.toString();
		MessageDialog.openInformation(workbench.getActiveWorkbenchWindow().getShell(), 
			"Installation Summary", summary);
		this.model.cleanup();
		return true;
	}
	
	
	public IJavaProject getJavaProject(){
		return this.project;
	}
	
	public ArrayList<String> getFilterJarList(){
		return this.filterJarList;
	}
	
	public URLClassLoader getProjectClassLoader(){
		return projectClassLoader;
	}
	
	public URLClassLoader getProjectClassLoaderWithoutBin(){
		return projectClassLoaderWithoutBin;
	}
	
	public ArrayList<String> getProjectClasspath(){
	return this.jarList;	
	}
	
}
