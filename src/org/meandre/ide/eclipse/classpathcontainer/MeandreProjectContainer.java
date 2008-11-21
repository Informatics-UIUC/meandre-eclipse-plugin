/**
 * @(#) MeandreProjectContainer.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.classpathcontainer;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.logger.MeandreLogger;

/**This container provides access to Meandre libraries that
 * are required to create a new Component project
 * 
 * @author Amit Kumar
 * Created on Jun 3, 2008 8:47:23 PM
 *
 */
public class MeandreProjectContainer implements IClasspathContainer {
	public final static Path ID = new Path("org.meandre.MEANDRE_INFRASTRUCTURE");

	// description of the container.
	String description;
	// version
	String version;
	// unique id for the the container
	private IPath path;
	private boolean isValid = false;
	private ClasspathContainerMetadata selectedClasspathContainerMetadata = null;



	public MeandreProjectContainer(IPath path){
		this.path = path;
		version = path.lastSegment();
		description =   "MEANDRE INFRASTRUCTURE -";
		selectedClasspathContainerMetadata= Activator.getClasspathContainerUtils().getClasspathContainerForVersion(version);
		if(selectedClasspathContainerMetadata==null){
			isValid = false;
			MeandreLogger.logError("Could not get classpathcontainer for " + path);
		}else{
			isValid=true;
			description =   "MEANDRE INFRASTRUCTURE -" + selectedClasspathContainerMetadata.name;
		}
		
	}




	public IClasspathEntry[] getClasspathEntries() {
	
		if(selectedClasspathContainerMetadata==null){
		MeandreLogger.logError("Error: selectedClasspathContainerMetadata is null" );
		return null;
		}
		 ArrayList<IClasspathEntry> entryList = new ArrayList<IClasspathEntry>();
	        // fetch the names of all files that match our filter
	        for( String lib: selectedClasspathContainerMetadata.jarList ) {
	            // strip off the file extension
	        	
	            int i = lib.lastIndexOf('.');
	            String ext= null;
	            if(i!=-1){ // a valid file is something.jar or something.1.2.2.3.jar
	            ext = lib.substring(i+1);
	            // bypass all the jars that have "-src." in the jar name
	            if(lib.indexOf("-src.")!=-1){
	            	continue;
	            }
	            // now see if this archive has an associated src jar
	            File srcArc = new File(lib.replace("."+ext, "-src."+ext));
	            Path srcPath = null;
	            // if the source archive exists then get the path to attach it
	            if( srcArc.exists()) {
	                srcPath = new Path(srcArc.getAbsolutePath());
	            }
	            // create a new CPE_LIBRARY type of cp entry with an attached source 
	            // archive if it exists
	            entryList.add( JavaCore.newLibraryEntry( 
	                    new Path(lib) , srcPath, new Path("/")));  
	            }
	        }
	        // convert the list to an array and return it
	        IClasspathEntry[] entryArray = new IClasspathEntry[entryList.size()];
	        return (IClasspathEntry[])entryList.toArray(entryArray);
	}

	public String getDescription() {
		return description;
	}

	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	public IPath getPath() {
		return this.path;
	}




	public boolean isValid() {
		return this.isValid;
	}





}
