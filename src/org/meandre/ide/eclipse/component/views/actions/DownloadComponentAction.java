/**
 * @(#) DownloadComponentAction.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.views.actions;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.views.MeandreTreeViewer;
import org.meandre.ide.eclipse.component.views.TreeParent;

/**This class creates the component descriptor.
 * 
 * @author Amit Kumar
 * Created on Jul 13, 2008 5:45:24 PM
 *
 */
public class DownloadComponentAction extends Action {
	
	private MeandreTreeViewer viewer;
	private String projectPath;
	private static final String[] FILTER_NAMES = {
	      "RDF files (*.rdf)",
	       "All Files (*.*)"};

	  // These filter extensions are used to filter which files are displayed.
	  private static final String[] FILTER_EXTS = { "*.rdf","*.*"};
	
	public DownloadComponentAction(MeandreTreeViewer viewer, String projectPath){
		super();
		
		this.viewer = viewer;
		this.projectPath = projectPath;
	}

	
	public void run(){
		ISelection selection = viewer.getSelection();
		
		IStructuredSelection iss=(IStructuredSelection)selection;
		
		
		if(selection==null||selection.isEmpty() || !(iss.getFirstElement() instanceof TreeParent)){
			showMessage("Select a component to download");
			return;
		}				
		
		System.out.println("selection is: " + iss.getFirstElement().getClass().getName());
		TreeParent tp = ((TreeParent)iss.getFirstElement());
		
		if(tp.getComponentUri()==null){
			showMessage("Select a component to download");
			return;	
		}
		
		
		if(!Activator.getMeandreProxy().isReady()){
			showMessage("Meandre Proxy is not connected. Check the server and the address.");
			return;
		}
		
		String componentDescriptor=Activator.getMeandreProxy().getComponentDescriptor(tp.getComponentUri());
	
		if(componentDescriptor!=null){
			FileDialog dlg = new FileDialog(viewer.getControl().getShell(), SWT.SAVE);
			
	        dlg.setFilterNames(FILTER_NAMES);
	        dlg.setFilterExtensions(FILTER_EXTS);
	        dlg.setFilterPath(projectPath);
	        
	        
	        
	        String fn = dlg.open();
	        if (fn != null) {
	        	File file = new File(fn.trim());
	        	
	        	FileWriter fileWriter;
				try {
					fileWriter = new FileWriter(file);
					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
					bufferedWriter.write(componentDescriptor);
					bufferedWriter.flush();
					bufferedWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					dlg = null;
				}
	        	
	        }
		}
		
		//IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		//page.getActiveEditor().
		/*IFile ifile=null;
		try {
			IDE.openEditor(page, new  FileEditorInput(ifile),
					"org.eclipse.ui.editorID", 
					Boolean.TRUE);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		*/

	}


	private void showMessage(String message) {
		MessageDialog.openInformation(
				viewer.getControl().getShell(),
				"Meandre Component View",
				message);
	}
}
