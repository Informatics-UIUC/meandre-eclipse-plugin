/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */

package org.meandre.ide.eclipse.component.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.TemplateException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.project.MeandreComponentProject;


/** This class is based on the wicket's eclipse IDE
 * 
 * @author Amit Kumar
 * Created on Jun 5, 2008 10:35:54 PM
 *
 */
public class NewComponentProjectPage extends WizardNewProjectCreationPage {
    private Combo meandreCoreVersion;
    private Combo componentType;
    private Text componentClassName;
    private MeandreComponentProject project;
    private WizardPageListener pageListener;
    String[] supportedVersions = null;
    String[] availableComponentTypes= null;

    public NewComponentProjectPage(String pageName, WizardPageListener pageListener) {
        super(pageName);
        setTitle("New Meandre Component project");
        setDescription("Create a new Meandre Component project.");
        project = new  MeandreComponentProject();
        this.pageListener = pageListener;
        supportedVersions=Activator.getClasspathContainerUtils().getSupportedClasspathContainerNames();
        availableComponentTypes = Activator.getAvailableComponentTypes();
    }

    @Override
    public void createControl(Composite parent) {
        Composite root = new Composite(parent, SWT.NULL);
        root.setLayout(new GridLayout());
        root.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        super.createControl(root);
        createWebControls(root);
        setControl(root);
    }
    
    @Override
    protected boolean validatePage() {
        boolean b = super.validatePage();
        if (!b) {
            return false;
        }
        
        StringBuffer errors = new StringBuffer();
        if (!project.validate(errors)) {
            setErrorMessage(null);
            setMessage(errors.toString());
            return false;            
        }

        setErrorMessage(null);
        setMessage(null);
        return true;
    }
    
    public IRunnableWithProgress getRunnable() {
        if (getProject().getJavaProject() != null) {
            return new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        performFinish(monitor);
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            };
        }
        return null;
    }
    
    private void performFinish(IProgressMonitor monitor) throws CoreException, BadLocationException, TemplateException {
        getProject().install(monitor);
    }
    
    private void createWebControls(Composite parent) {
        Group group = new Group(parent, SWT.NULL);
        group.setText("Meandre Component");
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        createMeandreVersionRootControl(group);
        createComponentClassNameControl(group);
        createComponentTypeControl(group);
        
    }
    
    private void createMeandreVersionRootControl(Composite parent) {
        meandreCoreVersion = createLabelAndCombo(parent, "Meandre Infrastructure Version ", project.getMeandreCoreVersion(), 
        		supportedVersions, new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	if(meandreCoreVersion != null){
            	
                project.setMeandreCoreVersion(meandreCoreVersion.getItem(meandreCoreVersion.getSelectionIndex()));
            	}
                setPageComplete(validatePage());
            }
        });
    }

 
    
    private void createComponentClassNameControl(Composite parent) {
       componentClassName = createLabelAndText(parent, "Component class", 
                project.getComponentClassName(),
                new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	
                project.setComponentClassName(componentClassName.getText());
                setPageComplete(validatePage());
            }
        });
    }
    
    
    private void createComponentTypeControl(Composite parent) {
        componentType = createLabelAndCombo(parent, "Component type", 
                 project.getComponentType(),availableComponentTypes,
                 new ModifyListener() {
             public void modifyText(ModifyEvent e) {
            	 if(componentType!=null)
                 project.setComponentType(componentType.getText());
                 setPageComplete(validatePage());
             }
         });
     }
    
    private Text createLabelAndText(Composite parent, String labelText, String defaultValue,
            ModifyListener listener) {
        Text text = new LabelledText(parent, SWT.SINGLE | SWT.BORDER, labelText, defaultValue).getText();
        text.addModifyListener(listener);
        return text;
    }

    
    private Combo createLabelAndCombo(Composite parent, String labelText, String defaultValue,String[] options,
            ModifyListener listener) {
        Combo text = new LabelledCombo(parent, SWT.SINGLE | SWT.BORDER, labelText, defaultValue,options).getText();
        text.addModifyListener(listener);
        if(text.getItemCount()>0){
        text.select(text.getItemCount()-1);
        }
        return text;
    }
    
    public MeandreComponentProject getProject() {
        return project;
    }
    

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            pageListener.pageEntered(this);
        } else {
            pageListener.pageExited(this);
        }
    }
}
