/*
 * @(#) ViewContentProvider.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.meandre.ide.eclipse.component.Activator;
/*
 * The content provider class is responsible for
 * providing objects to the view. It can wrap
 * existing objects in adapters or simply return
 * objects as-is. These objects may be sensitive
 * to the current input of the view, or ignore
 * it and always show the same content 
 * (like Task List, for example).
 */
 class ViewContentProvider implements IStructuredContentProvider {
	 
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		System.out.println("firing input changed : " + oldInput + "  " + newInput + " " + v.toString());
		
	}
	public void dispose() {
	}
	public Object[] getElements(Object parent) {
		if(Activator.getQueryableRepository()==null){
			return new String[]{"Not connected..."};
		}
		return Activator.getQueryableRepository().getAvailableExecutableComponentDescriptions().toArray(new Object[5]);
		//return new String[] { "One", "Two", "Three"+ Math.random()};
	}
}