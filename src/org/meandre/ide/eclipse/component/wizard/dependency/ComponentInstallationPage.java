/*
 * @(#) ComponentInstallationPage.java @VERSION@
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.meandre.ide.eclipse.component.wizard.dependency;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Preferences;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import org.eclipse.swt.layout.GridLayout;
import org.meandre.annotations.ComponentNature;
import org.meandre.annotations.CreateDefaultComponentDescriptor;
import org.meandre.annotations.DetectDefaultComponentAnnotations;
import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.preferences.PreferenceConstants;
import org.meandre.ide.eclipse.component.wizard.dependency.model.ComponentData;
import org.meandre.ide.eclipse.component.wizard.dependency.model.ComponentListModel;
import org.meandre.ide.eclipse.utils.ComponentJarUtils;
import org.meandre.ide.eclipse.utils.ComponentNatureHandler;
import org.meandre.ide.eclipse.utils.ProjectClassLoader;
import org.meandre.ide.eclipse.utils.ProjectSourceUtils;
import org.meandre.plugins.bean.ComponentAppletBean;
import org.meandre.server.MeandreEngineServicesConstants;
import org.meandre.tools.components.FindComponentDep;
import org.meandre.tools.components.InstallComponent;

/**This wizard page installs the component.
 *
 * @author Amit Kumar
 * Created on Jul 13, 2008 4:07:15 PM
 *
 */
public class ComponentInstallationPage extends WizardPage implements Listener{

	private Table table;
	private ProgressBar progressBar;
	private ProgressBar installprogressBar;
	private Button installComponentButton;
	boolean isJavaProject = Boolean.TRUE;
	private ComponentListModel model;
	String baseFolder;
	Preferences prefs;
	boolean hasAspectJ = Boolean.FALSE;
	boolean storeSource = Boolean.FALSE;
	String outputLocation;
	private Label componentLabel;
	private Label installLabel;
	String tmpFolder = System.getProperty("java.io.tmpdir");
	private ComponentJarUtils componentUtils;
	private ProjectSourceUtils projectSourceUtils;
	IWorkspace workspace;
	String workspacePath;
	boolean stopInstall = false;
	private Shell shell;
	String url;
	String jarInfoUrl;
	int port;
	String username;
	String password;
	boolean embed;
	boolean overwrite;
	boolean packagePath= true;

	protected ComponentInstallationPage() {
		super("Component Installation List");
		setTitle("Meandre Components");
		setDescription("Installing components");
		prefs = Activator.getDefault().getPluginPreferences();
		hasAspectJ =  prefs.getBoolean(PreferenceConstants.P_HAS_ASPECT_J);
		storeSource = prefs.getBoolean(PreferenceConstants.P_INCLUDE_SOURCE);
		this.componentUtils = new ComponentJarUtils();
		this.projectSourceUtils = new ProjectSourceUtils();
		workspace = ResourcesPlugin.getWorkspace();
		baseFolder = workspace.getRoot().getLocation().toPortableString();
		workspacePath = workspace.getRoot().getLocation().toOSString();

		url = prefs.getString(PreferenceConstants.P_SERVER);
		port = prefs.getInt(PreferenceConstants.P_PORT);

		if(!url.startsWith("http://")){
			url = "http://"+url;
		}

		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		
		if(Activator.getServerVersion().startsWith("1.3")){
		jarInfoUrl=url+":"+port+"/"+MeandreEngineServicesConstants.JAR_INFO_URL;
		url = url+ ":"+ port+ "/"+ MeandreEngineServicesConstants.ADD_REPOSITORY_URL_1_3;
		}else{
		jarInfoUrl=url+":"+port+"/"+MeandreEngineServicesConstants.JAR_INFO_URL;
		url = url+ ":"+ port+ "/"+ MeandreEngineServicesConstants.ADD_REPOSITORY_URL_1_4;
		}
		String port_s = prefs.getString(PreferenceConstants.P_PORT);
		username = prefs.getString(PreferenceConstants.P_LOGIN);
		password = prefs.getString(PreferenceConstants.P_PASSWORD);
		embed = prefs.getBoolean(PreferenceConstants.P_EMBED);
		overwrite = prefs.getBoolean(PreferenceConstants.P_OVERWRITE);
		packagePath=prefs.getBoolean(PreferenceConstants.P_CREATE_PACKAGE_PATH);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		shell =PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String[] columnNames = {"Component Class", "Component Name"};
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns=1;
		composite.setLayout(gridLayout);
		Label lab =new Label(composite,SWT.BORDER);
		lab.setText("List of Components for installation");

		table = new Table (composite, SWT.BORDER | SWT.MULTI| SWT.V_SCROLL);
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

		GridData gridData =new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = 300;
		gridData.widthHint = 400;

		table.setLayoutData(gridData);



		// Create a smooth progress bar
		progressBar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.SMOOTH);
		progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		progressBar.setMinimum(0);
		progressBar.setMaximum(30);


