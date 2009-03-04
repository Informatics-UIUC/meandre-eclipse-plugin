 /** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */

package org.meandre.ide.eclipse.component.popup.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.*;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.preferences.PreferenceConstants;
import org.meandre.ide.eclipse.utils.ProjectClassLoader;

import org.meandre.annotations.*;
import org.meandre.core.repository.CorruptedDescriptionException;


import org.meandre.tools.components.*;

/**This class creates the RDF descriptor for the component
 * 
 * @author Amit Kumar
 * Created on Mar 9, 2008 12:27:08 AM
 *
 */
public class CreateDescriptorAction implements IObjectActionDelegate, IEditorActionDelegate {
	
	private ISelection targetSelection;
	private IEditorPart editorPart;
	private boolean packagePath;

	/**
	 * Constructor for Action1.
	 */
	public CreateDescriptorAction() {
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
	public void run(IAction action) {
		Shell shell = new Shell();
		MessageConsole mc=Activator.findConsole(Activator.CONSOLE_NAME);
		mc.activate();
		MessageConsoleStream out=mc.newMessageStream();
		
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		String componentDescriptorFolder = prefs.getString(PreferenceConstants.P_DESC_DIR);
		boolean hasAspectJ =  prefs.getBoolean(PreferenceConstants.P_HAS_ASPECT_J);
		this.packagePath = prefs.getBoolean(PreferenceConstants.P_CREATE_PACKAGE_PATH);
		
		String descriptorFileName=null;
		String message = null;
		String className=null;
		ICompilationUnit unit= null;
		if(targetSelection instanceof IStructuredSelection){
			Object object = ((IStructuredSelection)targetSelection).getFirstElement();
			
			if(object instanceof ICompilationUnit){
				unit = (ICompilationUnit)object;
			}
			
		
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
					unit = 
					      JavaCore.createCompilationUnitFrom(f);
					}
				}
			
			
		}
	
		if(unit==null){
			out.println("Error: Could not get the compilation unit.");
			return;
		}
		
		try {
			className=unit.getTypes()[0].getFullyQualifiedName();
		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		try {
		
			ProjectClassLoader pLoader=	new ProjectClassLoader();
		  	Class claszz=pLoader.getProjectClassLoader(unit.getJavaProject(),hasAspectJ).loadClass(className);
			out.println("Got the class: " + claszz.getName());
			out.println("Creating Descriptor.");
	
			
			//Component componentAnnotation = (Component) claszz.getAnnotation(Component.class);
			CreateDefaultComponentDescriptor cdcd = new CreateDefaultComponentDescriptor();
			String rdfContent =cdcd.process(claszz);
			String fileName=this.writeToFile(rdfContent, componentDescriptorFolder,claszz.getName(), claszz.getSimpleName());
			message = " Desriptor created " + fileName;
			
			/*
			if(componentAnnotation!=null){	
				CreateComponentDescriptor ccd = new CreateComponentDescriptor(componentDescriptorFolder);
				ccd.init(claszz);
				descriptorFileName=  ccd.process();
				message = " Desriptor created " + descriptorFileName;
			}else{
				message  = " The class " + className +"  is not a valid component.";
				out.println("[Error] " + message);
			}
			*/
			
			}catch (ClassNotFoundException e) {

			message = "Class not found: " + className;
			out.println("[Error] Class not found "+ className);
			e.printStackTrace();
		} catch (CorruptedDescriptionException e) {
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


	MessageDialog.openInformation(shell,"Meandre Component Plug-in",message);
	shell.dispose();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
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

	public void setActiveEditor(IAction action, IEditorPart editorPart) {
		System.out.println(action.getText());
		this.editorPart = editorPart;
		
	}
	
	/**Write file to the tmp folder
	 * 
	 * @param description
	 * @param className
	 * @param simpleName
	 * @return
	 */
	 private String writeToFile(String description,String componentDescriptorFolder, String className, String simpleName) {
		 String dirPath = componentDescriptorFolder;
		  if (className.lastIndexOf(".") == -1) {
	            dirPath = componentDescriptorFolder;
	        } else {
	        	if(packagePath){
	            dirPath = componentDescriptorFolder + File.separator +
	                     className.substring(0, className.lastIndexOf("."));
	        	}else{
	        	dirPath = componentDescriptorFolder;
	        	}
	            
	        }
	        dirPath = dirPath.replace('.', File.separatorChar);
	        if (!(new File(dirPath)).exists()) {
	            new File(dirPath).mkdirs();
	        }
		 
	        String absoluteFilePath = dirPath +  File.separator + simpleName + ".rdf";
	        
	        BufferedWriter out = null;
	        final String encoding = "UTF-8";
	        try {
	            out = new BufferedWriter(new OutputStreamWriter(new
	                    FileOutputStream(absoluteFilePath), encoding));
	            out.write(description.trim());
	            out.flush();
	            out.close();
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (out != null) {
	                try {
	                    out.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	        return absoluteFilePath;
	    }
	

}
