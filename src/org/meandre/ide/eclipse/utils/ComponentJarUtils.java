/*
 * @(#) ComponentJarUtils.java @VERSION@
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
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.tools.asm.ComponentExecuteMethodTransformer;
import org.meandre.tools.asm.MethodDataType;
import org.meandre.tools.components.MeandreComponentDepHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import com.tonicsystems.jarjar.DepFind;
import com.tonicsystems.jarjar.DepHandler;

/**Refractored code from InstallComponentRunnable
 *
 * @author Amit Kumar
 * Created on Jul 12, 2008 2:20:16 AM
 *
 */
public class ComponentJarUtils {

	ProjectSourceUtils projectSourceUtils;

	public ComponentJarUtils(){
		this.projectSourceUtils = new ProjectSourceUtils();
	}



	/**Returns the list of interfaces the class implements
	 *
	 * @param claszz
	 * @return
	 */
	public String getInterfaceList(Class claszz) {
		Class[] interfaceList = claszz.getInterfaces();
		String interfaceListString="";
		for(int i=0; i<interfaceList.length;i++){
			if(i==0){
				interfaceListString = interfaceList[i].getName();
			}else{
				interfaceListString = interfaceListString +","+interfaceList[i].getName();
			}
		}
		return interfaceListString;
	}

	public HashMap<String, String> getComponentPropertyDataType(Class claszz){

		Field[] fields=claszz.getDeclaredFields();
		HashMap<String, String> hm=new HashMap<String,String>(4);
		for(int i=0; i < fields.length;i++){
			ComponentProperty cp=fields[i].getAnnotation(ComponentProperty.class);
			if(cp!= null){
				String propName = cp.name();
				String fieldType = fields[i].getType().getName();
				hm.put(propName, fieldType);
			}
		}
		return hm;
	}


	public HashMap<String, String> getComponentInputDataType(Class claszz){
		Field[] fields=claszz.getDeclaredFields();
		HashMap<String, String> hm=new HashMap<String,String>(4);
		for(int i=0; i < fields.length;i++){
			ComponentInput cp=fields[i].getAnnotation(ComponentInput.class);
			if(cp!= null){
				String propName = cp.name();
				String fieldType = fields[i].getType().getName();
				hm.put(propName, fieldType);
			}
		}
		return hm;
	}


	public HashMap<String, String> getComponentOutputDataType(Class claszz){

		Field[] fields=claszz.getDeclaredFields();
		HashMap<String, String> hm=new HashMap<String,String>(4);
		for(int i=0; i < fields.length;i++){
			ComponentOutput cp=fields[i].getAnnotation(ComponentOutput.class);
			if(cp!= null){
				String propName = cp.name();
				String fieldType = fields[i].getType().getName();

				hm.put(propName,fieldType);
			}
		}
		return hm;
	}






	/**Get the DataType from the component class
	 *
	 * @param filepath
	 * @return
	 */
	public HashMap<String, MethodDataType> getDataType(String filepath){
		ClassNode cn = new ClassNode();
		ClassReader cr;
		try {
			cr = new ClassReader(getClassBytes(filepath));
			cr.accept(cn, 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ComponentExecuteMethodTransformer cet = new ComponentExecuteMethodTransformer(null);
		cet.transform(cn);
		HashMap<String,MethodDataType> cd=cet.getComponentDataTypeHashMap();
		return cd;
	}




	private byte[] getClassBytes(String filepath) throws IOException {
		File file = new File(filepath);
		InputStream is = new FileInputStream(file);
		long length = file.length();

		if (length > Integer.MAX_VALUE) {
			// File is too large

		}

		byte[] bytes = new byte[(int)length];

		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}



	/**Finds the component dependencies transitively
	 *
	 * @param cp1
	 * @param appClasspath
	 * @param handler
	 * @param start
	 */
	public void findComponentDep(String cp1, String appClasspath,
			MeandreComponentDepHandler handler, boolean start,HashMap<String,String> classList) {
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
				findComponentDep(key,appClasspath,new MeandreComponentDepHandler(DepHandler.LEVEL_CLASS, key), Boolean.FALSE,classList);
			}
		}


	}

