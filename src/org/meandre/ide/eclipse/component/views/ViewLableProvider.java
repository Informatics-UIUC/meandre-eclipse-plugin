/*
 * @(#) ViewLableProvider.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */ 
package org.meandre.ide.eclipse.component.views;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.meandre.ide.eclipse.IImageKeys;
import org.meandre.ide.eclipse.component.Activator;

class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	URL urlComponent;
	URL urlComponentJava;
	URL urlComponentWWWJava;
	URL urlComponentLisp;
	URL urlComponentPython;
	
	URL urlJarWithSrc;
	URL urlJarComponent;
	URL urlServer;
	URL urlLocalServer;
	URL urlJar;
	
	URL urlProperty;
	URL urlInput;
	URL urlOutput;
	
	
	Image componentImage ;
	Image componentJavaImage ;
	Image componentJavaWWWImage ;
	
	Image componentLispImage ;
	Image componentPythonImage ;
	
	Image jarWithSrcImage;
	Image jarImage;
	Image jarComponentImage;
	Image serverImage;
	Image serverLocalImage;
	
	Image propertyImage;
	Image inputImage;
	Image outputImage;
	
	
	public ViewLabelProvider(){
		try {
			urlComponent=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.COMPONENT);
			urlJar=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.JAR);
			urlJarWithSrc=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.JAR_WITH_SRC);
			urlJarComponent=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.JAR_WITH_COMPONENT_CLASS);
			urlServer=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.SERVER);
			urlLocalServer=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.LOCAL_SERVER);
		
			urlComponentJava=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.COMPONENT_JAVA);
			urlComponentWWWJava=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.COMPONENT_JAVA_WWW);
			
			urlComponentPython=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.COMPONENT_PYTHON);
			urlComponentLisp=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.COMPONENT_LISP);
				
			urlProperty=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.PROPERTY);
			urlInput=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.INPUT);
			urlOutput=new URL(Activator.getDefault().getDescriptor().getInstallURL(),IImageKeys.OUTPUT);
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		 componentImage = ImageDescriptor.createFromURL(urlComponent).createImage();
	     jarWithSrcImage =ImageDescriptor.createFromURL(urlJarWithSrc).createImage();
	     jarImage = ImageDescriptor.createFromURL(urlJar).createImage();
		 jarComponentImage = ImageDescriptor.createFromURL(urlJarComponent).createImage();
		 serverImage = ImageDescriptor.createFromURL(urlServer).createImage();
		 serverLocalImage = ImageDescriptor.createFromURL(urlLocalServer).createImage();
		
		 componentJavaImage = ImageDescriptor.createFromURL(urlComponentJava).createImage();
		 componentJavaWWWImage = ImageDescriptor.createFromURL(urlComponentWWWJava).createImage();
			
		 componentPythonImage = ImageDescriptor.createFromURL(urlComponentPython).createImage();  
		 componentLispImage = ImageDescriptor.createFromURL(urlComponentLisp).createImage();
		 
		 
		 propertyImage = ImageDescriptor.createFromURL(urlProperty).createImage();
		 inputImage = ImageDescriptor.createFromURL(urlInput).createImage();
		 outputImage = ImageDescriptor.createFromURL(urlOutput).createImage();
		 
		 
	}
	
		
	
	
	
	public String getColumnText(Object obj, int index) {
		return getText(obj);
	}
	
	
	
	public Image getColumnImage(Object obj, int index) {
		return getImage(obj);
	}
	public Image getImage(Object obj) {
		if(obj instanceof TreeParent){
			if(((TreeParent)obj).isLocalServer()){
				return serverLocalImage;
			}else if(((TreeParent)obj).isServer()){
				return serverImage;
			}else{
				if(((TreeParent)obj).getComponentFormat().equalsIgnoreCase(Activator.LISP_FORMAT)){
					return componentLispImage;
				}else if(((TreeParent)obj).getComponentFormat().equalsIgnoreCase(Activator.PYTHON_FORMAT)){
					return componentPythonImage;
				}else if(((TreeParent)obj).isWebUI){
					return componentJavaWWWImage;
				}else if(((TreeParent)obj).getComponentFormat().equalsIgnoreCase(Activator.JAVA_FORMAT)){
					return componentJavaImage;
				}else{
				return componentImage;
				}
			}
		}else if(obj instanceof TreeObject){
			if(((TreeObject)obj).isHasSource()){
				return  jarWithSrcImage;	
			}else if(((TreeObject)obj).isComponent()){
				return jarComponentImage;	
			}else if(((TreeObject)obj).isProperty()){
				return propertyImage;	
			}else if(((TreeObject)obj).isInput()){
				return inputImage;
			}else if(((TreeObject)obj).isOutput()){
				return outputImage;
			}else {
				return jarImage;
			}
		}else{
			return PlatformUI.getWorkbench().
			getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
	
		}
	}
	
	 public void dispose() {
		 jarImage.dispose();
		 jarImage = null;
		 jarComponentImage.dispose();
		 jarComponentImage = null;
		 jarWithSrcImage.dispose();
		 jarWithSrcImage = null;
		 componentImage.dispose();
		 componentImage = null;
		 
		 componentJavaImage.dispose();
		 componentJavaImage = null;
		 componentPythonImage.dispose();
		 componentPythonImage = null;
		 
		 componentLispImage.dispose();
		 componentLispImage= null;
		 
		 
		 
		 serverImage.dispose();
		 serverImage = null;
		 serverLocalImage.dispose();
		 serverLocalImage = null;
		 
		 
	 } 
}