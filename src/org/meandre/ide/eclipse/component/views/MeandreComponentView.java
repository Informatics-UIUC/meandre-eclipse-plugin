/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */

package org.meandre.ide.eclipse.component.views;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;

import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.meandre.ide.eclipse.IImageKeys;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.views.actions.DownloadComponentAction;
import org.meandre.ide.eclipse.component.views.actions.FindComponentSourceAction;
import org.meandre.ide.eclipse.component.views.actions.RemoveComponentAction;


/**This view part displays the list of the components.
 * 
 * @author Amit Kumar
 * Created on March 03, 2008 9:12:00 PM
 *
 */
public class MeandreComponentView extends ViewPart {
	private MeandreTreeViewer viewer;
	
	private Action action1;
	private Action action2;
	private Action action3;
	private Action action4;

	private Action doubleClickAction;
	DrillDownAdapter drillDownAdapter;
	

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public MeandreComponentView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new MeandreTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		Activator.repositoryJob.setViewer(viewer);
		
		drillDownAdapter = new DrillDownAdapter(viewer);
		Session session = new Session();
		viewer.setContentProvider(new TreeViewContentProvider(session));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MeandreComponentView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		//getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		//manager.add(action1);
		//manager.add(new Separator());
		//manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(action3);
		manager.add(action4);
		
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(action3);
		manager.add(action4);
		
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				Activator.repositoryJob.schedule();
		}
		};
		action1.setText("Refresh");
		action1.setToolTipText("Refresh Component View");
		action1.setImageDescriptor(Activator.getImageDescriptor(IImageKeys.REFRESH_COMPONENTS));

		
		action2 = new RemoveComponentAction(viewer); 
		action2.setText("Remove");
		action2.setToolTipText("Remove Component");
		action2.setImageDescriptor(Activator.getImageDescriptor(IImageKeys.DELETE_COMPONENT));
	
		action3 = new FindComponentSourceAction(viewer);
		action3.setText("Open Source");
		action3.setToolTipText("Open Source");
		action3.setImageDescriptor(Activator.getImageDescriptor( IImageKeys.FIND_COMPONENT_SRC));


		action4 = new DownloadComponentAction(viewer,System.getProperty("user.home"));
		action4.setText("Download Descriptor");
		action4.setToolTipText("Download Descriptor");
		action4.setImageDescriptor(Activator.getImageDescriptor( IImageKeys.DOWNLOAD_COMPONENT));
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if(viewer.getExpandedState(obj)){
				viewer.collapseToLevel(obj,1);	
				}else{
				viewer.expandToLevel(obj,2);
				}
			}
		};




	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
				viewer.getControl().getShell(),
				"Meandre Component View",
				message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}


}