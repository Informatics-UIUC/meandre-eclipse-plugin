/*
 * @(#) MeandreEngineServices.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.server;

/**
 * 
 * @author Amit Kumar
 * modified on May 31, 2008 11:47:34 PM -added server plugin version and min supported server url 
 *
 */
public class MeandreEngineServicesConstants {
	public static final String ADD_REPOSITORY_URL_1_3 ="services/repository/add.rdf";
	public static final String ADD_REPOSITORY_URL_1_4 ="services/repository/add.json";
	public static final String SERVER_VERSION_URL =  "services/about/version.txt";
	public static final String SERVER_PLUGIN_URL =  "services/about/plugins.json";
	
	
	public static final String DEFAULT_SERVER ="http://127.0.0.1";
	public static final String DEFAULT_PORT ="1714";
	public static final String DEFAULT_LOGIN = "admin";
	public static final String DEFAULT_PASSWORD = "admin";
	// minimum server version required for running this plugin
	public static final String MIN_SERVER_VER ="1.2";
	// this plugin should be running on the server
	public static final String REQUIRED_SERVER_PLUGIN_KEY="JARTOOL";

	
	
}
