/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */

package org.meandre.ide.eclipse.component;

import java.io.File;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.ide.eclipse.component.jobs.GetRepositoryJob;
import org.meandre.ide.eclipse.component.jobs.ServerPingJob;
import org.meandre.ide.eclipse.component.logger.MeandreLogger;
import org.meandre.ide.eclipse.component.preferences.PreferenceConstants;
import org.meandre.ide.eclipse.utils.ClasspathContainerUtils;
import org.meandre.ide.eclipse.utils.FileSystemUtils;
import org.meandre.ide.eclipse.utils.JarObject;
import org.meandre.webapp.proxy.client.MeandrePluginProxy;

import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 * @author Amit Kumar
 *
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.meandre.ide.eclipse.component";
	
	public static final String CONSOLE_NAME ="Meandre Component Console";

	// The shared instance
	private static Activator plugin;
	
	public static final String  PING_JOB = "org.meandre.ide.eclipse.component.jobs.ServerPingJob";
	public static final String  GET_REP_JOB = "org.meandre.ide.eclipse.component.jobs.GetRepositoryJob";
	public static final String JAVA_EDITOR_ID = "org.eclipse.jdt.ui.CompilationUnitEditor";

	public static final String LISP_FORMAT = "clojure";
	public static final String PYTHON_FORMAT = "jython";
	public static final String JAVA_FORMAT = "java/class";

	public static final String MAU_EXTN = "mau";
	public static final String ZZ_EXTN = "zz";
	
	
	private static ServerPingJob serverPingJob=null;
	public static GetRepositoryJob repositoryJob=null;
	
	
	private static MeandrePluginProxy meandreProxy = null;
	private static QueryableRepository repository=null;
	private static FileSystemUtils fileSystemUtil = null;
	private static ClasspathContainerUtils classpathContainerUtil=null;
	private static String meandreServerVersion=null;
	
	public static boolean isConnected = Boolean.FALSE;
	
	static HashMap<String,JarObject> componentInfoHashMap = new HashMap<String,JarObject>(10);
	private static String pluginVersion="";
	private static String pluginName="";
	private IWorkspace workspace;
	
	// location of the source code template
	private static String TEMPLATE_LOC = "server"+ File.separator + "lib"+ File.separator + "_VERSION_"+ File.separator + "templates";
	
	

	
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@SuppressWarnings("unchecked")
	public void start(BundleContext context) throws Exception {
		long start = System.currentTimeMillis();
		super.start(context);
		plugin = this;
		pluginVersion=(String) this.getBundle().getHeaders().get("Bundle-Version");
		pluginName = plugin.getBundle().getSymbolicName();
		workspace = ResourcesPlugin.getWorkspace();
		 
		final Preferences.IPropertyChangeListener 
		propertyChangeListener =
			new Preferences.IPropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent event){
			update();	
			}

		};
		String server=this.getPluginPreferences().getString(PreferenceConstants.P_SERVER);
		int port = this.getPluginPreferences().getInt(PreferenceConstants.P_PORT);
		String username=this.getPluginPreferences().getString(PreferenceConstants.P_LOGIN);
		String password=this.getPluginPreferences().getString(PreferenceConstants.P_PASSWORD);
		String serverUrl=server;
	
		meandreProxy = new MeandrePluginProxy(username,password,serverUrl,port);
		meandreServerVersion=meandreProxy.getServerVersion();
		fileSystemUtil = new FileSystemUtils();
		
		//update();
		MeandreLogger.logInfo("Starting MeandreIde Component: " +  this.getBundle().getHeaders().get("Bundle-Version") + "  version");
	    repositoryJob = new GetRepositoryJob(GET_REP_JOB);
		repositoryJob.setProxy(meandreProxy);
		//repositoryJob.schedule();
		
		this.getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
		//System.out.println("Time in the Activator: " + (System.currentTimeMillis()-start));
	
		
		String serverLibFolder=fileSystemUtil.findDirectory(pluginName+"", "server" + File.separator + "lib");
		
		if(serverLibFolder==null){
			MeandreLogger.logError("The lib directories with server jar files could not be located.");
		}else{
			classpathContainerUtil = new ClasspathContainerUtils(serverLibFolder);		
			classpathContainerUtil.init();
			if(classpathContainerUtil.isValid()){
				MeandreLogger.logInfo("Found: " + classpathContainerUtil.getClasspathContainers().size() + " classpath containers for use.");
			}else{
				MeandreLogger.logError("Error: could not find the any classpath containers for use.");
			}
		}
		
		//System.out.println("  number of libraries: " + serverLibs.list().length);
	}

	protected void update() {
		boolean choice=this.getPluginPreferences().getBoolean(PreferenceConstants.P_PING_SERVER);
		// we are not pinging the server ever
		
		if(choice){
			if(serverPingJob==null){
				serverPingJob = new ServerPingJob(PING_JOB);
				serverPingJob.setProxy(meandreProxy);
				serverPingJob.schedule();
			}
		}else{
			if(serverPingJob !=null){
				serverPingJob.cancel();
				serverPingJob=null;
			}
		}
		
		checkIfServerAndLoginChanged();
	
		
		
	}

	private void checkIfServerAndLoginChanged() {
		boolean updateProxy = Boolean.FALSE;
		String serverUrl=meandreProxy.getServerUrl();
		String login = meandreProxy.getLogin();
		String password =meandreProxy.getPassword();
		
		
		String server_new=this.getPluginPreferences().getString(PreferenceConstants.P_SERVER);
		int port_new = this.getPluginPreferences().getInt(PreferenceConstants.P_PORT);
		String username_new=this.getPluginPreferences().getString(PreferenceConstants.P_LOGIN);
		String password_new=this.getPluginPreferences().getString(PreferenceConstants.P_PASSWORD);
		
		/*
		URL url=null;
		if(!server_new.startsWith("http")){
			server_new = "http://"+server_new;
		}
		try {
			url = new URL(server_new);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String serverUrl_new=server_new;
		if(url.getPort() ==-1){
			serverUrl_new = server_new +":"+port_new+"/";
		}
		*/
		if(!(server_new+":"+port_new+"/").equalsIgnoreCase(serverUrl) || 
				!username_new.equals(login) ||
				!password_new.equals(password)){
				updateProxy= Boolean.TRUE;	
		}
		
		
		if(updateProxy){
			
			meandreProxy.close();
			System.out.println("Updating the server information.");
			MeandreLogger.logError("");
			meandreProxy.update(login, password, server_new, port_new);
			meandreServerVersion=meandreProxy.getServerVersion();
		}
		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		MeandreLogger.logInfo("Stoping...");
		plugin = null;
		super.stop(context);
		
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	 public static MessageConsole findConsole(String name) {
	      ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      
	      for (int i = 0; i < existing.length; i++){
	         if (name.equals(existing[i].getName())){
	        	return (MessageConsole)existing[i];
	         }
	      }
	      //no console found, so create a new one
	      MessageConsole myConsole = new MessageConsole(name, null);
	      
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	   }

	public static synchronized void setRepository(QueryableRepository qr) {
		repository= qr;
	}
	
	public static QueryableRepository getQueryableRepository(){
		return repository;
	}


	public static MeandrePluginProxy getMeandreProxy() {
		return meandreProxy;
	}

	public static void resetComponentInfo() {
		componentInfoHashMap.clear();
	}

	public static void putComponentInfo(String jarLocation,
			String componentJarInfo) {
		//System.out.println("MANIFEST is: "+ componentJarInfo);
		
		if(componentInfoHashMap.get(jarLocation) != null){
			componentInfoHashMap.remove(jarLocation);
		}
		
		
		componentInfoHashMap.put(jarLocation,
				getComponentObject(componentJarInfo));
		
	}

	private static JarObject getComponentObject(String componentJarInfo) {
		if(componentJarInfo==null){
			return null;
		}
		if(componentJarInfo.trim().length()==0){
			return null;
		}
		
		StringTokenizer stok = new StringTokenizer(componentJarInfo,"|");
		HashMap<String,String> hm = new HashMap<String,String>(7);
		while(stok.hasMoreTokens()){
			String[] split = stok.nextToken().split("=");
			if(split.length==2){
			hm.put(split[0], split[1]);
			}
		}
	
		JarObject jarObject = new JarObject();
		boolean isComponent = Boolean.FALSE;
		try{
			isComponent = new Boolean(hm.get("isComponent"));
		}catch(Exception ex){
			
		}
		jarObject.setComponent(isComponent);
		boolean hasSource = Boolean.FALSE;
		try{
			hasSource = new Boolean(hm.get("hasSource"));
		}catch(Exception ex){
			
		}
		
		jarObject.setHasSource(hasSource);
		
		Long lastModified = 0l;
		try{
			lastModified = new Long(hm.get("lastModified"));
		}catch(Exception ex){
			
		}
		jarObject.setLastModified(lastModified);

		jarObject.setMd5(hm.get("md5"));
		jarObject.setName(hm.get("name"));
		
		Long size= 0l;
		try{
			size = new Long(hm.get("size"));
		}catch(Exception ex){
			
		}
		jarObject.setSize(size);
	
		Iterator<String> it = hm.keySet().iterator();
		String key=null;
		while(it.hasNext()){
			key = it.next();
			if(key.startsWith("prop_")){
				jarObject.addProperty(key.substring(5));
			}else if(key.startsWith("input_")){
				jarObject.addInputDataType(key.substring(6), hm.get(key));
			}else if(key.startsWith("output_")){
				jarObject.addOutputDataType(key.substring(7), hm.get(key));
			}
		}
		
		
		String interfaceList = hm.get("interfaceList");
		
		if(interfaceList!=null){
		StringTokenizer stok1 = new StringTokenizer(interfaceList,",");	
		while(stok1.hasMoreTokens()){
			jarObject.addInterface(stok1.nextToken());
		}
		}
		
		
		
		return jarObject;
		
	}

	public static JarObject getComponentInfo(String jarLocation) {
		return componentInfoHashMap.get(jarLocation);
	}

	public static String getMeandreServer() {
		return meandreProxy.getServerUrl();
	}

	public static String getLogin() {
		return meandreProxy.getLogin();
	}

	public static String getPluginVersion() {
		return pluginVersion;
	}

	public static ClasspathContainerUtils getClasspathContainerUtils() {
		return classpathContainerUtil;
	}

	public static String[] getAvailableComponentTypes() {
		return new String[]{"WebUIFragmentCallback","NonWebUIFragmentCallback"};
	}

	public static File getTemplate(String componentType,
			String meandreCoreVersion) {
	 
		String fileName = componentType+".java";
		String version = getVersion(meandreCoreVersion); 
		version = version.replace('.', '_');
		String templateDir = TEMPLATE_LOC.replace("_VERSION_",version);
		String filePath =  templateDir + File.separator + fileName;
		// converts the template location to the path in the plugin
		String realPath=FileSystemUtils.findDirectory(PLUGIN_ID, filePath);
		
		return new File(realPath);
	}

	
	  public static File getRawLocationFile(IPath simplePath) {
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

	
	  
	  public static String getVersion(String name){
		  StringTokenizer stok = new StringTokenizer(name);
		  String version = name;
		  if(stok.countTokens()>0){
			  version = stok.nextToken();
		  }
		  return version;
	  }
	  
	    public IWorkspace getWorkspace() {
	        return workspace;
	    }
	    
	    public IWorkbenchPage getActivePage() {
	        return getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    }

		public static String getServerVersion() {
			return meandreServerVersion;
		}
	    
	
	
}
