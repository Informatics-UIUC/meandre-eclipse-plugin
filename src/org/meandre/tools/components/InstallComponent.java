/*
 * @(#) InstallComponent.java @VERSION@
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.tools.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**This program takes the rdf descriptor and  list of jar file dependencies
 * and uploads them to the meandre repository
 * <pre>
 * Usage:
 * 		InstallComponent icomponent = new InstallComponent(url,port ,username, password);
		icomponent.init(libraryResourcesDir);
		boolean bool =icomponent.uploadComponent(componentRdfFile, jarDependencies);
  </pre>
 *
 * @author Amit Kumar
 * Created on Dec 28, 2007 4:06:04 PM
 * @updated April 15th : support for components that do not have any jar Files, 
 * just rdf.
 */
public class InstallComponent {

	// directory where the jar files are stored.
	static String libraryResourcesDir;
	// hashmap where the jarfile names are mapped to the File.
	public static HashMap<String, File> appJarList = new HashMap<String, File>(100);

	private static String username;
	private static String password;
	private static String meandreUploadUrl="http://127.0.0.1:1714/services/repository/add.json";
	private static int port=1714;
	HttpClient client;
	

	public InstallComponent(String url, int port,String username, String password){
		InstallComponent.username = username;
		InstallComponent.password = password;
		InstallComponent.meandreUploadUrl = url;
		InstallComponent.port = port;
		client= new HttpClient();
		client.getState().setCredentials(new AuthScope(null, port, null),
		new UsernamePasswordCredentials(InstallComponent.username, InstallComponent.password));
	}

	public void init (String libraryResourcesDir){
		InstallComponent.libraryResourcesDir = libraryResourcesDir;
		makeJarHashMap(InstallComponent.libraryResourcesDir);
	}

	/**Creates the  appJarList
	 * 
	 * @param jarFileList
	 */
	public void init(ArrayList<String> jarFileList){
		Iterator<String> it = jarFileList.iterator();
		String thisJarFile=null;
		while(it.hasNext()){
			thisJarFile = it.next();
			appJarList.put(thisJarFile, new File(thisJarFile));
		}
	}
	
	
	public void reset(){
		appJarList.clear();
	}


	public static void main(String[] args) throws Exception {


		String username = "admin";
		String password = "admin";
		String url = "http://127.0.0.1:1714/services/repository/add.rdf";
		int port = 1714;
		String componentRdf = "descriptors/components/org/monkproject/meandre/components/analytics/MorphAdornerPOSTagger.rdf";
		String[] jarDependencies = {"morphadorner-0.1-hacked.jar","xml-apis-2.0.2.jar","log4j-1.2.14.jar","jdom-1.0.jar","xercesImpl-2.8.1.jar"};
		String libraryResourcesDir = "lib";
		boolean overwrite = Boolean.TRUE;
		boolean dump = Boolean.FALSE;
		boolean embed = Boolean.FALSE;

		//String componentRdf = args[0];
		//String libraryResourcesDir = args[1];
		//String jarDependenciesList = args[2];
		
		if(!(new File(libraryResourcesDir)).exists() || !(new File(libraryResourcesDir)).isDirectory() ){
			System.out.println("Error: The jar directory: " + libraryResourcesDir + " does not exist.");
		}
		File componentRdfFile = new File(componentRdf);
		if(!componentRdfFile.exists()){
			System.out.println("Error: Component RDF file does not exist.");
			System.exit(0);
		}


		//String[] jarDependencies = jarDependenciesList.split(",");


		if(jarDependencies.length==0){
			System.out.println("[Error] The number of jar dependencies is zero. ");
			System.exit(0);
		}

		InstallComponent icomponent = new InstallComponent(url, port,username, password);
		icomponent.init(libraryResourcesDir);
		boolean bool =icomponent.uploadComponent(componentRdfFile, overwrite,dump,embed,jarDependencies);
		System.out.println("Uploaded: " +bool);
		
		bool =icomponent.uploadComponent(componentRdfFile, overwrite,dump,embed,jarDependencies);
		System.out.println("Uploaded: " +bool);
		
		icomponent.reset();
	}


	/**Upload the component
	 *
	 * @param componentRdfFile
	 * @param jarDependencies
	 * @return
	 */
	public boolean uploadComponent(File componentRdfFile,boolean overwrite ,
		   boolean dump,boolean embed,String[] jarDependencies){
		PostMethod filePost;
		filePost= new PostMethod(meandreUploadUrl);
		int arrayLen=0;
		int numJars=0;
		
		if(jarDependencies==null){
		arrayLen  = 4;
		numJars=0;
		}else{
		arrayLen = jarDependencies.length + 4;
		numJars=jarDependencies.length;
		}
		
		Part[] parts = new Part[arrayLen];
		try {
			parts[0] = new StringPart("embed", embed+"");
			parts[1] = new StringPart("overwrite", overwrite+"");
			parts[2] = new StringPart("dump", dump+"");
			parts[3] = new FilePart("repository", componentRdfFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File jarFile = null;
		int count = 4;
		for (int i = 0; i < numJars; i++) {
			jarFile = appJarList.get(jarDependencies[i]);
			if (jarFile != null) {
				try {
					parts[count] = new FilePart("context", jarFile);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				count++;
			} else {
				System.out.println("Error: Could not get > "+ jarDependencies[i]);
						
			}
		}
		System.out.println("Component: " + componentRdfFile.getName() + " Number of dependencies: " + numJars);
		System.out.println("FP: "+ parts.length);
			
	
		filePost.setRequestEntity( new MultipartRequestEntity(parts, filePost.getParams() ) );
		//filePost.setQueryString("overwrite=true");
		// makes it faster..
		//filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE,true);
		filePost.setDoAuthentication(true);
		
		int status = 401;
		try {
			status = client.executeMethod(filePost);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			filePost.releaseConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			filePost.releaseConnection();
		}

		System.out.println("Status: "+ status + " " + filePost.isAborted());
		
		try {
			Reader reader = new InputStreamReader(
					filePost.getResponseBodyAsStream(),filePost.getResponseCharSet());
			StringBuffer componentBuffer = new StringBuffer(32768);
			char[] buffer = new char[32768];
			int charsRead;
			while ((charsRead =reader.read(buffer)) != -1) {
				componentBuffer.append(buffer, 0, charsRead);
			}
			componentBuffer.trimToSize(); // trim the backing buffer to the true size so memory isn't wasted
			System.out.println(" Component BUFFER IS : " + componentBuffer.toString());

			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
		filePost.releaseConnection();
		}
		
		
		return !filePost.isAborted();
	}


	/**
	 * Create an ArrayList of files
	 *
	 * @param libFolder
	 */
	private static void makeJarHashMap(String libFolder) {
		File file = new File(libFolder);
		File[] listFiles = file.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			if (listFiles[i].getAbsolutePath().endsWith(".jar")) {
				appJarList.put(listFiles[i].getName(), listFiles[i]);
			} else if (listFiles[i].isDirectory()) {
				makeJarHashMap(listFiles[i].getAbsolutePath());
			}
		}
	}
	
	


}
