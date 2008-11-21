/**
 * @(#) FindComponentSourceAction.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.views.actions;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.views.MeandreTreeViewer;
import org.meandre.ide.eclipse.component.views.TreeParent;

public class FindComponentSourceAction extends Action {

	private MeandreTreeViewer viewer;
	
	public  FindComponentSourceAction(MeandreTreeViewer viewer){
		super();
		this.viewer = viewer;
	}
	
		public void run(){
			ISelection selection = viewer.getSelection();
			if(selection==null||selection.isEmpty()){
				showMessage("Select a component to view the source code.");
				return;
			}
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			System.out.println("Number of projects: "+workspace.getRoot().getProjects().length);
			IProject[]  projects=workspace.getRoot().getProjects();
			IStructuredSelection iss=(IStructuredSelection)selection;

			if(!(iss.getFirstElement() instanceof TreeParent)){
				showMessage("Select a component to view the source code.");
				return;
			}
			
			TreeParent tp=((TreeParent)iss.getFirstElement());
			String componentClass = tp.getComponentClass();

			boolean foundSource = Boolean.FALSE;
			for(int i=0; i < projects.length;i++){
				boolean hasJavaNature=false;
				try {
					if(projects[i].isOpen()){
						hasJavaNature=projects[i].hasNature("org.eclipse.jdt.core.javanature");
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if(hasJavaNature){
					//tp.getComponentName();

					System.out.println("Component Name: "  + componentClass);
					System.out.println("Is a Java Project: " + projects[i].getName());
					IJavaProject ijp=  JavaCore.create( projects[i]);
					System.out.println("Len class: "+ ijp.readRawClasspath().length);
					try {
						IType itype=ijp.findType(componentClass);
						if(itype!=null){
							System.out.println(itype.toString());

							IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(itype.getPath());
							IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();//<the page to open the editor in>;
							int componentLoc=itype.getCompilationUnit().getSource().indexOf("@Component");
							if(componentLoc==-1){
								componentLoc=0;
							}
							HashMap map = new HashMap();
							map.put(IMarker.CHAR_START, new Integer(componentLoc));
							map.put(IMarker.CHAR_END, new Integer(componentLoc + "@Component".length()));
							map.put(IWorkbenchPage.EDITOR_ID_ATTR, Activator.JAVA_EDITOR_ID);

							
							IMarker marker;
							try {
								marker = file.createMarker(IMarker.TEXT);
								marker.setAttributes(map);
								IDE.openEditor(page, marker); 
								marker.delete();
							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							foundSource = Boolean.TRUE;
							break;
						}else{
							System.out.println("Error: itype is null" );
						}
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}else{
					System.out.println("Not a Java Project: " + projects[i].getName());
				}


			}

			if(!foundSource){
				showMessage("Could not locate source for the component in the open java projects: " + componentClass);
			}

		}
		
		private void showMessage(String message) {
			MessageDialog.openInformation(
					viewer.getControl().getShell(),
					"Meandre Component View",
					message);
		}

}
