/**
 * @(#) MeandreContainerPage.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.meandre.ide.eclipse.classpathcontainer;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.meandre.ide.eclipse.component.Activator;


/**This container page allows user to choose the container version. 
 * 
 * @author Amit Kumar
 * Created on Jun 4, 2008 1:06:48 PM
 *
 */
public class MeandreContainerPage extends WizardPage 
               implements IClasspathContainerPage, IClasspathContainerPageExtension {

    private final static String DEFAULT_VER =  Activator.getClasspathContainerUtils().getDetafaultContainer().getName();
    private String[] classpathContainerNameList = null;
    private IJavaProject _proj;
    private Combo _dirCombo;
    private Button _dirBrowseButton;
    private Text _verText;
    private IPath _initPath = null;

    /**
     * Default Constructor - sets title, page name, description
     */
    public MeandreContainerPage() {
        super("Meandre Classpath Container Wizard","Meandre Classpath Container", null);
        setDescription("A classpath container that adds classpath required to create Meandre Components");
        setPageComplete(true);
        ArrayList<ClasspathContainerMetadata> ccm=Activator.getClasspathContainerUtils().getClasspathContainers();
        classpathContainerNameList = Activator.getClasspathContainerUtils().getSupportedClasspathContainerNames();
        
        
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension#initialize(org.eclipse.jdt.core.IJavaProject, org.eclipse.jdt.core.IClasspathEntry[])
     */
    public void initialize(IJavaProject project, IClasspathEntry[] currentEntries) {
        _proj = project;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());
        
        createDirGroup(composite);
        
       // createExtGroup(composite);
        
        setControl(composite);    
    }
    
    /**
     * Creates the directory label, combo, and browse button
     * 
     * @param parent the parent widget
     */
    private void createDirGroup(Composite parent) {
        Composite dirSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns = 3;
        dirSelectionGroup.setLayout(layout);
        dirSelectionGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL| GridData.VERTICAL_ALIGN_FILL));

        new Label(dirSelectionGroup, SWT.NONE).setText("Available Meandre Classpath containers: ");

        _dirCombo = new Combo(dirSelectionGroup, SWT.READ_ONLY|SWT.DROP_DOWN|SWT.SINGLE | SWT.BORDER);
        _dirCombo.setItems(classpathContainerNameList);
      //  _dirCombo.setText( getInitDir() );    
        if(classpathContainerNameList.length>1)
        _dirCombo.select(classpathContainerNameList.length-1);

        setControl(dirSelectionGroup);
    }
    
    /**
     * Creates the extensions label and text box
     * 
     * @param parent parent widget
     */
    private void createExtGroup(Composite parent) {
        Composite extSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns = 2;
        extSelectionGroup.setLayout(layout);
        extSelectionGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL| GridData.VERTICAL_ALIGN_FILL));

        new Label(extSelectionGroup, SWT.NONE).setText("Selected Version");
        
        _verText = new Text(extSelectionGroup,SWT.BORDER);
        _verText.setText(getInitExts()+"                  ");

        setControl(extSelectionGroup);
    }
    
  
    
    /**
     * Extracts the initial extensions list from a path passed in setSelection()
     * 
     * @return the intial comma separated list of extensions
     */
    private String getInitExts() {
        if(_initPath != null && _initPath.segmentCount() > 2 ) {
            return _initPath.segment(2);
        }
        // else 
        return DEFAULT_VER;
    }
    
    /**
     * @return the current directory
     */
    protected String getDirValue() {
        return _dirCombo.getText();
    }
    
    /**
     * @return directory relative to the parent project
     */
    protected String getRelativeDirValue() {
        int projDirLen = _proj.getProject().getLocation().toString().length();
        return getDirValue().substring( projDirLen );
    }
    
    /**
     * Checks that the directory is a subdirectory of the project being configured
     * 
     * @param dir a directory to validate
     * @return true if the directory is valid
     */
    private boolean isDirValid(String version) {
    return	Activator.getClasspathContainerUtils().hasClasspathContainer(version);
    }
    
   
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#finish()
     */
    public boolean finish() {
       if(!isDirValid(getDirValue())) {
            setErrorMessage( "Invalid Version:" + getDirValue());            
            return false;
        }        
        return true;        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#getSelection()
     */
    public IClasspathEntry getSelection() {
    	String versionInfo = this._dirCombo.getText();
    	String version = getVersion(versionInfo);
        IPath containerPath =MeandreProjectContainer.ID.append( "/"+version);
        return  JavaCore.newContainerEntry(containerPath);
    }

    private String getVersion(String versionInfo) {
		if(versionInfo==null){
			return null;
		}
		StringTokenizer stok = new StringTokenizer(versionInfo);
		if(stok.countTokens()>0){
			String version = stok.nextToken();	
			return version;
		}
		return versionInfo;
	}


	/* (non-Javadoc)
     * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#setSelection(org.eclipse.jdt.core.IClasspathEntry)
     */
    public void setSelection(IClasspathEntry containerEntry) {
        if(containerEntry != null) {
            _initPath = containerEntry.getPath();
        }        
    }    
}
