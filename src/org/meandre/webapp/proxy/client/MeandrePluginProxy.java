/*
 * @(#) MeandrePluginClient.java @VERSION@
 *
 * Copyright (c) 2008+ Amit Kumar
 *
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.webapp.proxy.client;

import java.util.ArrayList;

import org.meandre.plugins.bean.Plugin;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/** Accommodating changes for Peter's new changes.
 *
 * @author Amit Kumar
 * Created on Nov 11, 2008 4:01:48 PM
 *
 */
public class MeandrePluginProxy extends org.meandre.tools.client.v1.MeandreProxy {

	private String user;
	private String passwd;
	private String serverHost;
	private String baseUrl;
	private int serverPort;


	// list of plugins running
	ArrayList<Plugin> pluginList = new ArrayList<Plugin>(5);

	private boolean missingPlugin = Boolean.FALSE;

	// used to deserialize the plugin list
	XStream xstream = new XStream(new JettisonMappedXmlDriver());

	public MeandrePluginProxy(String user, String passwd, String serverHost,
			int serverPort) {
		super();
		//super(user, passwd, serverHost, serverPort);
		this.user=user;
		this.passwd = passwd;
		this.serverHost=serverHost;
		this.serverPort = serverPort;
		setServerUrl();
	}

	public void update(){
		super.update(user,passwd, serverHost, serverPort);
		setServerUrl();
	}

	@Override
	public void update ( String sUser, String sPasswd, String sServerHost,
			int iServerPort ){
		this.user=sUser;
		this.passwd = sPasswd;
		this.serverHost=sServerHost;
		this.serverPort = iServerPort;
		setServerUrl();
		try{
		super.update(sUser,sPasswd, sServerHost, iServerPort);
		}catch(Exception ex){
			System.out.println("Cannot connect: "+ ex.getMessage());
		}
	}




	@Override
	public void close() {
		flushRoles();
		flushRepository();
	}


	public String getServerUrl(){
		return baseUrl;
	}


	public void setServerUrl() {
		if(!serverHost.startsWith("http")){
			serverHost = "http://"+serverHost;
		}
		if(serverHost.indexOf(":")==-1){
		this.baseUrl  =  serverHost + ":" +
        Integer.toString(serverPort) + "/";
		}else{
		this.baseUrl  = serverHost +":"+serverPort +"/";
		}
	}



	public String getLogin() {
		return this.user;
	}

	@Override
	public String getPassword() {
		return this.passwd;
	}

	public void updateServerPlugins() {
		String jsonString = getServerPluginsAsJSON();
		if (jsonString == null) {
			this.missingPlugin = false;
			return;
		}
		if (jsonString.startsWith("[{")) {
			jsonString = "{\"list\":{\"org.meandre.plugins.bean.Plugin\":" + jsonString
					+ "}}";
		}
		System.out.println(jsonString);
		// continue with the 1.3 and
		try {
			this.pluginList = (ArrayList<Plugin>) xstream.fromXML(jsonString);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			this.pluginList = new ArrayList<Plugin>(4);
			System.out.println(ex.getMessage());
		}
		if (this.pluginList != null) {
			System.out.println("Found: " + pluginList.size() + " plugins.");
		}

	}


}
