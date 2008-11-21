/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */

package org.meandre.ide.eclipse.component.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.meandre.ide.eclipse.classpathcontainer.MeandreProjectContainer;
import org.meandre.ide.eclipse.utils.FolderHelper;

/**
 * 
 * @author Amit Kumar
 * Created on Jun 6, 2008 12:06:14 AM
 *
 */
public class NewComponentJavaProjectPage extends JavaCapabilityConfigurationPage {
    private NewComponentProjectPage mainPage;
    private WizardPageListener pageListener;

    public NewComponentJavaProjectPage(NewComponentProjectPage mainPage, WizardPageListener pageListener) {
        super();
        this.mainPage = mainPage;
        this.pageListener = pageListener;
    }
    
    public void performFinish(IProgressMonitor monitor) throws CoreException, InterruptedException {
        try {
            monitor.beginTask("", 10);
            createProject(new SubProgressMonitor(monitor, 3));
            configureJavaProject(new SubProgressMonitor(monitor, 7));
        } finally {
            monitor.done();
        }
    }

    private void createProject(IProgressMonitor monitor) throws CoreException {
        IProject project = mainPage.getProjectHandle();
        IPath projectLocation = mainPage.getLocationPath();
        IJavaProject javaProject = JavaCore.create(project);
        createProject(project, projectLocation, new SubProgressMonitor(monitor, 1));
        IPath outputLocation = createOutputFolder().getFullPath();
        init(javaProject, outputLocation, getDefaultSourceLocations(), false);
      }

    private IClasspathEntry[] getDefaultSourceLocations() {
        IProject project = mainPage.getProjectHandle();
        List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
        String coreVersion =mainPage.getProject().getMeandreCoreVersion();
        String version = this.getVersion(coreVersion);
        IPath containerPath = MeandreProjectContainer.ID.append(version);
        MeandreProjectContainer container = new  MeandreProjectContainer( containerPath);
        entries.addAll(Arrays.asList(container.getClasspathEntries()));
        entries.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
        IFolder srcFolder = project.getFolder("src/java");
        entries.add(JavaCore.newSourceEntry(srcFolder.getFullPath()));
        entries.add(JavaCore.newSourceEntry(project.getFolder("src/test").getFullPath()));
        return entries.toArray(new IClasspathEntry[entries.size()]);
    }
    
    private String getVersion(String coreVersion) {
    	StringTokenizer stok = new StringTokenizer(coreVersion);
    	String version = coreVersion;
    	if(stok.countTokens()>0){
    	version = stok.nextToken();
    	}
		return version;
	}

	private IFolder createOutputFolder() throws CoreException {
        IProject project = mainPage.getProjectHandle();
        IFolder target = project.getFolder("target");
        IFolder classes = target.getFolder("classes");
        FolderHelper.mkdirs(classes);
        return classes;
    }

    public void initProject() {
        try {
            IRunnableContext context = getContainer();
            context.run(false, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        createProject(monitor);
                    }
                    catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });            
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
        }
    }

    public void removeProject() {
        IProject project = mainPage.getProjectHandle();
        try {
            project.delete(true, true, new NullProgressMonitor());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
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
