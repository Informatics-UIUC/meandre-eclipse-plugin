/*
 * @(#) RunMAURunnable.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.popup.actions;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.utils.ProjectClassLoader;
import org.meandre.mau.MAUExecutor;

/**Runs the MAU object in a thread
 * 
 * @author Amit Kumar
 * Created on Jul 13, 2008 5:42:38 PM
 *
 */
public class RunMAURunnable implements IRunnableWithProgress{
	
	private ISelection targetSelection;
	public RunMAURunnable(ISelection targetSelection) {
		this.targetSelection= targetSelection;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		  	monitor.beginTask("Running MAU",100);
		  	String message = null;
			boolean isError = Boolean.FALSE;
			Object object = ((IStructuredSelection)targetSelection).getFirstElement();
			org.eclipse.core.internal.resources.File file = (org.eclipse.core.internal.resources.File)object;
			//IJavaProject jproject = (IJavaProject) file.getProject().getAdapter(IJavaProject.class);
			System.out.println("here...");
			MessageConsole mc = Activator.findConsole(Activator.CONSOLE_NAME);
			mc.activate();
			MessageConsoleStream out = mc.newMessageStream();
			
			
			IProject project=file.getProject();
			IJavaProject javaProject = JavaCore.create(project);
			if(javaProject!=null){
			URLClassLoader uclassloader=ProjectClassLoader.getProjectClassLoader(javaProject, Boolean.TRUE);
			try {
				Class mauClass = Class.forName("org.meandre.mau.MAUExecutor", true, uclassloader);
				if(mauClass==null){
				System.out.println("mau is null...");
				}else{
				Constructor constructor=	mauClass.getConstructor( new Class[] {String.class} ); 
				MAUExecutor mauexec = (MAUExecutor)constructor.newInstance(new String(file.getLocation().toOSString()));
				
				
				/****/
				mauexec.setParentClassloader(uclassloader);
				mauexec.setOutpuStream(out);
				try {
					mauexec.run();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(!mauexec.hadGracefullTermination()){
					Set<String> messages = mauexec.getAbortMessages();
					int i=0;
					for(String thismessage:messages){
						if(i==0){
							message = thismessage;
						}else{
							message = message + " " + thismessage;
						}
					}
				}else{
					message = "The flow ran successfully.";
				}
				
				
				
				System.out.println("mau class is not null... "+mauClass.getConstructors()[0]);	
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("====> "+ uclassloader.getURLs().length);
			}else{
				System.out.println(project);
			}
			
			/*
			MAUExecutor  mae = new MAUExecutor(file.getLocation().toOSString());
			mae.setOutpuStream(out);
			try {
				mae.run();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(!mae.hadGracefullTermination()){
				Set<String> messages = mae.getAbortMessages();
				int i=0;
				for(String thismessage:messages){
					if(i==0){
						message = thismessage;
					}else{
						message = message + " " + thismessage;
					}
				}
			}else{
				message = "The flow ran successfully.";
			}
			
		*/
			//showMessage(message);
		  	System.out.println(message);
		  	monitor.worked(100);
			monitor.done();
		
	}

}