		installLabel = new Label(composite,SWT.BORDER);
		installLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));



		installprogressBar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.SMOOTH);
		installprogressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		installprogressBar.setMinimum(0);
		installprogressBar.setMaximum(30);



		installComponentButton = new Button(composite, SWT.PUSH);
		installComponentButton.setText("Install");
		installComponentButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		installComponentButton.addListener(SWT.Selection, this);

		setControl(composite);
		setPageComplete(true);
	}




	public void handleEvent(Event event) {
		if(event.widget== installComponentButton){
			installSelectedComponents();
		}else if(event.widget == table){
			String string = event.detail == SWT.CHECK ? "Checked": "Selected";
			boolean tableItemStatus =           ((TableItem)event.item).getChecked();
			System.out.println(event.item + " " + string +" " +event.stateMask + " " +tableItemStatus);
			String key= ((TableItem)event.item).getText();
			ComponentData cdata=   this.model.getData(key);
			assert cdata != null;
			cdata.setSelected(tableItemStatus);
			this.model.addData(key, cdata);
			String prevText=this.componentLabel.getText();
			int numSelected = this.model.getNumSelected();
			if(numSelected>0){
				prevText = "Found " + this.model.getkeySet().size() +" components. \t" +"Selected " + numSelected + " for installation.";
				this.componentLabel.setText(prevText);
			}
		}
		setPageComplete(isPageComplete());
		getWizard().getContainer().updateButtons();
	}

	public void onEnterPage() {
		ComponentListModel model = getModel();
		Iterator<String> its = model.getkeySet().iterator();
		ComponentData cdata = null;
		while(its.hasNext()){
			cdata = model.getData(its.next());
			if(cdata.isSelected()){
				addDataToTable(cdata);
			}
		}
	}

	private void addDataToTable(ComponentData cdata) {
		TableItem tableItem = new TableItem(table, SWT.NONE);
		tableItem.setText(0,cdata.getClassName());
		tableItem.setText(1, cdata.getName());
	}

	private ComponentListModel getModel() {
		MeandreDependencyComponentWizard wizard = (MeandreDependencyComponentWizard) getWizard();
		return wizard.model;
	}



	public void installSelectedComponents(){
		MessageConsole mc = Activator.findConsole(Activator.CONSOLE_NAME);
		mc.activate();
		MessageConsoleStream out = mc.newMessageStream();
		Iterator<String> it = this.getModel().getkeySet().iterator();
		this.stopInstall = false;
		this.componentLabel.setText("");
		installprogressBar.setSelection(0);
		installprogressBar.setMaximum(10);
		progressBar.setSelection(0);
		progressBar.setMaximum(this.getModel().getNumSelected());
		int countInstalled=0;
		int numSuccess = 0;
		while(it.hasNext()){
			String className = it.next();
			if(this.stopInstall){
				continue;
			}

			ComponentData cdata = this.getModel().getData(className);
			if(cdata.isSelected()){
				if(this.checkIfClassInLibraryClasspath(className, this.getProjectClassLoaderWithoutBin())){
					System.out.println("Cannot install this component: " + className + " as it is present in the project" +
							" library. Please remove the offending jar file from the classpath to install this component ");
				continue;
				}

				Class claszz=null;
				try{
					claszz = getProjectClassLoader().loadClass(className);
				}catch(Exception ex){
					System.out.println("[ERROR] className " + className + "  " + ex.getMessage());
					return;
				}
				this.componentLabel.setText("Installing " + cdata.getName()+"...");
				if(installComponent(cdata.getName(),className,claszz,cdata.getCompilationUnit(),out)){
					cdata.setInstalled(true);
					numSuccess++;
					countInstalled++;
				}else{
					cdata.setInstalled(false);
				}

				progressBar.setSelection(countInstalled);
			}
		}

			try {
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		this.componentLabel.setText("Installed " + numSuccess+ " components. Press finish to see the summary.");

	}



	public boolean installComponent(String name,String className,Class claszz,ICompilationUnit unit,MessageConsoleStream out){
		HashMap<String,String> classList = new HashMap<String,String>();
		try {
			int progress=1;
			IType itype=getJavaProject().findType(className);
			String projectPath  =null;
			String outputLocation = new File(baseFolder+ ProjectClassLoader.getProjectOutput(getJavaProject())).getAbsolutePath();
			File outputLocationDir = new File(outputLocation);
			out.println("[INFO] 5 output loc ");
			String componentPath = new File(outputLocationDir,className.replace('.', File.separatorChar) + ".class").getAbsolutePath();
			out.println("[INFO] 6.2 componentPath " + componentPath);
			String componentEntity = componentPath.substring((outputLocationDir.getAbsolutePath()+File.separator).length());
			out.println("[INFO] 7 componentEntity " +  componentEntity);
			componentEntity = componentEntity.replace(".class","");
			componentEntity= componentEntity.replace(File.separatorChar,'.');
			out.println("[INFO] 8 componentEntity " +  componentEntity);

			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(itype.getPath());
			IPath parentFile =file.getParent().getLocation();
			projectPath =  new File(workspacePath + getJavaProject().getPath().toOSString()).getAbsolutePath();
			String sourcePath = getComponentSourceLocation(getJavaProject(), getJavaProject().getPath().toOSString(),componentEntity);
			String ouputDir=ProjectClassLoader.getProjectOutput(getJavaProject());
			this.installLabel.setText("Creating component resource descriptor: " + name);

			//CreateComponentDescriptor ccd = new CreateComponentDescriptor(tmpFolder);
			CreateDefaultComponentDescriptor ccd = new CreateDefaultComponentDescriptor();
			String descriptorFileName = null;
			try {
				String rdfString=ccd.process(claszz);
				descriptorFileName = writeToFile(rdfString, claszz.getName(), claszz.getSimpleName());
				}catch (CorruptedDescriptionException e) {
				e.printStackTrace();
				}
			this.installprogressBar.setSelection(progress++);



			this.installLabel.setText("Getting component annotations: " + name);
			this.installprogressBar.setSelection(progress++);
			DetectDefaultComponentAnnotations annotationReader=ccd.getAnnotationReader();

			HashMap<String,Object> componentAnnotationHashMap=annotationReader.
			getComponentClassAnnotationMap(claszz, org.meandre.annotations.Component.class);

			//HashMap<String, Object> componentNatureHashMap = annotationReader.getComponentClassAnnotationMap(claszz,
			//		org.meandre.annotations.ComponentNature.class);

			HashMap<String, Object> componentNaturesHashMap = annotationReader.getComponentClassAnnotationMap(claszz,
					org.meandre.annotations.ComponentNatures.class);


			//Component componentAnnotation = (Component) claszz.getAnnotation(Component.class);
			//ComponentNatures componentNatures = (ComponentNatures) claszz.getAnnotation(ComponentNatures.class);
			HashMap<String,ComponentNature> componentNatureHashMap =   annotationReader.getComponentNatureAnnotation(claszz);




			out.println("[INFO] 9 sourcePath " +  sourcePath);
			out.println("[INFO] 10 className " +  className);
			out.println("[INFO] 11 project " + getJavaProject().getPath());
			out.println("[INFO] 12 ouputDir " + ouputDir);
			out.println("Got the class..." + claszz.getName());

			System.out.println("Annotatin size--->"+claszz.getAnnotations().length);

			Object objectResource = componentAnnotationHashMap.get("resources");
			String resources[] =null;

			if(objectResource!=null){
			resources= (String[])componentAnnotationHashMap.get("resources");
			}


			ArrayList<String> resourceList = this.projectSourceUtils.getResourceList(resources,parentFile);

			boolean componentJarCreated= Boolean.FALSE;
			Object objectName = componentAnnotationHashMap.get("name");
			String fileName =null;
			if(objectName!=null){
			fileName = ((String)objectName).toLowerCase();//componentAnnotation.name().toLowerCase();
			fileName = fileName.replaceAll("\\s+", "-");
			}
			if(fileName==null){
				out.println("Error: name is null "+ fileName);
				return false;
			}
			String componentJar = tmpFolder+File.separator+claszz.getName()+"-"+fileName+".jar";
		
			out.println("creating component jar file: " + componentJar);
			out.println("===> " + projectPath + "  " + sourcePath + " " + componentJar + "  "+ resourceList);
			this.installLabel.setText("Getting component source " + name);
			this.installprogressBar.setSelection(progress++);
			this.installLabel.setText("Creating component jar package: " + name);
			this.installprogressBar.setSelection(progress++);
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
						classList);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				out.println("[ERROR] "+ e1.getMessage());
				e1.printStackTrace();

				if(!displayMessageAndAskToContinue(name,className," error creating component jar file. " )){
					this.stopInstall = true;
				}
				return false;
			}

			this.installLabel.setText("Created component jar package: " + name);
			this.installprogressBar.setSelection(progress++);


			out.println("After creating component jar");
			out.println("Trying to find applets");

			this.installLabel.setText("Detecting applets:  " + name);

			// now create the applets if needed
			ComponentNatureHandler componentNatureHandler = new ComponentNatureHandler(this.getJavaProject(), out);
			ArrayList<ComponentAppletBean> appletList = new ArrayList<ComponentAppletBean>(3);
			if(componentNaturesHashMap.size()>0){
				//for( String key: componentNaturesHashMap.keySet()){
					ComponentNature cnList[]= (ComponentNature[])componentNaturesHashMap.get("natures");
					if(cnList!=null){

					for(ComponentNature cn: cnList){
					if(!checkIfClassInLibraryClasspath(cn.extClass().getName(), this.getProjectClassLoaderWithoutBin()))
					{
						ComponentAppletBean cab=	componentNatureHandler.getComponentAppletBean(cn);
						if(cab!=null){
							appletList.add(cab);
							}else{
							out.println("Error: Could not find Component Nature: " + cn.extClass().getName());
						}
					}else{
						out.println("Cannot install this component: " + cn.extClass().getName() + " as it is present in the project" +
						" library. Please remove the offending jar file from the project classpath to install this component ");
					}

					}

					}

				//}
			}

			if(componentNatureHashMap.size()>0){
				for(String key: componentNatureHashMap.keySet()){
				ComponentNature componentNature  = componentNatureHashMap.get(key);
				if(!checkIfClassInLibraryClasspath(componentNature.extClass().getName(),
						this.getProjectClassLoaderWithoutBin())){
				ComponentAppletBean cab=	componentNatureHandler.getComponentAppletBean(componentNature);
				if(cab!=null){
					appletList.add(cab);
				}else{
					out.println("Error: Could not find Component Nature: " + componentNature.extClass().getName());
				}
				}else{
						out.println("Cannot install this component: " + componentNature.extClass().getName() + " as it is present in the project" +
						" library. Please remove the offending jar file from the classpath to install this component ");
					}
				}


			}

			this.installprogressBar.setSelection(progress++);

			if(appletList.size()>0){
				this.installLabel.setText("Found " + appletList.size() + " applets in the component: " + name);
			}
			out.println("After find applets:- Now Finding Dependencies");

			this.installLabel.setText("Looking for component dependencies: " +  name);

			FindComponentDep fcd = new FindComponentDep(getProjectClasspath());
			out.println("Before finding component dep");
			Object dependencyObject  = componentAnnotationHashMap.get("dependency");
			String[] dependency=null;

			if(dependencyObject!=null){
			dependency = (String[])dependencyObject;
			}

			fcd.execute(componentPath,dependency);
			out.println("After finding component dep");
			this.installprogressBar.setSelection(progress++);
			this.installLabel.setText("Found "+   fcd.getDependencyList().size() + " dependencies: " + name);
			this.installprogressBar.setSelection(progress++);



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
				if(!displayMessageAndAskToContinue(name,className, message )){
					this.stopInstall = true;
				}
					return false;


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
				if(!displayMessageAndAskToContinue(name,className," some dependencies could not be located. "+ appletErrorMessage.trim() )){
					this.stopInstall = true;
				}
				out.print("Error: Some dependencies could not be located: " + appletErrorMessage.trim());
				return false;
			}



			ArrayList<String> dlist = fcd.getDependencyList();
			// add the component jar file to the list of files to be uploaded
			dlist.add(componentJar);
			// add the applet jar file to the list of the files to be uploaded
			for(ComponentAppletBean cb:appletList){
				if(cb.hasCreatedJarFile()){
					dlist.add(cb.getAppletJarName());
				}
			}


			this.installLabel.setText("Getting ready to upload "+   dlist.size() + " dependencies: " + name);



			System.out.println(dlist.size());
			Iterator<String> it1 = dlist.iterator();
			String jarDependency = null;
			out.println("Found " + dlist.size() + " dependencies.");


			while (it1.hasNext()) {
				jarDependency = it1.next();
				System.out.println("Found following dependencies: "	+ jarDependency);
				out.println(jarDependency);
			}
			this.installprogressBar.setSelection(progress++);


			boolean dump = Boolean.FALSE;

			this.installLabel.setText("Uploading "+dlist.size()+" component jars: " + name);

			InstallComponent ic = new InstallComponent(url, jarInfoUrl, port,username, password);
			ic.init(dlist);
			String[] jararray = (String[]) dlist
			.toArray(new String[0]);
			System.out.println("Jar array is: " + jararray.length);
			out.println("Uploading " + jararray.length+ " files. Please wait...");
			ic.uploadComponent(new File(descriptorFileName),overwrite, dump, embed, jararray);
			this.installprogressBar.setSelection(progress++);
		} catch (JavaModelException e) {
			if(displayMessageAndAskToContinue(name,className," error uploading jar file. "+ e.getMessage() )){
				this.stopInstall = true;
			}
			out.print(e.getMessage());
			e.printStackTrace();
		}
		return true;

	}



	private boolean displayMessageAndAskToContinue(String name,
			String className, String message) {
			boolean proceed= Boolean.FALSE;
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT | SWT.RETRY | SWT.IGNORE);
	        messageBox.setText("Warning:");
	        messageBox.setMessage("Component " + name +": "+ message + " Do you want to continue to install other components?");
	        int buttonID = messageBox.open();
	        switch(buttonID) {
	          case SWT.RETRY:
	        	  proceed= true;
	        	  break;
	            // saves changes ...
	          case SWT.ABORT:
	            proceed= false;
	            break;
	          case SWT.IGNORE:
	           proceed= true;
	        }
	  return proceed;
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


	public boolean canFlipToNextPage()
	{
		// no next page for this path through the wizard
		return false;
	}

	private IJavaProject getJavaProject() {
		return ((MeandreDependencyComponentWizard)this.getWizard()).getJavaProject();
	}


	private URLClassLoader getProjectClassLoader(){
		return ((MeandreDependencyComponentWizard)this.getWizard()).getProjectClassLoader();

	}

	private URLClassLoader getProjectClassLoaderWithoutBin(){
		return ((MeandreDependencyComponentWizard)this.getWizard()).getProjectClassLoaderWithoutBin();

	}

	private ArrayList<String> getProjectClasspath(){
		return ((MeandreDependencyComponentWizard)this.getWizard()).getProjectClasspath();
	}


	private boolean checkIfClassInLibraryClasspath(String className,
			URLClassLoader urlClassloaderWithoutBin) {
		boolean foundClassInJarLibs = Boolean.TRUE;
		@SuppressWarnings("unused")
		Class<?> claszz_tmp=null;
		try{
			claszz_tmp= urlClassloaderWithoutBin.loadClass(className);
			foundClassInJarLibs = Boolean.TRUE;
		}catch(ClassNotFoundException ex){
			// this is good -we don't want to find the class here
			foundClassInJarLibs = Boolean.FALSE;
		}
		return foundClassInJarLibs;
	}

	/**Write file to the tmp folder
	 *
	 * @param description
	 * @param className
	 * @param simpleName
	 * @return
	 */
	 private String writeToFile(String description, String className, String simpleName) {
		 String dirPath = tmpFolder;
		  if (className.lastIndexOf(".") == -1) {
	            dirPath = tmpFolder;
	        } else {
	        	if(packagePath){
	            dirPath = tmpFolder + File.separator +
	                     className.substring(0, className.lastIndexOf("."));
	        	}else{
	        	dirPath = tmpFolder;
	        	}

	        }
	        dirPath = dirPath.replace('.', File.separatorChar);
	        if (!(new File(dirPath)).exists()) {
	            new File(dirPath).mkdirs();
	        }

	        String absoluteFilePath = dirPath +  File.separator + simpleName + ".rdf";

	        BufferedWriter out = null;
	        final String encoding = "UTF-8";
	        try {
	            out = new BufferedWriter(new OutputStreamWriter(new
	                    FileOutputStream(absoluteFilePath), encoding));
	            out.write(description.trim());
	            out.flush();
	            out.close();
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (out != null) {
	                try {
	                    out.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	        return absoluteFilePath;
	    }


}
