/*
 * @(#) ComponentNatureHandler.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

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
import org.eclipse.ui.console.MessageConsoleStream;
import org.meandre.annotations.ComponentNature;
import org.meandre.plugins.bean.ComponentAppletBean;
import org.meandre.tools.components.MeandreComponentDepHandler;

import com.tonicsystems.jarjar.DepFind;
import com.tonicsystems.jarjar.DepHandler;

/**This class is used to handle different natures of components.
 * For Applet nature requires that the applet jar file and the dependency/resources 
 * be injected in the Applet.
 *  A nature that we might support in future would be dependency injection 
 *  framework based components.
 * 
 * @author Amit Kumar
 * Created on Jun 10, 2008 6:35:27 PM
 *
 */
public class ComponentNatureHandler {

	HashMap<String,String> classList = new HashMap<String,String>();
	HashMap<String,String> sourceList = new HashMap<String, String>();
	File outputLocationDir;
	IJavaProject javaProject;
	String workspacePath;
	String projectPath;
	MessageConsoleStream out;
	
	// return a jar file with applet code and the resources applet needs
	
	public ComponentNatureHandler(IJavaProject project, MessageConsoleStream out) throws JavaModelException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String baseFolder = workspace.getRoot().getLocation().toPortableString();
		String outputLocation = new File(baseFolder+ ProjectClassLoader.getProjectOutput(project)).getAbsolutePath();
		this.workspacePath = 	workspace.getRoot().getLocation().toOSString();
		this.outputLocationDir = new File(outputLocation);
		this.javaProject = project;
		this.projectPath = new File(workspacePath + project.getPath().toOSString()).getAbsolutePath();
		this.out = out;
	}


	/** Returns the jar file location of the applet file
	 * @throws JavaModelException 
	 * 
	 */
	public ComponentAppletBean getComponentAppletBean(ComponentNature cn) throws JavaModelException{
		ComponentAppletBean cab = new ComponentAppletBean();
		Class appletClass = cn.extClass();
		String className = appletClass.getName(); 
		String appletPath = new File(outputLocationDir,
				className.replace('.', File.separatorChar) + ".class").getAbsolutePath();
		String classEntity = className.replace(".class", "");
		IType itype=javaProject.findType(classEntity);
		if(itype==null){
			//showMessage("Error could not get the " +appletClass.getName());
			System.out.println("Error could not get the " + appletClass.getName());
			return null;
		}
		IPath parentFile=null;
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(itype.getPath());
		parentFile =file.getParent().getLocation();
	
		ArrayList<String> resourceList = getResourceList(cn,parentFile,cab);
		
		String  appletClassPath = new File(outputLocationDir,
		className.replace('.', File.separatorChar) + ".class").getAbsolutePath();
		
		String sourcePath = getComponentSourceLocation(itype);


		
			String fileName = classEntity.toLowerCase();
			fileName = fileName.replaceAll("\\s+", "-");
			String appletJarFileName = System.getProperty("java.io.tmpdir")+File.separator+fileName+".jar";

		boolean createdJar = Boolean.FALSE;
		try {
			createdJar=createAppletJar(appletClass, 
					 appletClassPath, this.outputLocationDir.getAbsolutePath(),
						this.projectPath,sourcePath,
						appletJarFileName,resourceList,
						Boolean.TRUE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for(String dep:cn.dependency()){
			cab.addDependency(dep);
		}
		
		cab.setAppletClassPath(appletClassPath);
		cab.setMainClass(className);
		cab.setAppletJarName(appletJarFileName);
		cab.createdJarFile(createdJar);
		
		return cab;
		
	}
	
	
	

	/**Finds the component dependencies transitively
	 * 
	 * @param cp1
	 * @param appClasspath
	 * @param handler
	 * @param start
	 */
	private void findComponentDep(String cp1, String appClasspath,
			MeandreComponentDepHandler handler, boolean start) {
		if(start){
			classList.clear();
		}

		if(!classList.containsKey(cp1)){
			classList.put(cp1, "true");
		}else if(classList.get(cp1).equals("false")){
			classList.put(cp1, "true");
		}
		try {
			new DepFind().run(cp1, appClasspath, handler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//	System.out.println("Handler Size: "+handler.getJarList().size());

		if(!start && handler.getJarList().size()==0){
			return;
		}

		java.util.Iterator<String> it = handler.getJarList().iterator();
		String key=null;


		while(it.hasNext()){
			key = new File(appClasspath+File.separator+ it.next()+".class").getAbsolutePath();
			if(!classList.containsKey(key)){
				classList.put(key, "false");
				findComponentDep(key,appClasspath,new MeandreComponentDepHandler(DepHandler.LEVEL_CLASS, key), Boolean.FALSE);
			}
		}


	}
	
	/**
	 * This function creates a component jar file; that has all the classes the
	 * component depends upon
	 * @param claszz 
	 * 
	 * @param cp1
	 * @param appClasspath
	 * @param jarFile
	 * @return
	 * @throws IOException
	 */
	private boolean createAppletJar(Class claszz, 
			String appletClass, String appBinPath,
			String projectPath,String sourcePath,
			String outJarFileName,ArrayList<String> resourceList,
			boolean storeSource) throws IOException {
		File outFile = new File(outJarFileName);
		MeandreComponentDepHandler handler = new MeandreComponentDepHandler(
				DepHandler.LEVEL_CLASS, appletClass);
		findComponentDep(appletClass, appBinPath,handler, Boolean.TRUE);
		ArrayList<IFile> sourceList = getSourceList(appBinPath);

		if (classList.keySet().size() == 0) {
			return Boolean.FALSE;
		}

		

		

		BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(outFile));
		Manifest manifest = new Manifest();
		Attributes manifestAttr = manifest.getMainAttributes();
		manifestAttr.putValue("Manifest-Version", "1.0");
		manifestAttr
		.putValue("Meandre-Applet", "1.0.0");
		manifestAttr.putValue("isComponent", "false");
		manifestAttr.putValue("nature", "applet");
		manifestAttr.putValue("hasSource", storeSource+"");
	
	

		JarOutputStream jo = new JarOutputStream(bo, manifest);
		System.out.println("Number of class dependencies: "	+ classList.keySet().size());
		System.out.println("Source Code entries: " + sourceList.size());

		java.util.Iterator<String> it = classList.keySet().iterator();
		Iterator<IFile> itSource = sourceList.iterator();




		BufferedInputStream bi = null;
		JarEntry je = null;
		// store the class files
		while (it.hasNext()) {
			File file = null;
			String fname = null;
			String className = null;
			className = it.next();
			fname = className;
			bi = new BufferedInputStream(new FileInputStream(fname));
			String jarEntry = className.replace(appBinPath+File.separator, "");
			jarEntry = jarEntry.replace(File.separatorChar, '/');


			je = new JarEntry(jarEntry);
			jo.putNextEntry(je);

			byte[] buf = new byte[1024];
			int anz;

			while ((anz = bi.read(buf)) != -1) {
				jo.write(buf, 0, anz);
			}

		}

		if(storeSource){
			// store the java files
			IFile sourceFile = null;
			while(itSource.hasNext()){
				sourceFile = itSource.next();
				String fname=sourceFile.getRawLocation().makeAbsolute().toOSString();
				bi = new BufferedInputStream(new FileInputStream(fname));
				String jarEntry = sourceFile.getProjectRelativePath().toOSString(); 
				jarEntry = jarEntry.replace(File.separatorChar, '/');
				je = new JarEntry(jarEntry);
				jo.putNextEntry(je);

				byte[] buf = new byte[1024];
				int anz;

				while ((anz = bi.read(buf)) != -1) {
					jo.write(buf, 0, anz);
				}
			}
			it= resourceList.iterator();
			String resourceFile = null;
			while(it.hasNext()){
				resourceFile = it.next();
				bi = new BufferedInputStream(new FileInputStream(resourceFile));
				String jarEntry =resourceFile.replace(projectPath+File.separator, "");
				jarEntry=jarEntry.replace(File.separatorChar, '/');
				je = new JarEntry(jarEntry);
				jo.putNextEntry(je);

				byte[] buf = new byte[1024];
				int anz;

				while ((anz = bi.read(buf)) != -1) {
					jo.write(buf, 0, anz);
				}
			}
		}


		// we got source now resources are what we want to package
		// we package them twice in the src and in the root

		it= resourceList.iterator();
		String resourceFile = null;
		while(it.hasNext()){
			resourceFile = it.next();
			bi = new BufferedInputStream(new FileInputStream(resourceFile));
			String jarEntry =resourceFile.replace(projectPath +sourcePath, "");
			je = new JarEntry(jarEntry);
			jo.putNextEntry(je);

			byte[] buf = new byte[1024];
			int anz;

			while ((anz = bi.read(buf)) != -1) {
				jo.write(buf, 0, anz);
			}
		}


		bi.close();
		jo.close();
		bo.close();
		return Boolean.TRUE;
	}
	
	/**
	 * 
	 * @param appBinPath
	 * @return
	 */ 
	private ArrayList<IFile> getSourceList(String appBinPath) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[]  projects=workspace.getRoot().getProjects();
		HashMap <String, String> sourceListCollector = new HashMap<String,String>();
		ArrayList <IFile> sourceFiles = new ArrayList<IFile>();
		int totalSourceCount = this.classList.keySet().size();


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
				IJavaProject ijp=  JavaCore.create( projects[i]);

				try {
					Iterator<String> it = this.classList.keySet().iterator();
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
									sourceListCollector.put(componentEntity, "true");
									//String ss=file.getFullPath().toOSString();
									if(!sourceFiles.contains(file)){
									sourceFiles.add(file);
									totalSourceCount--;				
									}
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

			}


		}
		return sourceFiles;
	}

	/**This function retrieves the resource list from the component
	 * 
	 * @param componentAnnotation
	 * @param parentFile (in this case it is the applet class)
	 * @param cab 
	 * @return
	 */
	private ArrayList<String> getResourceList(
		ComponentNature componentNatureAnnotation, IPath parentFile, ComponentAppletBean cab) {
		ArrayList<String> resourceList = new ArrayList<String>(2);
		if(componentNatureAnnotation!=null && componentNatureAnnotation.resources()!=null){
			String[] resources = componentNatureAnnotation.resources();
			for(int i=0; i < resources.length;i++){

				if(resources[i]!=null && resources[i].trim().length()>0){
					File resourceFile = new File(parentFile.toOSString(),resources[i]);
					if(resourceFile.exists()){
						System.out.println("getting the resources");
						try {
							if(!cab.hasResource(resources[i])){
							cab.addResources(resources[i]);
							resourceList.add(resourceFile.getCanonicalPath());
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
							System.out.println("[Error] Ignoring Applet Resource: " + resourceFile.getAbsolutePath() + " does not exist.");
					}

				}
			}

			return resourceList;
		}
		return null;
	}

	
	/**Returns the location of the src folder for the component 
	 * 
	 * @param project
	 * @param projectPath
	 * @param componentEntity
	 * @return
	 */
	private String getComponentSourceLocation(IType itype){
		String relativeProjectPath=javaProject.getPath().toOSString();
		String componentEntity=itype.getElementName();
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(itype.getPath());
		String ss=file.getFullPath().toPortableString();
		ss = ss.replace(".java", "");
		String tmp=ss.replace(relativeProjectPath,"");
		String componentPackagePath = componentEntity.replace('.', File.separatorChar);
		tmp=tmp.replace(componentPackagePath, "");
		return tmp;
	}

}
