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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.meandre.client.TransmissionException;

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
	private static String meandreJarInfoUrl="http://127.0.0.1:1714/";
	private static int port=1714;
	HttpClient client;
	

	public InstallComponent(String url, String jarInfoUrl,int port,String username, String password){
		InstallComponent.username = username;
		InstallComponent.password = password;
		InstallComponent.meandreUploadUrl = url;
		InstallComponent.meandreJarInfoUrl = jarInfoUrl;
		//String  = "plugins/jar/" + jarFile + "/info";
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

		InstallComponent icomponent = new InstallComponent(url,"http://127.0.0.1:1714/plugins/", port,username, password);
		icomponent.init(libraryResourcesDir);
		boolean bool =icomponent.uploadComponent(componentRdfFile, overwrite,dump,embed,false,jarDependencies);
		System.out.println("Uploaded: " +bool);
		
		bool =icomponent.uploadComponent(componentRdfFile, overwrite,dump,embed,false,jarDependencies);
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
		   boolean dump,boolean embed,boolean uploadOnlyChangedJar,String[] jarDependencies){
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
		String infoUrl =this.meandreJarInfoUrl;
		String tmpFolder = System.getProperty("java.io.tmpdir");
		
		for (int i = 0; i < numJars; i++) {
			jarFile = appJarList.get(jarDependencies[i]);
			String localMD5 = getMD5(jarFile);
			infoUrl = this.meandreJarInfoUrl+"/"+localMD5+".md5"+"/info";
			// check if the jar file is already present
			String serverSideFileName=null;
			
			if(uploadOnlyChangedJar){
				serverSideFileName=getServerSideJarFile(infoUrl,jarFile.getName());
			}
			if (jarFile != null) {
				if(serverSideFileName!=null){
				//System.out.println("NOW creating and SENDING 0 BYTE FILE and SENDING IT");
				 // create a file with 0 bytes and send that instead of the new file	
				File file = new File(tmpFolder+File.separator+serverSideFileName);
				try {
					file.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					parts[count] = new FilePart("context",file);
				} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				
				}else{
					try {
						parts[count] = new FilePart("context", jarFile);
					} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
	
	/**Returns the server filename for the jar file present on the server
	 * 
	 * @param jarFile
	 * @return
	 */
	private String getServerSideJarFile(String sRestCommand,String jarFile) {
		String jarInfo=null;
		String serverfileName=null;
		try {
			Set<NameValuePair> nvps = new HashSet<NameValuePair>();
			jarInfo = executeGetRequestString(sRestCommand, nvps);
		} catch (TransmissionException e) {
			System.out.println(e.getMessage());
		}
		if(jarInfo!=null){
			StringTokenizer stok = new StringTokenizer(jarInfo,"|");
			HashMap<String,String> hm = new HashMap<String,String>(7);
			while(stok.hasMoreTokens()){
				String[] split = stok.nextToken().split("=");
				if(split.length==2){
				hm.put(split[0], split[1]);
				}
			}
			serverfileName= hm.get("name");
		}
		
		if(serverfileName!=null){
			//System.out.println("SERVER FILE NAME IS: " +serverfileName);
			return serverfileName;
		}
		
		return null;
	}

	private String executeGetRequestString(String restCommand,
			Set<NameValuePair> nvps) throws TransmissionException {
		   try {
		        byte[] baRetrieved = executeGetRequestBytes(restCommand, nvps);
		        String sRetrieved = new String(baRetrieved, "UTF-8");
		        return sRetrieved;
		    } catch (UnsupportedEncodingException e) {
		        throw new TransmissionException(
		                "Server response couldn't be converted to UTF-8 text", e);
		    }catch(TransmissionException e){
		        throw e;
		    }
	}

	private byte[] executeGetRequestBytes(String sRestCommand,
			Set<NameValuePair> queryParams) throws TransmissionException {
		GetMethod get = new GetMethod();
		get.setPath(sRestCommand);
		get.setDoAuthentication(true);
		if(queryParams != null){
		    NameValuePair[] nvp = new NameValuePair[queryParams.size()]; 
		    nvp = queryParams.toArray(nvp);
		    get.setQueryString(nvp);
		}
		byte[] baResponse = null;
		try{
			System.out.println("executing get:" + get.getURI());
			client.executeMethod(get);
			baResponse = get.getResponseBody();
		}catch(Exception e){
		    e.printStackTrace();
			throw new TransmissionException(e);
		}
		return baResponse;
	}


    private String getMD5(File file) {
        byte[] bytes=null;                                                                                                                                              
        try {                                                                                                                                                           
                bytes=createChecksum(file);                                                                                                                             
        } catch (Exception e) {                                                                                                                                         
                ByteArrayOutputStream boas = new ByteArrayOutputStream();                                                                                               
                e.printStackTrace(new PrintStream(boas));                                                                                                               
               System.out.println(boas.toString());                                                                                                                           
        }                                                                                                                                                               
        if(bytes==null){                                                                                                                                                
                return null;                                                                                                                                            
        }else if(bytes.length==0){                                                                                                                                      
                return null;                                                                                                                                            
        }                                                                                                                                                               
        String hexString=null;                                                                                                                                          
        try {                                                                                                                                                           
        hexString=      StringUtils.getHexString(bytes);                                                                                                                
        } catch (UnsupportedEncodingException e) {                                                                                                                      
                ByteArrayOutputStream boas = new ByteArrayOutputStream();                                                                                               
                e.printStackTrace(new PrintStream(boas));                                                                                                               
              System.out.println(boas.toString());                                                                                                                           
        }                                                                                                                                                               
        return hexString;                                                                                                                                               
}        
    
    public  byte[] createChecksum(File file) throws Exception{                                                                                                                     
                InputStream fis =  new FileInputStream(file);                                                                                                                   
                byte[] buffer = new byte[1024];                                                                                                                                 
                MessageDigest complete = MessageDigest.getInstance("MD5");                                                                                                      
                int numRead;                                                                                                                                                    
                do {                                                                                                                                                            
                        numRead = fis.read(buffer);                                                                                                                             
                        if (numRead > 0) {                                                                                                                                      
                                complete.update(buffer, 0, numRead);                                                                                                            
                        }                                                                                                                                                       
                } while (numRead != -1);                                                                                                                                        
                fis.close();                                                                                                                                                    
                return complete.digest();                                                                                                                                       
}
 
	
}
