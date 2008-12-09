/*
 * @(#) ComponentListPage.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.wizard.dependency;



import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import org.eclipse.swt.layout.GridLayout;

import org.meandre.annotations.Component;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.preferences.PreferenceConstants;
import org.meandre.ide.eclipse.component.wizard.dependency.model.ComponentData;
import org.meandre.ide.eclipse.component.wizard.dependency.model.ComponentListModel;
import org.meandre.ide.eclipse.utils.ProjectClassLoader;

public class ComponentListPage extends WizardPage implements Listener{

	private IStructuredSelection targetSelection;
	private Table table;
	private ProgressBar progressBar;
	private List list;
	private Label label;
	private Button findComponentButton;
	private Button selectAllButton;
	boolean isJavaProject = Boolean.TRUE;
	String baseFolder;
	Preferences prefs;
	boolean hasAspectJ = Boolean.FALSE;
	boolean storeSource = Boolean.FALSE;
	String outputLocation;
	URLClassLoader urlClassloader;
	TableEditor editor;
	private Label componentLabel;
	ArrayList<IFolder> folders;

	protected ComponentListPage(IStructuredSelection selection) {
		super("Component List");
		this.targetSelection = selection;
		setTitle("Discovering Components");
		setDescription("Searching for components");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		baseFolder = workspace.getRoot().getLocation().toPortableString();
		prefs = Activator.getDefault().getPluginPreferences();
		hasAspectJ =  prefs.getBoolean(PreferenceConstants.P_HAS_ASPECT_J);
		storeSource = prefs.getBoolean(PreferenceConstants.P_INCLUDE_SOURCE);
		folders = new ArrayList<IFolder>(5);

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		Shell shell =PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String[] columnNames = {"Component Class", "Component Name"};
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns=1;
		composite.setLayout(gridLayout);
		label = new Label(composite, SWT.BORDER);
		label.setText("Package Directory");
		list = new List(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL| SWT.H_SCROLL);
		GridData gridData =new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = 200;
		gridData.widthHint = 400;
		list.setLayoutData(gridData);
		Label lab =new Label(composite,SWT.BORDER);
		lab.setText("List of Components");

		table = new Table (composite,SWT.CHECK | SWT.BORDER | SWT.MULTI| SWT.V_SCROLL);
		table.setLinesVisible (false);
		table.setHeaderVisible(true);
		table.addListener(SWT.Selection, this);

		componentLabel = new Label(composite, SWT.BORDER);
		componentLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));



		for (int i=0; i<columnNames.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(columnNames[i]);
			column.setWidth (300);
		}
		gridData =new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = 300;
		gridData.widthHint = 400;

		table.setLayoutData(gridData);

		//select All button don't display it
		selectAllButton = new Button(composite, SWT.CHECK);
		selectAllButton.setText("Select All Components");
		selectAllButton.setEnabled(false);
		selectAllButton.addListener(SWT.Selection, this); 
	

		// Create a smooth progress bar
		progressBar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.SMOOTH);
		progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		progressBar.setMinimum(0);
		progressBar.setMaximum(30);

		findComponentButton = new Button(composite, SWT.PUSH);
		findComponentButton.setText("Find");
		findComponentButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		findComponentButton.addListener(SWT.Selection, this);
		setControl(composite);	
		addSelection();
	}



	private void addSelection (){
		if(targetSelection instanceof IStructuredSelection){
			//Object object = ((IStructuredSelection) targetSelection)
			//.getFirstElement();
			Iterator<IStructuredSelection> its=((IStructuredSelection) targetSelection).iterator();
			Object object = null;
			while(its.hasNext()){
				//	System.out.println("=====> " + ((IFolder)its.next()));
				object = its.next();
				//}

				if(object instanceof IFolder){
					folders.add((IFolder)object);
					outputLocation= new File(baseFolder+ ProjectClassLoader.getProjectOutput(getJavaProject())).getAbsolutePath();
					urlClassloader=ProjectClassLoader.getProjectClassLoader(getJavaProject(),hasAspectJ);
					if(urlClassloader==null){
						System.out.println("[INFO] NULL project project classloader is null");	
					}else{
						System.out.println("[INFO] 12 project " +  urlClassloader.getURLs().length);
					}
					list.add(((IFolder)object).getProjectRelativePath().toOSString());
					list.select(list.getTopIndex());
				}
			}
		}



	}

	/**Adds all the components that have a valid @Component annotation
	 * 
	 */
	private void findComponents() {
		selectAllButton.setEnabled(false);
		Iterator<IFolder> folder =  folders.iterator(); 
		int count=0;
		progressBar.setSelection(0);
		table.removeAll();

		while(folder.hasNext()){
			IJavaElement packageElement = JavaCore.create(folder.next());

			progressBar.setSelection(0);

			if(packageElement.getElementType()==  IJavaElement.PACKAGE_FRAGMENT  
					|| packageElement.getElementType()==  IJavaElement.PACKAGE_FRAGMENT_ROOT){
				System.out.println("yes package:");
			}

			if(packageElement.getElementType()==  IJavaElement.PACKAGE_FRAGMENT ){
				IPackageFragment ipf = (IPackageFragment) packageElement;

				try {

					ICompilationUnit[] ipfcompunit=ipf.getCompilationUnits();
					progressBar.setMaximum(ipfcompunit.length);
					if(ipfcompunit!=null){
						for(int i=0; i < ipfcompunit.length; i++){

							progressBar.setSelection(i+1);
							if(ipfcompunit[i].getTypes()!=null && 
									ipfcompunit[i].isStructureKnown() && 
									!ipfcompunit[i].getElementName().equalsIgnoreCase("package-info.java")){
								System.out.println(ipfcompunit[i].getElementName() + "----->" + ipfcompunit[i].getElementType() );
								String className = ipfcompunit[i].getTypes()[0].getFullyQualifiedName();
								IType itype=getJavaProject().findType(className);
								if(itype==null){
									System.out.println("Error could not get the " + className);
									continue;
								}

								//System.out.println("Output is here: " + outputLocation);
								File outputLocationDir = new File(outputLocation);
								System.out.println("[INFO] 5 output loc ");


								String componentPath = new File(outputLocationDir,
										className.replace('.', File.separatorChar) + ".class").getAbsolutePath();
								System.out.println("[INFO] 6.2 componentPath " + componentPath);


								Class claszz=null;
								try{
									claszz = urlClassloader.loadClass(className);
								}catch(Exception ex){
									System.out.println("[ERROR] className " + className + "  " + ex.getMessage());
									return;
								}

								Component componentAnnotation = (Component) claszz.getAnnotation(Component.class);
								if(componentAnnotation!=null){
									ComponentData cdata = new ComponentData();
									cdata.setClassName(className);
									cdata.setSelected(Boolean.FALSE);
									cdata.setInstalled(Boolean.FALSE);
									cdata.setType("file");
									cdata.setName(componentAnnotation.name());
									cdata.setCompilationUnit(ipfcompunit[i]);
									count++;
									componentLabel.setText("Found: " + className + " ("+count+")");
									getModel().addData(className, cdata);
									addDataToTable(cdata);
								}

							}

						}
					}
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(count>0){
					selectAllButton.setEnabled(true);
				}
				componentLabel.setText("Found "+ count  + " components.");

			}

			System.out.println(packageElement.getElementName() + "   " + packageElement.getElementType());
		}
		/*	System.out.println(packageElement);
			if(itype==null){
				System.out.println("itype ==null");
			}else{
				System.out.println(itype);
			}
		 */



	}


	private void addDataToTable(ComponentData cdata) {
		TableItem tableItem = new TableItem(table, SWT.NONE);
		tableItem.setText(0,cdata.getClassName());
		tableItem.setText(1, cdata.getName());
	}

	public void handleEvent(Event event) {
		System.out.println("----->"+event);
		Status status = new Status(IStatus.OK, "not_used", 0, "", null);
		if(event.widget== selectAllButton){
			boolean itemStatus =((Button)event.widget).getSelection();//          ((TableItem)event.item).getChecked();
			
			Set<String> keyset=   getModel().getkeySet();
			for(String key:keyset){
				ComponentData cdata=   getModel().getData(key);
				cdata.setSelected(itemStatus);
				getModel().addData(key, cdata);
			}
			int itemCount=table.getItemCount();
			for(int i=0; i < itemCount; i++){
				Item item=table.getItem(i);
				((TableItem) item).setChecked(itemStatus);
			}
			
			String prevText=this.componentLabel.getText();
			int numSelected = getModel().getNumSelected();
			if(numSelected>0){
				prevText = "Found " + getModel().getkeySet().size() +" components. " +"Selected " + numSelected + " for installation.";
				this.componentLabel.setText(prevText);
			}else{
				this.componentLabel.setText("No Component selected.");
				status = new Status(IStatus.ERROR, "not_used", 0, 
						"Select atleast one component to continue", null);	   
			}



		}else if(event.widget== findComponentButton){
			findComponents();
		}else if(event.widget == table){
			String string = event.detail == SWT.CHECK ? "Checked": "Selected";
			boolean tableItemStatus =           ((TableItem)event.item).getChecked();
			System.out.println(event.item + " " + string +" " +event.stateMask + " " +tableItemStatus);
			String key= ((TableItem)event.item).getText();
			ComponentData cdata=   getModel().getData(key);
			assert cdata != null;
			cdata.setSelected(tableItemStatus);
			getModel().addData(key, cdata);
			String prevText=this.componentLabel.getText();
			int numSelected = getModel().getNumSelected();
			if(numSelected>0){
				prevText = "Found " + getModel().getkeySet().size() +" components. " +"Selected " + numSelected + " for installation.";
				this.componentLabel.setText(prevText);
			}else{
				status = new Status(IStatus.ERROR, "not_used", 0, 
						"Select atleast one component to continue", null);	   
			}
		}
		applyToStatusLine(status);
		getWizard().getContainer().updateButtons();
	}

	public IWizardPage getNextPage()
	{    		
		ComponentInstallationPage page = ((MeandreDependencyComponentWizard)getWizard()).installPage;
		page.onEnterPage();
		return page;
	}

	/**
	 * @see IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage()
	{
		if (getErrorMessage() != null) return false;
		if(getModel().getNumSelected()>0){
			return true;
		}
		return false;
	}

	private ComponentListModel getModel() {
		MeandreDependencyComponentWizard wizard = (MeandreDependencyComponentWizard) getWizard();
		return wizard.model;
	}

	/**
	 * Applies the status to the status line of a dialog page.
	 */
	private void applyToStatusLine(IStatus status) {
		String message= status.getMessage();
		if (message.length() == 0) message= null;
		switch (status.getSeverity()) {
		case IStatus.OK:
			setErrorMessage(null);
			setMessage(message);
			break;
		case IStatus.WARNING:
			setErrorMessage(null);
			setMessage(message, WizardPage.WARNING);
			break;				
		case IStatus.INFO:
			setErrorMessage(null);
			setMessage(message, WizardPage.INFORMATION);
			break;			
		default:
			setErrorMessage(message);
		setMessage(null);
		break;		
		}
	}

	private IJavaProject getJavaProject() {
		return ((MeandreDependencyComponentWizard)this.getWizard()).getJavaProject();
	}

}
