/*
 * @(#) InstallComponentAction.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.popup.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * This Action uploads a component to the server
 * 
 * @author Amit Kumar Created on Mar 10, 2008 11:25:31 AM
 * 
 */
public class InstallComponentAction implements IObjectActionDelegate, IEditorActionDelegate  {
	private ISelection targetSelection;
	private IEditorPart editorPart;

	/**
	 * Constructor for Action1.
	 */
	public InstallComponentAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Shell shell = new Shell();
		ProgressMonitorDialog pd = new ProgressMonitorDialog(shell);

		ICompilationUnit unit = null;
		IJavaProject project=null;
		
		if (targetSelection instanceof IStructuredSelection) {
			Object object = ((IStructuredSelection) targetSelection)
			.getFirstElement();
				// only two supported selection types
			if(object instanceof FileEditorInput || object instanceof ICompilationUnit ){
				if(object instanceof FileEditorInput){	
					FileEditorInput fei= ((FileEditorInput)(object));
					IFile ifile=fei.getFile();
					IProject iproject=ifile.getProject();
					boolean hasJavaNature = Boolean.FALSE;
					try {
						if(iproject.isOpen()){
							hasJavaNature=iproject.hasNature("org.eclipse.jdt.core.javanature");
						}
					} catch (CoreException e) {
						e.printStackTrace();
					}
					if(!hasJavaNature){
						showMessage(iproject.getName() + " is not a java project. ");
						return;
					}

					unit=JavaCore.createCompilationUnitFrom(ifile);
					project = JavaCore.create(iproject);
					if(unit==null){
						showMessage("Could not retrive complication unit from " + 
								ifile.getName()+ " check if the project compiles");
						return;
					}
				}else{
					unit = (ICompilationUnit)object;
					project = ((ICompilationUnit) object).getJavaProject();
				}
				
			}else{
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
					project = JavaCore.create(f.getProject());
					unit = 
					      JavaCore.createCompilationUnitFrom(f);
					System.out.println(unit);
					}
				}
			}
		}if(targetSelection instanceof TextSelection){
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
				project = JavaCore.create(f.getProject());
				unit = 
				      JavaCore.createCompilationUnitFrom(f);
				System.out.println(unit);
				}
			}
		
		}
		
		try {
 			pd.run(true,true, new InstallComponentRunnable(unit,project));
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		
		
	    
	   /*if(targetSelection instanceof IStructuredSelection){
		
		   Iterator<IStructuredSelection> ist = ((IStructuredSelection)targetSelection).iterator();
	    	while(ist.hasNext()){
	    	 try {
	 			pd.run(true,true, new InstallComponentRunnable(ist.next(),editorPart));
	 		} catch (InvocationTargetException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		} catch (InterruptedException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		}	
	    	}
		 
	    }else{
	    	*/
		
		  /*try {
	 			pd.run(true,true, new InstallComponentRunnable(targetSelection,editorPart));
	 		} catch (InvocationTargetException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		} catch (InterruptedException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		}
	 		*/
	 	//	 pd.close(); 	
	  // }

	    
	   
		System.out.println("After the call...");
	}

	// display the error message
	private void sendErrorMessage(String message) {
		// TODO Auto-generated method stub

	}



	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.targetSelection = selection;
	}

	public void setActiveEditor(IAction arg0, IEditorPart editorPart) {
		this.editorPart = editorPart;
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