	/**
	 * This function creates a component jar file; that has all the classes the
	 * component depends upon
	 * @param claszz
	 * @param project
	 *
	 * @param cp1
	 * @param appClasspath
	 * @param jarFile
	 * @return
	 * @throws IOException
	 */
	public boolean createComponentJar(Class claszz,
			IJavaProject project, String componentClass, String outputLocation,
			String projectPath,String sourcePath,
			String jarFile,ArrayList<String> resourceList,
			String componentVersion,
			boolean storeSource,HashMap<String,String> classList)
	throws IOException {

		MessageConsole mc = Activator.findConsole(Activator.CONSOLE_NAME);
		mc.activate();
		MessageConsoleStream out = mc.newMessageStream();
		out.println("In create component jar function >" + jarFile);
		File outFile = new File(jarFile);
		out.println("Before creating DepHandler>>> " + componentClass);

		MeandreComponentDepHandler handler = new MeandreComponentDepHandler(
				DepHandler.LEVEL_CLASS, componentClass);
		out.println("After creating DepHandler " + componentClass);
		findComponentDep(componentClass, outputLocation,handler, Boolean.TRUE,classList);

		if (classList.keySet().size() == 0) {
			return Boolean.FALSE;
		}
		ArrayList<IFile> sourceList = this.projectSourceUtils.getSourceList(project,outputLocation, classList);
		String getInterfaceList = getInterfaceList(claszz);
		HashMap<String,MethodDataType> hmDataType = getDataType(componentClass);
		BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(outFile));
		Manifest manifest = new Manifest();
		Attributes manifestAttr = manifest.getMainAttributes();
		manifestAttr.putValue("Manifest-Version", "1.0");
		manifestAttr
		.putValue("Meandre-Component-Svn-Version", componentVersion);
		manifestAttr.putValue("isComponent", "true");
		manifestAttr.putValue("hasSource", storeSource+"");
		manifestAttr.putValue("interfaceList", getInterfaceList);

		HashMap<String,String> componentProperties =getComponentPropertyDataType(claszz);

		if(componentProperties!=null){
			Iterator its = componentProperties.keySet().iterator();
			String key=null;
			while(its.hasNext()){
				key = (String) its.next();
				if(validManifestKey(key))
				manifestAttr.putValue("prop_"+key,"java/lang/String");
			}
		}

		componentProperties = getComponentInputDataType(claszz);
		String value=null;
		if(componentProperties!=null){
			Iterator its = componentProperties.keySet().iterator();
			String key=null;
			while(its.hasNext()){
				key = (String) its.next();
				MethodDataType mdt=hmDataType.get(key+"_getDataComponentFromInput");
				if(mdt!=null){
					value=mdt.getVariableDataType();
					if(validManifestKey(key))
					manifestAttr.putValue("input_"+key,value );
				}

			}
		}

		componentProperties = getComponentOutputDataType(claszz);
		if(componentProperties!=null){
			Iterator its = componentProperties.keySet().iterator();
			String key=null;
			while(its.hasNext()){
				key = (String) its.next();
				MethodDataType mdt=hmDataType.get(key+"_pushDataComponentToOutput");
				if(mdt!=null){
					value=mdt.getVariableDataType();
					if(validManifestKey(key))
					manifestAttr.putValue("output_"+key,value);
				}
			}
		}

		JarOutputStream jo = new JarOutputStream(bo, manifest);
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

			String jarEntry = className.replace(outputLocation+File.separator, "");
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
				if(sourceFile.getRawLocation()!=null){
					String fname=sourceFile.getRawLocation().makeAbsolute().toOSString();
					bi = new BufferedInputStream(new FileInputStream(fname));
					String jarEntry = fname.replace(projectPath + sourcePath, "");
					jarEntry = jarEntry.replace(File.separatorChar, '/');
					je = new JarEntry(jarEntry);
					jo.putNextEntry(je);

					byte[] buf = new byte[1024];
					int anz;

					while ((anz = bi.read(buf)) != -1) {
						jo.write(buf, 0, anz);
					}
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

	/**Check if the key is valid
	 *
	 * @param s
	 * @return
	 */
	private boolean validManifestKey(final String s) {
		final char[] chars = s.toCharArray();
		  for (int x = 0; x < chars.length; x++) {
		    final char c = chars[x];
		    if ((c >= 'a') && (c <= 'z')) continue; // lowercase
		    if ((c >= 'A') && (c <= 'Z')) continue; // uppercase
		    if ((c >= '0') && (c <= '9')) continue; // numeric
		    if ((c >= '_') && (c <= '-')) continue; // valid key
		    return false;
		  }
		  return true;

	}




}
