/*
 * @(#) UploadComponentTest.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.webapp.proxy;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/** Tests the upload component.
 * 
 * @author Amit Kumar
 * Created on Nov 17, 2008 12:19:01 PM
 *
 */
public class UploadComponentTest {
    // a hashmap of file name and File values
	public static HashMap<String, File> appJarList = new HashMap<String, File>(
			100);
	// location where the jar files are kept
	public static String jarDirLocation = "/tmp/jar";
	// Component RDF file location
	public static String repositoryLocation = "/tmp/rdf/MyComponent.rdf";
	static HttpClient client;
	// location of the 
	///services/repository/add.html
	public static final String addURL = "http://127.0.0.1:1714/services/repository/add.xml";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		client = new HttpClient();
		client.getState().setCredentials(new AuthScope(null, 1714, null),
				new UsernamePasswordCredentials("admin", "admin"));
		makeJarHashMap(jarDirLocation);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUpload() {
		String meandreUploadUrl = addURL;
		String[] jarDependencies = {"com.example.MyComponent-mycomponent.jar"};
		File componentRdfFile = new File(repositoryLocation);
		boolean dump = true;
		boolean embed = false;
		boolean overwrite = false;
		
		assertTrue(componentRdfFile.exists());
		
		
		PostMethod filePost;
		filePost = new PostMethod(meandreUploadUrl);
		int arrayLen = 0;
		int numJars = 0;

		if (jarDependencies == null) {
			arrayLen = 4;
			numJars = 0;
		} else {
			arrayLen = jarDependencies.length + 4;
			numJars = jarDependencies.length;
		}

		Part[] parts = new Part[arrayLen];
		try {
			parts[0] = new StringPart("embed", embed + "");
			parts[1] = new StringPart("overwrite", overwrite + "");
			parts[2] = new StringPart("dump", dump + "");
			parts[3] = new FilePart("repository", componentRdfFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		File jarFile = null;
		int count = 4;
		assertTrue(numJars==1);
		for (int i = 0; i < numJars; i++) {
			jarFile = appJarList.get(jarDependencies[i]);
			if (jarFile != null) {
				try {
					parts[count] = new FilePart("context", jarFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				count++;
			} else {
				System.out.println("Error: Could not get > "
						+ jarDependencies[i]);

			}
		}
		System.out.println("Component: " + componentRdfFile.getName()
				+ " Number of dependencies: " + numJars);
		
		filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost
				.getParams()));
		// filePost.setQueryString("overwrite=true");
		// makes it faster..
		// filePost.getParams().setBooleanParameter(HttpMethodParams.
		// USE_EXPECT_CONTINUE,true);
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

		System.out.println("Status: " + status + " " + filePost.isAborted());

		try {
			Reader reader = new InputStreamReader(filePost
					.getResponseBodyAsStream(), filePost.getResponseCharSet());
			StringBuffer componentBuffer = new StringBuffer(32768);
			char[] buffer = new char[32768];
			int charsRead;
			while ((charsRead = reader.read(buffer)) != -1) {
				componentBuffer.append(buffer, 0, charsRead);
			}
			componentBuffer.trimToSize(); // trim the backing buffer to the true
											// size so memory isn't wasted
			System.out.println(" Component BUFFER IS : "+ componentBuffer.toString());
					

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			filePost.releaseConnection();
		}

		System.out.println(!filePost.isAborted());
	}

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
