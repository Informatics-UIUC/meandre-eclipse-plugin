/*
 * @(#) FileSystemUtils.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.meandre.ide.eclipse.component.logger.MeandreLogger;
import org.osgi.framework.Bundle;

public class FileSystemUtils {
	
	
	/**For a given bundle and the folder return the path on the file system.
	 * 
	 * @param bundleId
	 * @param samplesFolder
	 * @return
	 */
	public static String findDirectory(String bundleId, String samplesFolder) {
		
		Bundle bundle = Platform.getBundle(bundleId);
		if (bundle != null) {
				URL samplesDir = bundle
					.getEntry(samplesFolder);
			if (samplesDir != null) {
				URL url;
				try {
					url = FileLocator.resolve(samplesDir);
					if (url.getProtocol().equals("file")) {
						File samplesPath = new File(url.getFile().replace('/',
								File.separatorChar));
						return samplesPath.getAbsolutePath();
					}
				} catch (IOException e) {
					MeandreLogger.logError("File not found: " + e.getMessage());
				}
			}
		}
		return null;
	}

}
