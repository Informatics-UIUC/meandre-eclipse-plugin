/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.wizard;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.meandre.ide.eclipse.IImageKeys;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.project.MeandreComponentProject;


/**
 * 
 * @author Amit Kumar
 * Created on Jun 6, 2008 12:06:28 AM
 *
 */
public class NewComponentProjectWizard extends BasicNewResourceWizard implements WizardPageListener {
    private NewComponentProjectPage mainPage;
    private NewComponentJavaProjectPage javaProjectPage;
    private IWizardPage nextPage;

    public NewComponentProjectWizard() {
        setNeedsProgressMonitor(true);
    }
    
    @Override
    
    public void addPages() {
    	URL urlProject = null;
		try {
			urlProject = new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.COMPONENT_PROJECT);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	ImageDescriptor projectImage = ImageDescriptor.createFromURL(urlProject);
    	mainPage = createMainPage(projectImage);
        javaProjectPage = createJavaProjectPage(projectImage);
        addPage(mainPage);
        addPage(javaProjectPage);
    }

    private NewComponentJavaProjectPage createJavaProjectPage(ImageDescriptor image) {
        NewComponentJavaProjectPage page = new NewComponentJavaProjectPage(mainPage, this);
        page.setImageDescriptor(image);
        return page;
    }

    private NewComponentProjectPage createMainPage(ImageDescriptor image) {
        NewComponentProjectPage page = new NewComponentProjectPage("New Meandre Component project", this);
        page.setImageDescriptor(image);
        return page;
    }

    public void pageEntered(IWizardPage page) {
        nextPage = page;
    }

    public void pageExited(IWizardPage page) {
        if (page.equals(mainPage) && nextPage.equals(javaProjectPage)) {
            javaProjectPage.initProject();
        } else if (page.equals(javaProjectPage) && nextPage.equals(mainPage)) {
            javaProjectPage.removeProject();
        }
    }
    
    @Override
    public boolean performFinish() {
        MeandreComponentProject wicketProject = mainPage.getProject();
        try {
            javaProjectPage.performFinish(new NullProgressMonitor());
        } catch (InterruptedException e) {
            return false;
        } catch (CoreException e) {
            e.printStackTrace();
            return false;
        }
        
        IJavaProject javaProject = javaProjectPage.getJavaProject();
        wicketProject.setJavaProject(javaProject);

        if (!performFinish(mainPage.getRunnable())) {
            return false;
        }

        return true;
    }

    private boolean performFinish(IRunnableWithProgress runnable) {
        IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
        try {
            getContainer().run(false, true, op);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }
}
