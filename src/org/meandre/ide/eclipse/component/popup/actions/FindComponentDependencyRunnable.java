/*
 * @(#) FindComponentDependencyRunnable.java @VERSION@
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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.preferences.PreferenceConstants;
import org.meandre.ide.eclipse.utils.JarUtils;
import org.meandre.ide.eclipse.utils.ProjectClassLoader;
import org.meandre.annotations.Component;
import org.meandre.tools.components.*;

/**This class runs the dependency analysis for a component.
 * 
 * @author Amit Kumar
 * Created on Jul 13, 2008 5:40:27 PM
 *
 */
public class FindComponentDependencyRunnable implements IRunnableWithProgress {
	
	

	String message = null;
	private ISelection targetSelection;
	private IEditorPart editorPart;
	
	public FindComponentDependencyRunnable(ISelection targetSelection, IEditorPart editorPart) {
		this.targetSelection= targetSelection;
		this.editorPart = editorPart;
	}
	
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		monitor.beginTask("finding component dependencies",100);
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		boolean hasAspectJ =  prefs.getBoolean(PreferenceConstants.P_HAS_ASPECT_J);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String baseFolder = workspace.getRoot().getLocation().toPortableString();
		ArrayList<String> resourceList = new ArrayList<String>(2);
		
		MessageConsole mc=Activator.findConsole(Activator.CONSOLE_NAME);
		mc.activate();
		MessageConsoleStream out=mc.newMessageStream();
		
		
		 
					
			// list of jar files that should be removed
		  String filterJar = prefs.getString(PreferenceConstants.P_FILTERJAR);
			
			
			ArrayList<String> filterJarList = new ArrayList<String>(10);
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
		
		
			Object object=null;
		
		if(targetSelection instanceof IStructuredSelection){
			object = ((IStructuredSelection)targetSelection).getFirstElement();
		}else if(targetSelection instanceof TextSelection){
			if(this.editorPart!= null){
				IFile f = ((IFileEditorInput) this.editorPart.getEditorInput()).getFile();
				System.out.println(f.getFullPath().toOSString());
				IProject iproject = f.getProject();
				boolean hasJavaNature = Boolean.FALSE;
				try {
					if(iproject.isOpen()){
						hasJavaNature=iproject.hasNature("org.eclipse.jdt.core.javanature");
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
			
				if(hasJavaNature){
				IJavaProject project = JavaCore.create(f.getProject());
				object = 
				      JavaCore.createCompilationUnitFrom(f);
				System.out.println(object);
				}
			}
		
		}
		
		if(object==null){
			out.println("[Error] could not retrieve the compilation unit.");
			return;
		}
			
			if(object instanceof ICompilationUnit){
				try {
						String className=((ICompilationUnit)object).getTypes()[0].getFullyQualifiedName();
						ProjectClassLoader pLoader=	new ProjectClassLoader();
						IJavaProject project = ((ICompilationUnit)object).getJavaProject();
						IType itype=project.findType(className);
						IPath parentFile=null;
						if(itype!=null){
							IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(itype.getPath());
							parentFile =file.getParent().getLocation();
						}	
							
							
						
						
						
						if(monitor.isCanceled()){
							return;
						}
						 
						 monitor.subTask("getting the class "+ className);
						 monitor.worked(10);
						 out.println("getting the class "+ className);
						 Class claszz=pLoader.getProjectClassLoader(project,hasAspectJ).loadClass(className);
						 Thread.currentThread().sleep(1000);
						 
						 

						 if(monitor.isCanceled()){
							return;
						 }
						 monitor.subTask("getting the project classpath");
						 monitor.worked(20);
				
						 ArrayList alist = (ArrayList)pLoader.getProjectClassPathURLs(project,hasAspectJ);
						
						 Thread.currentThread().sleep(1000);
						 
						 if(monitor.isCanceled()){
							return;
						 }
						
						
						monitor.worked(40);
						 
						out.println("number of jars in the project classpath: "+alist.size());
						Iterator<URL> it = alist.iterator();
						ArrayList<String> jarList = new ArrayList<String>(20);
						
						File jarfile = null;
						while (it.hasNext()) {
							jarfile = new File(it.next().getFile());
							if (jarfile.exists()) {

								if (JarUtils.notInFilter(jarfile.getName(),
										filterJarList)) {
									if (jarfile.isFile()) {
										jarList.add(jarfile.getAbsolutePath());
									}

								}

							}

						}
						
						
						out.println("number of jars in the project classpath after filtering: "+jarList.size());
						
						
						String outputLocation =new File(baseFolder + pLoader.getProjectOutput(project)).getAbsolutePath();
						
						monitor.subTask("getting the dependencies");
						out.println("getting the dependencies");
						
						monitor.worked(60);
						Component componentAnnotation = (Component) claszz.getAnnotation(Component.class);
						
						FindComponentDep fcd = new FindComponentDep(jarList);
						String componentPath =outputLocation +File.separator+ className.replace('.',File.separatorChar)+".class";// getComponentPath(className,outputLocatio);
						
						out.println("initiating the find component dependency: "+componentPath);
						for(int i=0;i < jarList.size();i++){
						out.println("appJarList --> "+ jarList.get(i));
						}
						
						
						if(componentAnnotation==null){
						fcd.execute(componentPath);
						}else if(componentAnnotation.dependency()==null){
						fcd.execute(componentPath);
						}else{
						fcd.execute(componentPath, componentAnnotation.dependency());
						}
						
						//TODO: Move to a a function
						// get the resources
						if(componentAnnotation!=null && componentAnnotation.resources()!=null){
							String[] resources = componentAnnotation.resources();
							for(int i=0; i < resources.length;i++){
								
								if(resources[i]!=null && resources[i].trim().length()>0){
								 File resourceFile = new File(parentFile.toOSString(),resources[i]);
								 if(resourceFile.exists()){
									 monitor.subTask("getting the resources " + resources[i]);
									 out.println("getting the resources");
									 monitor.worked(60+i);
									 resourceList.add(resourceFile.getAbsolutePath());
								 }else{
									 //TODO:  RESOURCE NOT FOUND
								 }
						
								}
							}
							
							
						}
						
						if(monitor.isCanceled()){
							return;
						}
						
						ArrayList<String> dlist=fcd.getDependencyList();
						out.println("Component: "+ componentPath + "");
						out.println("Found : " + dlist.size() + " dependencies.");
						Iterator<String> it1 = dlist.iterator();
						String jarFile=null;
						while(it1.hasNext()){
							jarFile = it1.next();
							message = message +"," +jarFile;
							out.println(jarFile);
						}
						
						it1= resourceList.iterator();
						String resourceFileStringList=null;
						int countResource=0;
						String thisResource=null;
						while(it1.hasNext()){
							thisResource= it1.next();
							if(countResource==0){
								resourceFileStringList = thisResource;
							}else{
								resourceFileStringList = resourceFileStringList + "," + thisResource;
							}
							System.out.println("Resource : " + thisResource);
							out.println("Resource : " + thisResource);
						}
						
						monitor.subTask("Done.");
						out.println("Done.");
						
						monitor.worked(90);
						
				
					
				} catch (JavaModelException e) {
					message = "Java Model Exception " + e.getMessage();
					out.println("[Error] " + message);
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				}
				
			}
		
			
		
		
			
	
		
		monitor.done();
		
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
