/*
 * @(#) InstallComponentRunnable.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.popup.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.logger.MeandreLogger;
import org.meandre.ide.eclipse.component.preferences.PreferenceConstants;
import org.meandre.ide.eclipse.utils.ComponentJarUtils;
import org.meandre.ide.eclipse.utils.ComponentNatureHandler;
import org.meandre.ide.eclipse.utils.JarUtils;
import org.meandre.ide.eclipse.utils.ProjectClassLoader;
import org.meandre.ide.eclipse.utils.ProjectSourceUtils;
import org.meandre.plugins.bean.ComponentAppletBean;
import org.meandre.server.MeandreEngineServicesConstants;
import org.meandre.tools.components.CreateComponentDescriptor;
import org.meandre.tools.components.FindComponentDep;
import org.meandre.tools.components.InstallComponent;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentNature;
import org.meandre.annotations.ComponentNatures;
import org.meandre.core.repository.CorruptedDescriptionException;



/**This class installs a component to the meandre server
 * @category spaghetti 
 * @author Amit Kumar
 * @modified Amit Kumar -July 12th 2008 Refractoring code
 * @modified Amit Kumar -Sep 5th 2008 Added check to find out if the 
 * component class is in the library classpath
 * 
 * Created on Apr 29, 2008 10:31:50 PM
 *  
 * 
 */
public class InstallComponentRunnable implements IRunnableWithProgress {

	String message = null;
	private ComponentJarUtils componentUtils;
	private ProjectSourceUtils projectSourceUtils;

	private ICompilationUnit unit = null;
	private IJavaProject project=null;
	

	HashMap<String,String> classList = new HashMap<String,String>();
	HashMap<String,String> sourceList = new HashMap<String, String>();


	
	public InstallComponentRunnable( ICompilationUnit unit, IJavaProject project) {
		this.unit= unit;
		this.project = project;
		this.componentUtils = new ComponentJarUtils();
		this.projectSourceUtils = new ProjectSourceUtils();
	}

	@SuppressWarnings("unchecked")
	public void run(IProgressMonitor monitor)
	throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Installing Component",100);
		if(Activator.getServerVersion()==null){
			MeandreLogger.logError("Error: Meandre Server not running?");
			monitor.subTask("Aborting -The Meandre Server is not running.");
			Thread.sleep(5000);
			monitor.done();
			return;
		}

		monitor.subTask("getting preferences");
		String tmpFolder = System.getProperty("java.io.tmpdir");
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String baseFolder = workspace.getRoot().getLocation().toPortableString();
		boolean hasAspectJ =  prefs.getBoolean(PreferenceConstants.P_HAS_ASPECT_J);
		boolean storeSource = prefs.getBoolean(PreferenceConstants.P_INCLUDE_SOURCE);
		String workspacePath = workspace.getRoot().getLocation().toOSString();

		monitor.worked(2);
		Thread.sleep(1000);


		// list of jar files that should be removed
		String filterJar = prefs.getString(PreferenceConstants.P_FILTERJAR);
		MessageConsole mc = Activator.findConsole(Activator.CONSOLE_NAME);
		mc.activate();
		MessageConsoleStream out = mc.newMessageStream();



		ArrayList<String> filterJarList = new ArrayList<String>(10);
		if (filterJar != null) {
			String[] list = filterJar.split(",");
			if (list != null) {
				for (int i = 0; i < list.length; i++) {
					list[i] = list[i].trim();
					if (list[i] != null)
						list[i] = list[i].toLowerCase();
					list[i] = list[i].replace('*', ' ');
					filterJarList.add(list[i].trim());
				}
			}
		}
		if(monitor.isCanceled()){
			return;
		}

		monitor.worked(5);
		monitor.subTask("Getting jars to filter "+ filterJarList.size());
		Thread.sleep(1000);
		String className = null;
		if(unit==null || project==null){
			out.println("[Error] could not retrieve the compilation unit.");
			return;
		}
			
