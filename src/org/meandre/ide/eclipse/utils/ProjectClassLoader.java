/*
 * @(#) ProjectClassLoader.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.utils;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.logger.MeandreLogger;

/**Reads the Eclipse Project and provides the the Project classloader based
 * on drools workflow engine
 *  // added getProjectClassLoader(project, addBinLocation,filterAspectJ) function to
 *  // allow getting library classpaths only.
 * @author Amit Kumar
 * Created on Mar 8, 2008 1:26:15 PM
 * @modified by Amit Kumar Sep 5, 2008 3:22:45 PM
 */
public class ProjectClassLoader {
	
    public static URLClassLoader getProjectClassLoader(IJavaProject project, boolean filterAspectJ) {
        return getProjectClassLoader(project,true,filterAspectJ);
    }

    
    public static URLClassLoader getProjectClassLoader(IJavaProject project, boolean addBinLocation, boolean filterAspectJ) {
    	//MessageConsole mc = Activator.findConsole(Activator.CONSOLE_NAME);
		//mc.activate();
		//MessageConsoleStream out = mc.newMessageStream();
		List<URL> pathElements = getProjectClassPathURLs(project, filterAspectJ);
    	if(addBinLocation){
    		   IPath location = getProjectLocation(project.getProject());
               IPath outputPath;
			try {
				outputPath = location.append(project.getOutputLocation()
				           .removeFirstSegments(1));
				 pathElements.add(outputPath.toFile().toURL());
	       } catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
                
    	}
    	URL urlPaths[] = (URL[]) pathElements.toArray(new URL[pathElements.size()]);
        return new URLClassLoader(urlPaths, Thread.currentThread().getContextClassLoader());
    }
    
    
    
    private static URL getRawLocationURL(IPath simplePath)
            throws MalformedURLException {
        File file = getRawLocationFile(simplePath);
        return file.toURL();
    }

    private static File getRawLocationFile(IPath simplePath) {
        IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(simplePath);
        File file = null;
        if (resource != null) {
            file = ResourcesPlugin.getWorkspace().getRoot().findMember(
                    simplePath).getRawLocation().toFile();
        } else {
            file = simplePath.toFile();
        }
        return file;
    }

    public static List<URL> getProjectClassPathURLs(IJavaProject project, boolean hasAspectJ) {
        List<URL> pathElements = new ArrayList<URL>();
    	MessageConsole mc = Activator.findConsole(Activator.CONSOLE_NAME);
		mc.activate();
		MessageConsoleStream out = mc.newMessageStream();
	
        try {
        	IClasspathEntry[] paths =  project.getRawClasspath();//project.getResolvedClasspath(true);
           
            if (paths != null) {
                
                for ( int i = 0; i < paths.length; i++ ) {
                    IClasspathEntry path = paths[i];
                    MeandreLogger.logInfo("1 Project Classpath: "+path);
                    
                    if (path.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                        URL url = getRawLocationURL(path.getPath());
                        MeandreLogger.logInfo("Project Classpath RAW URL..."+url);
                        MeandreLogger.logInfo("Adding:> " + url.getPath() +" " + path.getPath());
                        pathElements.add(url);
                    }else if(path.getEntryKind() == IClasspathEntry.CPE_CONTAINER){
                    		MeandreLogger.logInfo("path.getPath():"+ path.getPath()+":");

                    		if(!"org.eclipse.jdt.launching.JRE_CONTAINER".equals(path.getPath().segment(0))) {
                    			if(!"org.eclipse.ajdt.core.ASPECTJRT_CONTAINER".equals(path.getPath().segment(0))
                    			|| hasAspectJ		
                    			){
                    			
                        		IClasspathContainer icc =JavaCore.getClasspathContainer(path.getPath(), project);
                        		MeandreLogger.logInfo("Found: " + icc.getClasspathEntries().length + " in the "+path.getPath());
                        		IClasspathEntry[] containerPaths  = icc.getClasspathEntries();
                        		for(int thisContainerClass=0; thisContainerClass < containerPaths.length; thisContainerClass++){
                        			if(containerPaths[thisContainerClass].getEntryKind() == IClasspathEntry.CPE_LIBRARY){
                        				pathElements.add(getRawLocationURL(containerPaths[thisContainerClass].getPath()));	
                        				MeandreLogger.logInfo("Adding: " + containerPaths[thisContainerClass].getPath() +" " + path.getPath());
                        				 out.println("Adding: " + containerPaths[thisContainerClass].getPath() +" " + path.getPath());
                        			}else{
                        				MeandreLogger.logInfo("Error: Don't know how to handle" +containerPaths[thisContainerClass].getPath());
                        				out.println("Error: Don't know how to handle" +containerPaths[thisContainerClass].getPath());
                        			}
                        		}
                    	
                    			}
                    		
                    	}
                    	  MeandreLogger.logInfo("Path Entry: " + path.getEntryKind() +" -- " + path.getContentKind());
                    }
                }
            }
             // also add classpath of required projects
            String[] names = project.getRequiredProjectNames();
            for ( int i = 0; i < names.length; i++ ) {
                String projectName = names[i];
                IProject reqProject = project.getProject().getWorkspace()
                    .getRoot().getProject(projectName);
                if (reqProject != null) {
                    IJavaProject reqJavaProject = JavaCore.create(reqProject);
                    pathElements.addAll(getProjectClassPathURLs(reqJavaProject,hasAspectJ));
                }
            }
        } catch (JavaModelException e) {
        	MeandreLogger.logError(e.getMessage());
        } catch (MalformedURLException e) {
    
        }
        return pathElements;
    }
    
    public static IPath getProjectLocation(IProject project) {
        if (project.getRawLocation() == null) {
            return project.getLocation();
        } else {
            return project.getRawLocation();
        }
    }
    
    /**Return the location of the generated class files
     * 
     * @param project
     * @return
     */

		public static String getProjectOutput(IJavaProject project){
    	try {
			IPath path=  project.getOutputLocation();
			return (path.toOSString());
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    
}