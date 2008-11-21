/**
 * @(#) CreateComponentJar.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.tools.components;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import com.tonicsystems.jarjar.DepFind;
import com.tonicsystems.jarjar.DepHandler;

/**This class creates a jar file for a given component.
 * It calculates the class file dependencies and jars them.
 * This task complements the FindComponentDep which finds out
 * the jar file dependencies.
 * 
 * @author Amit Kumar
 * Created on Mar 23, 2008 2:55:32 AM
 *
 */
public class CreateComponentJar {
	
	public static void main(String args[]) throws IOException{
		
		
		//String cp1 = "bin/org/monkproject/meandre/tests/Rectangle.class";
		//String appClasspath ="/content/workspace/Meandre-Component-Devkit-trunk/bin";
	
		if(args.length!=3){
		System.out.println("Error: Invalid arguments. Expects three arguments. ComponentClasspath, " +
				"\nlocation of the bin directory and the name of the jar file");
		System.exit(0);	
		}
		
		
		String cp1 = args[0];
		String appClasspath =args[1];
		String jarFile = args[2];
		
		
		
		
		
		File outFile = new File(jarFile);

		BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(outFile));
		
		Manifest manifest =new Manifest();
        Attributes manifestAttr = manifest.getMainAttributes();
        manifestAttr.putValue("Manifest-Version","1.0");
        manifestAttr.putValue("Meandre-Component-Svn-Version","2.0.0");
        java.util.Set entries= manifestAttr.entrySet();
         for(java.util.Iterator i = entries.iterator(); i.hasNext();){
        	 System.out.println("Manifest attribute:>> "
			+i.next().toString());
         }
         JarOutputStream jo = new JarOutputStream(bo,manifest);
 		
		
	    MeandreComponentDepHandler handler = new MeandreComponentDepHandler(DepHandler.LEVEL_CLASS, cp1);
		try {
			new DepFind().run(cp1,appClasspath, handler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Number of class dependencies: " + handler.getJarList().size());
		java.util.Iterator<String> it = handler.getJarList().iterator();
		String fname=null;
		String className=null;
		File file =  null;
		BufferedInputStream bi=null;
		JarEntry je=null;
		while(it.hasNext()){
			className= it.next() +".class";
			fname = appClasspath +  File.separator + className;
			 bi= new BufferedInputStream(new FileInputStream(fname));
			je = new JarEntry(className);
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
	}

}
