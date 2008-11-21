/*
 * @(#) MeandreViewer.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.views;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

// commented out the Hover Listener -it really does not work
// the 
public class MeandreTreeViewer extends TreeViewer {

	public MeandreTreeViewer(Composite parent) {
		super(parent);
		//this.getTree().addListener(SWT.MouseHover, new TreeMouseHoverListener(this));
	}

	public MeandreTreeViewer(Composite parent, int i) {
		super(parent,i);
		//this.getTree().addListener(SWT.MouseHover, new TreeMouseHoverListener(this));
		
	}

}
