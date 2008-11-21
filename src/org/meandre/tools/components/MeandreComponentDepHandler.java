/*
 * @(#) MeandreComponentDepHandler.java @VERSION@
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.tools.components;

import java.io.IOException;
import java.util.ArrayList;
import com.tonicsystems.jarjar.AbstractDepHandler;

public class MeandreComponentDepHandler extends AbstractDepHandler{

	ArrayList<String> alist= null;
	String componentName = null;

	public MeandreComponentDepHandler(int level, String componentName) {
		super(level);
		alist = new ArrayList<String>(10);
		this.componentName = componentName;
	}

	@Override
	protected void handle(String from, String to) throws IOException {
		if(!this.alist.contains(to)){
		   this.alist.add(to);
		}
	}

	public ArrayList<String> getJarList(){
		return alist;
	}

	public void reset() {
		alist.clear();
	}
}
