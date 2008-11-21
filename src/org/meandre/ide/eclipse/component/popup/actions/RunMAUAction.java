/*
 * @(#) RunMAUAction.java @VERSION@
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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;



/**This class runs a MAU file
 * 
 * @author Amit Kumar
 * Created on Jul 13, 2008 5:42:10 PM
 *
 */
public class RunMAUAction  implements IObjectActionDelegate {
	private ISelection targetSelection;
	
	
	public void setActivePart(IAction action, IWorkbenchPart workpart) {
		// TODO Auto-generated method stub
		
	}

	
	public RunMAUAction(){
	}
	
	
	public void run(IAction action) {
		if(targetSelection instanceof IStructuredSelection){
		
				Shell shell = new Shell();
			    ProgressMonitorDialog pd = new ProgressMonitorDialog(shell);
				try {
					pd.run(true,true, new RunMAURunnable(targetSelection));
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("After the call...");
		}
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.targetSelection=selection;
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
