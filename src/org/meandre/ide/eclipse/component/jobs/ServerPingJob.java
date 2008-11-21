/*
 * @(#) ServerPingJob.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.meandre.ide.eclipse.component.Activator;
import org.meandre.ide.eclipse.component.logger.MeandreLogger;
import org.meandre.webapp.proxy.client.MeandrePluginProxy;


public class ServerPingJob extends Job {
	
	private MeandrePluginProxy meandreProxy;
	// check every 5 seconds
	static final int SLEEP_TIME=10000;
	
	public ServerPingJob(String name) {
		super(name);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Checking if Meandre Server is available: ", 1);
		boolean isConnected = Boolean.FALSE;
		isConnected=meandreProxy.ping();
		Activator.isConnected = isConnected;
		if(!isConnected)
		MeandreLogger.logWarning("Could not connect to the Meandre Server");
		
		monitor.done();
		schedule(SLEEP_TIME);
		return Status.OK_STATUS;
	}

	public void setProxy(MeandrePluginProxy meandreProxy2) {
		this.meandreProxy = meandreProxy2;
	}

	
	
}
