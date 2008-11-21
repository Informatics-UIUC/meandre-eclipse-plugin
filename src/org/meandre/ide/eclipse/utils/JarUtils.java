/*
 * @(#) JarUtils.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.utils;

import java.util.ArrayList;
import java.util.Iterator;

public class JarUtils {

	public static boolean notInFilter(String name, ArrayList<String> filterJarList) {
		if (filterJarList.size() == 0) {
			return Boolean.TRUE;
		}
		Iterator<String> it = filterJarList.iterator();
		String filterJar = null;
		name = name.toLowerCase();
		while (it.hasNext()) {
			filterJar = it.next();
			if (name.startsWith(filterJar)) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

}
