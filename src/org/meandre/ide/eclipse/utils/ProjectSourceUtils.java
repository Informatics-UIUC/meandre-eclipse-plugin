/*
 * @(#) ProjectSourceUtils.java @VERSION@
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.meandre.annotations.Component;

/**Refractored code from InstallComponentRunnable
 * 
 * @author Amit Kumar
 * Created on Jul 12, 2008 2:20:09 AM
 *
 */
public class ProjectSourceUtils {
	
	/**
	 * 
	 * @param appBinPath
	 * @return
	 */ 
	public ArrayList<IFile> getSourceList(String appBinPath,HashMap<String,String> classList) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[]  projects=workspace.getRoot().getProjects();
		HashMap <String, String> sourceListCollector = new HashMap<String,String>();
		ArrayList <IFile> sourceFiles = new ArrayList<IFile>();
		int totalSourceCount = classList.keySet().size();


		for(int i=0; i < projects.length;i++){
			boolean hasJavaNature=false;
			try {
				if(projects[i].isOpen()){
					hasJavaNature=projects[i].hasNature("org.eclipse.jdt.core.javanature");
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// red or black
			if(hasJavaNature){
				// double or nothing
				System.out.println("Is a Java Project: " + projects[i].getName());
				IJavaProject ijp=  JavaCore.create( projects[i]);

				try {
					Iterator<String> it = classList.keySet().iterator();
					if(totalSourceCount!=0){
						// better than gambling -we try the best leaving nothing to chance
						while(it.hasNext()){
							String componentClass= it.next();
							String componentEntity = componentClass.replace(appBinPath+File.separator,"");
							componentEntity = componentEntity.replace(".class","");
							componentEntity= componentEntity.replace(File.separatorChar,'.');

							if(!sourceListCollector.containsKey(componentEntity) || 
									sourceListCollector.get(componentEntity).equals("false")){

								IType itype=ijp.findType(componentEntity);
								if(itype!=null){
									System.out.println(itype.toString());
									IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(itype.getPath());

									// know when to stop
									sourceListCollector.put(componentEntity, "true");
									// deposit
									String ss=file.getFullPath().toOSString();
									sourceFiles.add(file);

									totalSourceCount--;						
									//foundSource = Boolean.TRUE;
								}else{
									System.out.println("Error: itype is null" );
								}	

							}



						}
					}

				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}else{
				System.out.println("Not a Java Project: " + projects[i].getName());
			}


		}
		return sourceFiles;
	}

	

	/**This function retrieves the resource list from the component
	 * 
	 * @param componentAnnotation
	 * @param parentFile
	 * @return
	 */
	public ArrayList<String> getResourceList(
			Component componentAnnotation, IPath parentFile) {
		ArrayList<String> resourceList = new ArrayList<String>(2);
		if(componentAnnotation!=null && componentAnnotation.resources()!=null){
			String[] resources = componentAnnotation.resources();
			for(int i=0; i < resources.length;i++){

				if(resources[i]!=null && resources[i].trim().length()>0){
					File resourceFile = new File(parentFile.toOSString(),resources[i]);
					if(resourceFile.exists()){
						System.out.println("getting the resources");
						try {
							resourceList.add(resourceFile.getCanonicalPath());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						//TODO:  RESOURCE NOT FOUND
					}

				}
			}

		}
		return resourceList;
	}

	
	public ArrayList<String> getResourceList(
			String resources[], IPath parentFile) {
		ArrayList<String> resourceList = new ArrayList<String>();
		if(resources!=null){
			for(int i=0; i < resources.length;i++){
				if(resources[i]!=null && resources[i].trim().length()>0){
					File resourceFile = new File(parentFile.toOSString(),resources[i]);
					if(resourceFile.exists()){
						System.out.println("getting the resources");
						try {
							resourceList.add(resourceFile.getCanonicalPath());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						//TODO:  RESOURCE NOT FOUND
					}

				}
			}

		}
		return resourceList;
	}


}
