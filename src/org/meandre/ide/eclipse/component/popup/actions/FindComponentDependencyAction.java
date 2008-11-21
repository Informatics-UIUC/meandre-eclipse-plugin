/**
 * @(#) FindComponentDependencyAction.java @VERSION@
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
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;


/** This Action will retrieve the dependecy of the component under selection.
 * 
 * @author Amit Kumar
 * Created on Mar 9, 2008 9:48:37 PM
 *
 */
public class FindComponentDependencyAction implements IObjectActionDelegate, IEditorActionDelegate  {
	
	private ISelection targetSelection;
	private IEditorPart  editorPart;

	/**
	 * Constructor for Action1.
	 */
	public FindComponentDependencyAction() {
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
	@SuppressWarnings({ "unchecked", "static-access" })
	public void run(IAction action) {
		Shell shell = new Shell();
	    ProgressMonitorDialog pd = new ProgressMonitorDialog(shell);
		try {
			    pd.run(true,true, new FindComponentDependencyRunnable(targetSelection,editorPart){
						
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		shell.dispose();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.targetSelection = selection;
		
	}

	/**check if the jar file is removed.
	 * 
	 * @param name
	 * @param filterJarList
	 * @return
	 */
	private boolean notInFilter(String name, ArrayList<String> filterJarList) {
		if (filterJarList.size() == 0) {
			return Boolean.TRUE;
		}
		Iterator<String> it = filterJarList.iterator();
		String filterJar = null;
		name = name.toLowerCase();
		while (it.hasNext()) {
			filterJar = it.next();
			if (name.startsWith(filterJar)) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	public void setActiveEditor(IAction arg0, IEditorPart editorPart) {
		this.editorPart =editorPart;
		System.out.println("here in FindComponentDependency: " + arg0 + "   " + editorPart);
	}



}
