/*
 * @(#) JsonTest.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.webapp.proxy;

import java.util.ArrayList;

import org.meandre.plugins.bean.Plugin;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;



public class JsonTest {
	static String jsonString ="{\"list\":{\"org.meandre.plugins.bean.Plugin\":[{\"key\":\"JARTOOL\",\"alias\":\"\\/plugins\\/jar\\/*\",\"isServlet\":\"true\",\"className\":\"org.meandre.plugins.tools.JarToolServlet\"},{\"key\":\"VFS\",\"alias\":\"\\/plugins\\/vfs\\/*\",\"isServlet\":\"true\",\"className\":\"org.meandre.plugins.vfs.VFSServlet\"}]}}";
	
	//static String jsonString="{"org.meandre.plugins.bean.Plugin":[{"key":"JARTOOL","className":"org.meandre.plugins.tools.JarToolServlet","isServlet":"true","alias":"/plugins/jar/*"},{"key":"VFS","className":"org.meandre.plugins.vfs.VFSServlet","isServlet":"true","alias":"/plugins/vfs/*"}]}
//"
	public static void main(String args[]){
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		ArrayList<Plugin> alist = (ArrayList<Plugin>)xstream.fromXML(jsonString);
		System.out.println(alist.get(0).getKey());
		
	}
}
