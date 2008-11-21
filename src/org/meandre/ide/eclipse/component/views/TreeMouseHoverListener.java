/*
 * @(#) TreeMouseHoverListener.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

public class TreeMouseHoverListener implements Listener {
	MeandreTreeViewer viewer;
	final Shell shell = new Shell (Display.getDefault());
	
	Listener labelListener =null;
	
	
	public TreeMouseHoverListener(MeandreTreeViewer viewer){
		this.viewer = viewer;
		shell.setLayout (new FillLayout ());
	
	}
	

	public void handleEvent(Event event) {
		
		labelListener = new Listener () {
			public void handleEvent (Event event) {
				Label label = (Label)event.widget;
				Shell shell = label.getShell ();
				switch (event.type) {
					case SWT.MouseDown:
						Event e = new Event ();
						e.item = (TreeItem) label.getData ("_TREEITEM");
						// Assuming table is single select, set the selection as if
						// the mouse down event went through to the table
						viewer.getTree().setSelection (new TreeItem [] {(TreeItem) e.item});
						viewer.getTree().notifyListeners (SWT.Selection, e);
						shell.dispose ();
						viewer.getTree().setFocus();
						break;
					case SWT.MouseExit:
						shell.dispose ();
						break;
				}
			}
		};
		Shell tip = null;
		Label label = null;
		System.out.println("Hover: " + event);
		  Point point = new Point(event.x, event.y);
		  TreeItem item = viewer.getTree().getItem(point);
		  if (item != null) {
				if (tip != null  && !tip.isDisposed ()) tip.dispose ();
				tip = new Shell (shell, SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
				tip.setBackground (Display.getDefault().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
				FillLayout layout = new FillLayout ();
				layout.marginWidth = 2;
				tip.setLayout (layout);
				label = new Label (tip, SWT.NONE);
				label.setForeground (Display.getDefault().getSystemColor (SWT.COLOR_INFO_FOREGROUND));
				label.setBackground (Display.getDefault().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
				label.setData ("_TREEITEM", item);
				System.out.println("DATA IS: " + item.getData()+" ITEM is: " + item);
				label.setText (item.getText());
				label.addListener (SWT.MouseExit, labelListener);
				label.addListener (SWT.MouseDown, labelListener);
				Point size = tip.computeSize (SWT.DEFAULT, SWT.DEFAULT);
				Rectangle rect = item.getBounds (0);
				Point pt =viewer.getTree().toDisplay (rect.x, rect.y);
				tip.setBounds (pt.x, pt.y, size.x, size.y);
				tip.setVisible (true);
			}
		
	}

}
