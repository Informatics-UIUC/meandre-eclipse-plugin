/*
 * @(#) DescriptorFilenameFilter.java @VERSION@
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.tools.components;

import java.io.File;
import java.io.FilenameFilter;

/**Filename filter for descriptor
 *
 * @author Amit Kumar
 * Created on Dec 28, 2007 8:12:39 PM
 *
 */
public class DescriptorFilenameFilter implements FilenameFilter {

	public boolean accept(File dir, String filename) {
		if(filename.endsWith(".rdf")){
			return true;
		}
		return false;
	}

}
