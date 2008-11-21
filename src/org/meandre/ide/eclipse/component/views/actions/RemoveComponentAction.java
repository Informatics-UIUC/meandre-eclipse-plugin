/*
 * @(#) RemoveComponentAction.java @VERSION@
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
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.views.MeandreTreeViewer;
import org.meandre.ide.eclipse.component.views.TreeObject;
import org.meandre.ide.eclipse.component.views.TreeParent;

/**This class removes a component from the server
 * 
 * @author Amit Kumar
 * Created on Jul 13, 2008 5:47:10 PM
 *
 */
public class RemoveComponentAction extends Action {
	
	public MeandreTreeViewer viewer;
	
	
	public RemoveComponentAction(MeandreTreeViewer viewer){
		this.viewer = viewer;
	}
	
	public void run() {
		boolean successRemoval = Boolean.FALSE;
		ISelection selection = viewer.getSelection();
		Iterator<IStructuredSelection> it=null;
		if(selection==null||selection.isEmpty() || selection instanceof TreeObject){
			showMessage("Select a component to remove.");
			return;
		}else{
			it =((IStructuredSelection)selection).iterator();	
		
		}
			
		String componentName=null;
		HashMap<String,Boolean> removeMap = new HashMap<String,Boolean>();

		Object tmpObject =  null;
		while(it.hasNext()){
			tmpObject = it.next();
			if(tmpObject.getClass().getName().equals("org.meandre.ide.eclipse.component.views.TreeParent")){
			componentName=((TreeParent)tmpObject).getComponentName();
			boolean value=	Activator.getMeandreProxy().getRemove(componentName);
			
			if(value){
				successRemoval=Boolean.TRUE;
				removeMap.put(componentName, Boolean.TRUE);
				viewer.remove(new TreeParent(componentName));
			}else{
				removeMap.put(componentName, Boolean.FALSE);
			}
			
		}
		}
		
		String message ="Removed Components: \n";
		Iterator<String> itKeys = removeMap.keySet().iterator();
		String key;
		while(itKeys.hasNext()){
			key=itKeys.next();
			if(removeMap.get(key)){
				message = message +	key + " " + removeMap.get(key) +"\n";
			}
		}

		if(successRemoval){
			Activator.repositoryJob.schedule();
			showMessage(message);
		}else{
			showMessage("Could not remove the selected component.")	;
		}

	}
	
	
	private void showMessage(String message) {
		MessageDialog.openInformation(
				viewer.getControl().getShell(),
				"Meandre Component View",
				message);
	}

}
