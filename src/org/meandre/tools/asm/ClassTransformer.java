/**
 * @(#) ClassTransfomer.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.tools.asm;

import org.objectweb.asm.tree.ClassNode;

public class ClassTransformer {
	protected ClassTransformer ct; 
	
	public ClassTransformer(ClassTransformer ct) { 
	this.ct = ct; 
	} 
	public void transform(ClassNode cn) { 
	if (ct != null) { 
	ct.transform(cn); 
	} 
	} 
	

}
