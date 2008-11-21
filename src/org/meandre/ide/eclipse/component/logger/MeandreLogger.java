/*
 * @(#) MeandreLogger.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.meandre.ide.eclipse.component.Activator;

/**This class provides logging facility
 *  Based on the FavoritesLog class of the Eclipse 
 *  Commercial Quality Plugins book
 * @author Amit Kumar
 * Created on Mar 12, 2008 1:34:23 AM
 *
 */
public class MeandreLogger {

	public static void logInfo(String message) {
		log(IStatus.INFO, IStatus.OK, message, null);
	}
	
	public static void logWarning(String message) {
		log(IStatus.WARNING, IStatus.WARNING, message, null);
	}
	
	public static void logError(String message) {
		log(IStatus.ERROR, IStatus.ERROR, message, null);
	}
	
	public static void logError(Throwable exception) {
		logError("Unexpected Exception", exception);
	}

	public static void logError(String message, Throwable exception) {
		log(IStatus.ERROR, IStatus.OK, message, exception);
	}

	public static void log(int severity, int code, String message,
			Throwable exception) {
		log(createStatus(severity, code, message, exception));
	}

	public static IStatus createStatus(int severity, int code, String message,Throwable exception) {
		return new Status(severity, Activator.PLUGIN_ID, code, message,exception);
	}

	public static void log(IStatus status) {
		Activator.getDefault().getLog().log(status);
	}

}