			try {
				className = unit.getTypes()[0].getFullyQualifiedName();
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

				if(className ==null){
					out.println("Error could not retrieve the class for selected file " + unit.toString());
					return;
				}
				out.println("class name is: " + className);

                                /*
				String complianceLevel=	project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
				if(!complianceLevel.equalsIgnoreCase("1.5")){
					monitor.worked(100);
					monitor.subTask("Error: The plugin works with component projects that are JDK 1.5 compliant.\n The current project is compliant with  "+ complianceLevel);
					Thread.sleep(5000);
					return;
				}
                                */
		

				try {

					IType itype=project.findType(className);
					if(itype==null){
						//showMessage("Error could not get the " + className);
						out.println("Error could not get the " + className);
						return;
					}
			
					String projectPath  =null;
					String outputLocation = new File(baseFolder+ ProjectClassLoader.getProjectOutput(project)).getAbsolutePath();
					//System.out.println("Output is here: " + outputLocation);
					File outputLocationDir = new File(outputLocation);
					String componentPath = new File(outputLocationDir,
					className.replace('.', File.separatorChar) + ".class").getAbsolutePath();

					out.println("Component Class Name: " + componentPath);

					String componentEntity = componentPath.substring((outputLocationDir.getAbsolutePath()+File.separator).length());
					componentEntity = componentEntity.replace(".class","");
					componentEntity= componentEntity.replace(File.separatorChar,'.');
					out.println("Component Class File System Path: " +  componentEntity);

					IPath parentFile=null;
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(itype.getPath());
					parentFile =file.getParent().getLocation();
					projectPath =  new File(workspacePath + project.getPath().toOSString()).getAbsolutePath();
					String sourcePath = getComponentSourceLocation(project, project.getPath().toOSString(),componentEntity);


					out.println("Component Source: " +  sourcePath);
					out.println("Project Path:  " +  project.getPath());
					
					// the classloader without the output bin folder
					// we want to check if the component class is already in a jar library
					// if so we want to complain.
					URLClassLoader urlClassloaderWithoutBin=ProjectClassLoader.getProjectClassLoader(project,false,hasAspectJ);
					
					boolean foundClassInJarLibs = Boolean.TRUE;
					foundClassInJarLibs=checkIfClassInLibraryClasspath(className,urlClassloaderWithoutBin);
					
					
					if(foundClassInJarLibs){
						out.println("The list of the jar files is: ");
						for(int i =0; i < urlClassloaderWithoutBin.getURLs().length; i++){
							out.println(urlClassloaderWithoutBin.getURLs()[i].toString());
						}
						out.println("Error: The component class  " + className + " is already in the classpath.");
						out.println("The install component will not work correctly -it will not be able to create" +
								" the jar file with the component classes and dependencies.");
						return;
					}
					
					
					URLClassLoader urlClassloader=ProjectClassLoader.getProjectClassLoader(project,hasAspectJ);
					if(urlClassloader==null){
						out.println("Error: Project Classpath is null...");	
						return;
					}else{
						out.println("Number of libraries in the classpath: " + urlClassloader.getURLs().length);
					}
					
					Class claszz=null;
					try{
						claszz = urlClassloader.loadClass(className);
					}catch(ClassNotFoundException ex){
						out.println("[ERROR] className " + className + "  " + ex.getMessage());
						out.println("Could not locate: "+ className +". Please clean the project and build.");
						for(int i=0; i < urlClassloader.getURLs().length; i++){
							out.println(urlClassloader.getURLs()[i].toString());
						}
						
						
						return;
					}
					String ouputDir=ProjectClassLoader.getProjectOutput(project);

					
					
					
					out.println("[INFO] 12 ouputDir " + ouputDir);
					if(monitor.isCanceled()){
						return;
					}
					monitor.worked(10);
					monitor.subTask("got component project "+ project.getProject().getName());
					Thread.sleep(1000);



					out.println("Got the class..." + claszz.getName());
		
					Component componentAnnotation = (Component) claszz.getAnnotation(Component.class);
					
					System.out.println("Annotatin size--->"+claszz.getAnnotations().length);
					if(monitor.isCanceled()){
						return;
					}

					monitor.worked(15);
					monitor.subTask("trying to get component annotation "+ claszz.getName());
					Thread.sleep(1000);



					if (componentAnnotation == null) {
						out.println("[Error] The class is not a valid component.");
						message = "The class is not a valid component.It either does not have @Component annotation or is not in a compile state";
						monitor.worked(100);
						monitor.subTask("Error: The class is not a valid component.\nIt does not have @Component annotation or is not in a compile state");
						Thread.sleep(2000);
						monitor.done();
						return;
					} 
					
						ComponentNatures componentNatures = (ComponentNatures) claszz.getAnnotation(ComponentNatures.class);
						ComponentNature componentNature =  	(ComponentNature) claszz.getAnnotation(ComponentNature.class);
						ArrayList<URL> alist = (ArrayList<URL>) ProjectClassLoader.getProjectClassPathURLs(project,hasAspectJ);
						
						
						out.println("# of jars in the classpath "+ alist.size());
								

						if(monitor.isCanceled()){
							return;
						}
						monitor.worked(25);
						monitor.subTask("got project classpath jar libraries "+ alist.size());
						Thread.sleep(1000);



						Iterator<URL> it = alist.iterator();
						ArrayList<String> jarList = new ArrayList<String>(20);
						File jarfile = null;
						while (it.hasNext()) {
							jarfile = new File(it.next().getFile());
							if (jarfile.exists()) {

								if (JarUtils.notInFilter(jarfile.getName(),
										filterJarList)) {
									//out.println("Adding: "
									//		+ jarfile.getAbsolutePath()
									//		+ " to the app classpath");
									if (jarfile.isFile()) {
										jarList.add(jarfile.getAbsolutePath());
									}

								}

							}else{
								out.println("Error " + jarfile.getAbsolutePath() + " does not exist");
							}

						}


						ArrayList<String> resourceList = this.projectSourceUtils.getResourceList(componentAnnotation,parentFile);

						boolean componentJarCreated= Boolean.FALSE;
						String fileName = componentAnnotation.name().toLowerCase();
						fileName = fileName.replaceAll("\\s+", "-");
						String componentJar = System.getProperty("java.io.tmpdir")+File.separator+claszz.getName()+"-"+fileName+".jar";
						out.println("creating component jar file: " + componentJar);
						ArrayList<IFile> sourceList = this.projectSourceUtils.getSourceList(outputLocation, classList);
						try {
							componentJarCreated=this.componentUtils.createComponentJar(claszz,
									componentPath,
									outputLocation,
									projectPath,
									sourcePath,
									componentJar, 
									resourceList,
									"2.0.1", 
									storeSource,
									classList, 
									sourceList);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							out.println("[ERROR] "+ e1.getMessage());
							e1.printStackTrace();
							return;
						}
					
						if(monitor.isCanceled()){
							return;
						}


						if(!componentJarCreated){
							message = "Error: Could not create the component jar file " + componentJar+ ".";
							monitor.worked(100);
							monitor.subTask(message);
							Thread.sleep(2000);
							return;
						}

						monitor.worked(35);
						monitor.subTask("created component jar "+ componentJar);
						Thread.sleep(1000);
						
						out.println("Trying to find applets");
						// now create the applets if needed
						ComponentNatureHandler componentNatureHandler = new ComponentNatureHandler(project,out);
						ArrayList<ComponentAppletBean> appletList = new ArrayList<ComponentAppletBean>(3);
						if(componentNatures !=null){
							for( ComponentNature cn:componentNatures.natures()){
									if(!checkIfClassInLibraryClasspath(cn.extClass().getName(),urlClassloaderWithoutBin )){
										ComponentAppletBean cab=	componentNatureHandler.getComponentAppletBean(cn);
										if(cab!=null){
											appletList.add(cab);
										}
									}else{
										out.println("The class " + cn.extClass().getName() + " is in the library path. Please remove the " +
												" offending jar file from the project classpath before continuing");
										return;
									}
								}
							}
					
						if(componentNature!=null){
							ComponentAppletBean cab=	componentNatureHandler.getComponentAppletBean(componentNature);
							if(cab!=null){
								if(!checkIfClassInLibraryClasspath(cab.getMainClass(),urlClassloaderWithoutBin )){
								appletList.add(cab);
								}else{
									out.println("The class " + cab.getMainClass()  + " is in the library path. Please remove the " +
											" offending jar file from the project classpath before continuing");
									return;
								}
							}
						}

						out.println("After finding applets:- Now Finding Dependencies");


						FindComponentDep fcd = new FindComponentDep(jarList);
						fcd.execute(componentPath,componentAnnotation.dependency());
						//ArrayList<String> missingJarFiles = new ArrayList<String>(2);
						//ArrayList<String> uploadJarFiles = new ArrayList<String>(10);
						if(fcd.getDependencyNotFoundList().size() >0){
							String message ="Following external dependencies not found " +
							"in the project classpath. ";
							for(int i=0; i < fcd.getDependencyNotFoundList().size();i++){
								if(i==0){
									message = message +""+ fcd.getDependencyNotFoundList().get(i);
								}else{
									message = message + ","+fcd.getDependencyNotFoundList().get(i);
								}
							}
							message = message + " Press cancel to stop.";
							monitor.worked(45);
							monitor.subTask(message);

							Thread.sleep(10000);
						}

					
						if(monitor.isCanceled()){
							return;
						}

						
						boolean hasApplet = appletList.size()>0;
						boolean hasMissingAppletJar = Boolean.FALSE;
						String appletErrorMessage=" ";
						
						//fcd.reset();
						for(ComponentAppletBean cb:appletList){
							if(cb.hasCreatedJarFile()){
								fcd.execute(cb.getAppletClassPath(),cb.getDependency().toArray(new String[cb.getDependency().size()]));
							}else{
								appletErrorMessage = appletErrorMessage + " " + cb.getMainClass();
								hasMissingAppletJar = Boolean.TRUE;
							}
						}
						
						if(hasApplet && hasMissingAppletJar){
							out.println("Error: Applet jar could not be located: " + appletErrorMessage.trim());
							return;
						}
						


						ArrayList<String> dlist = fcd.getDependencyList();
						// add the component jar file to the list of files to be uploaded
						dlist.add(componentJar);
						// add the applet jar file to the list of the files to be uploaded
						for(ComponentAppletBean cb:appletList){
							if(cb.hasCreatedJarFile()){
								out.println("Applet Jar File: " + cb.getAppletJarName());
								dlist.add(cb.getAppletJarName());
							}
						}
						
						
						System.out.println(dlist.size());
						Iterator<String> it1 = dlist.iterator();
						String jarDependency = null;
						out.println("Found " + dlist.size() + " dependencies.");

						if(monitor.isCanceled()){
							return;
						}
						monitor.worked(65);
						monitor.subTask("found "+ dlist.size() + " jar dependencies.");
						Thread.sleep(1000);


						while (it1.hasNext()) {
							jarDependency = it1.next();
							System.out.println("Found following dependencies: "
									+ jarDependency);
							out.println(jarDependency);
							message = message + " " + jarDependency;
						}

						CreateComponentDescriptor ccd = new CreateComponentDescriptor(
								tmpFolder);
						String descriptorFileName = null;
						try {
							ccd.init(claszz);
							descriptorFileName = ccd.process();
							message = " Desriptor created "
								+ descriptorFileName;
							out.println("Descriptor created: "
									+ descriptorFileName);
							if(monitor.isCanceled()){
								return;
							}
							monitor.worked(75);
							monitor.subTask("created component rdf descriptor in "+ tmpFolder);
							Thread.sleep(1000);

						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							//showMessage(e.getMessage() + " " + className);
							out.println("Class not found " + className +" "+ e.getMessage());
							e.printStackTrace();
						} catch (CorruptedDescriptionException e) {
							// TODO Auto-generated catch block
							//showMessage(e.getMessage() + " " + className);
							out.println("Error in the descriptor: " +  className+ " " +e.getMessage());
							e.printStackTrace();
						}

						if(monitor.isCanceled()){
							return;
						}
						monitor.worked(77);
						monitor.subTask("getting ready to upload components");
						Thread.sleep(1000);

						String url = prefs
						.getString(PreferenceConstants.P_SERVER);
						int port = prefs.getInt(PreferenceConstants.P_PORT);
						if (url == null) {
							out.println("Missing Server location.");
							if(monitor.isCanceled()){
								return;
							}
							monitor.worked(100);
							monitor.subTask("Missing Server location.");
							Thread.sleep(2000);
							return;
						}

						if(!url.startsWith("http://")){
							url = "http://"+url;
						}
						
						if (url.endsWith("/")) {
							url = url.substring(0, url.length() - 1);
						}
					
						if(Activator.getServerVersion().startsWith("1.3")){
							url = url
									+ ":"
									+ port+"/"
									+ MeandreEngineServicesConstants.ADD_REPOSITORY_URL_1_3;
							}else{
								url = url
								+ ":"
								+ port + "/"
								+ MeandreEngineServicesConstants.ADD_REPOSITORY_URL_1_4;
							}
						String port_s = prefs
						.getString(PreferenceConstants.P_PORT);
						String username = prefs
						.getString(PreferenceConstants.P_LOGIN);
						String password = prefs
						.getString(PreferenceConstants.P_PASSWORD);
						boolean embed = prefs
						.getBoolean(PreferenceConstants.P_EMBED);
						boolean overwrite = prefs
						.getBoolean(PreferenceConstants.P_OVERWRITE);

						boolean dump = Boolean.FALSE;

						InstallComponent ic = new InstallComponent(url, port,
								username, password);
						ic.init(dlist);
						String[] jararray = (String[]) dlist
						.toArray(new String[0]);
						System.out.println("Jar array is: " + jararray.length);
						out.println("Uploading " + jararray.length
								+ " files. Please wait...");
						if(monitor.isCanceled()){
							return;
						}
						monitor.worked(80);
						monitor.subTask("Uploading component " + descriptorFileName +" and #" +jararray.length + " jars" );
						Thread.sleep(1000);



						ic.uploadComponent(new File(descriptorFileName),
								overwrite, dump, embed, jararray);
						monitor.worked(100);
						monitor.done();
					

				} catch (JavaModelException e) {
					message = "Java Model Exception " + e.getMessage();
					e.printStackTrace();
				} finally {

					try {
						out.flush();
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Activator.repositoryJob.schedule();
				}
}



	/**Returns the location of the src folder for the component 
	 * 
	 * @param project
	 * @param projectPath
	 * @param componentEntity
	 * @return
	 */
	public String getComponentSourceLocation(IJavaProject project, 
			String projectPath,String componentEntity){
		IType itype = null;
		try {
			itype = project.findType(componentEntity);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(itype.getPath());
		String ss=file.getFullPath().toPortableString();
		ss = ss.replace(".java", "");
		String tmp=ss.replace(projectPath,"");
		String componentPackagePath = componentEntity.replace('.', File.separatorChar);
		tmp=tmp.replace(componentPackagePath, "");
		return tmp;
	}


	

	
/* CANNOT USE THIS METHOD IN THE THREAD
	private void showMessage(String message) {
		Shell shell =PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialog.openInformation(
				shell,
				"Meandre Install Component",
				message);
		shell.dispose();
	}
*/

	@SuppressWarnings("unchecked")
	private boolean checkIfClassInLibraryClasspath(String className,
			URLClassLoader urlClassloaderWithoutBin) {
		boolean foundClassInJarLibs = Boolean.TRUE;
		@SuppressWarnings("unused")
		Class claszz_tmp=null;
		try{
			claszz_tmp= urlClassloaderWithoutBin.loadClass(className);
			foundClassInJarLibs = Boolean.TRUE;
		}catch(ClassNotFoundException ex){
			// this is good -we don't want to find the class here
			foundClassInJarLibs = Boolean.FALSE;
		}
		return foundClassInJarLibs;
	}

}
