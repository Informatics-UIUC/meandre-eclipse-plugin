/*
 * @(#) MeandreComponentProject.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.project;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.TemplateException;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.wizard.MeandreComponentProjectStatus;
import org.meandre.ide.eclipse.utils.FileHelper;
import org.meandre.ide.eclipse.utils.FolderHelper;


/**
 * 
 * @author Amit Kumar
 * Created on Jun 6, 2008 12:06:48 AM
 *
 */
public class MeandreComponentProject {
    private String componentClassName = "com.example.MyComponent";
    private String componentType = "NonWebUIFragmentCallback";
    private String meandreCoreVersion = "1.3.1 (1.3.1v)";
    private IJavaProject javaProject;


    public void setComponentClassName(String componentClassName) {
        this.componentClassName = componentClassName;
    }

    public void setMeandreCoreVersion(String version) {
        this.meandreCoreVersion = version;
    }

    public boolean validate(StringBuffer errors) {
     
        if (meandreCoreVersion == null || meandreCoreVersion.trim().equals("")) {
            errors.append("Meandre Version must be given.");
            return false;
        }
        
        if (componentClassName == null || componentClassName.trim().equals("")) {
            errors.append("component class name must be given.");
            return false;
        }
        
        return true;
    }

    public String getComponentClassName() {
        return componentClassName;
    }

    public String getMeandreCoreVersion() {
        return meandreCoreVersion;
    }


    
    public MeandreComponentProjectStatus install(IProgressMonitor monitor) throws BadLocationException, 
        TemplateException, CoreException {
        
        monitor.beginTask("New Meandre Component Project", 4);
        
        
        monitor.worked(1);
        
        createcomponentClass();
        monitor.worked(1);
        
        
        associate();
        monitor.worked(1);
        
        return new MeandreComponentProjectStatus(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, "", null);
    }
    
    private void associate() {
      //  NatureAssociation association = new NatureAssociation();
       // association.doAssociate(javaProject.getProject());
    }

   

    
    private void createcomponentClass() throws BadLocationException, TemplateException, CoreException {
        IFolder root = getComponentClassPackageFolder();
        File srcFile= Activator.getTemplate(getComponentType(),getMeandreCoreVersion());
     	File destFileDir =Activator.getRawLocationFile(root.getFullPath());
        File destFile =  new File(destFileDir,getComponentClassShortName()+".java");
        HashMap<String,String> hm = new HashMap<String,String>(2);
        hm.put("_CLASS_", this.getComponentClassShortName());
        hm.put("_PACKAGE_",this.getComponentClassPackageName());
        
        System.out.println(destFile.toURI());
        try {
        	FileHelper.copyAndFilterFile(srcFile,destFile,hm);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//IPath path = root.getFullPath().append(getComponentClassShortName()+".java");
		//IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		//IDE.openEditor(Activator.getDefault().getActivePage(), file);
    }

    public IJavaProject getJavaProject() {
        return javaProject;
    }

    public void setJavaProject(IJavaProject javaProject) {
        this.javaProject = javaProject;
    }


    
    public IFolder getComponentClassPackageFolder() throws CoreException {
        IFolder folder = findSrcFolder().getFolder(
                getComponentClassPackageName().replace('.', '/'));
        FolderHelper.mkdirs(folder);
        return folder;
    }

    private IFolder findSrcFolder() throws JavaModelException {
        for (IClasspathEntry entry : javaProject.getRawClasspath()) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                return javaProject.getProject().getFolder(
                        entry.getPath().makeRelative().removeFirstSegments(1));
            }
        }
        
        return javaProject.getProject().getFolder("");
    }

    public String getComponentClassPackageName() {
        int index = componentClassName.lastIndexOf('.');
        if (index != -1) {
            return componentClassName.substring(0, index);
        }
        return "";
    }

    public String getComponentClassShortName() {
        int index = componentClassName.lastIndexOf('.');
        if (index != -1) {
            return componentClassName.substring(index + 1);
        }
        return componentClassName;
    }

	/**
	 * @return the componentType
	 */
	public String getComponentType() {
		return componentType;
	}

	/**
	 * @param componentType the componentType to set
	 */
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}


	
}
