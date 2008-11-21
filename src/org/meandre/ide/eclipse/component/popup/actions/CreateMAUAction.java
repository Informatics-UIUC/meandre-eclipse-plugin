/*
 * @(#) CreateMAUAction.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.popup.actions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.zigzag.parser.ParseException;
import org.meandre.zigzag.parser.ZigZag;



public class CreateMAUAction  implements IObjectActionDelegate {
	private ISelection targetSelection;
	
	
	public void setActivePart(IAction action, IWorkbenchPart workpart) {
		// TODO Auto-generated method stub
		
	}

	
	public CreateMAUAction(){
	}
	
	
	public void run(IAction action) {
		MessageConsole mc=Activator.findConsole(Activator.CONSOLE_NAME);
		mc.activate();
		MessageConsoleStream out=mc.newMessageStream();
		if(targetSelection instanceof IStructuredSelection){
			String message = null;
			boolean isError = Boolean.FALSE;
			Object object = ((IStructuredSelection)targetSelection).getFirstElement();
			org.eclipse.core.internal.resources.File file = (org.eclipse.core.internal.resources.File)object;
			
			
			Shell shell = new Shell();
			try {
			   
			  FileInputStream fis = null;
			   fis = (FileInputStream) file.getContents();
			   try {
				System.out.println(file.getContents().available());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(file.getLocation().toOSString());
			   ZigZag parser = new ZigZag(fis);
			   parser.setFileName(file.getLocation().toOSString());
               parser.initFlowGenerator();
               parser.getFlowGenerator().init(file.getLocation().toOSString());
               parser.getFlowGenerator().setPrintStream(new PrintStream(out));
               parser.start();
               parser.getFlowGenerator().generateMAU(parser.getFileName());
			   }catch ( ParseException pe ) {
                   pe.printStackTrace();
                   isError = Boolean.TRUE;
                   message = pe.getMessage();
               } catch (CoreException e) {
				e.printStackTrace();
                isError = Boolean.TRUE;
                message = e.getMessage();
               }finally{
				shell.dispose();
			}
			
               if(isError)
               showMessage("An Error occured: " + message);
               else
               showMessage("MAU File created: " + file.getLocation().toOSString().replace(".zz", ".mau"));
		}else{
			
			showMessage("Select the zz file from the package explorer view.");
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
