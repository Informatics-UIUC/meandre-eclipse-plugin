/*
 * @(#) TestManifest.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.test;


import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestManifest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testManifest(){
	Manifest manifest  = new Manifest();
	Attributes attributes = manifest.getMainAttributes();
	attributes.putValue("1main", "ttt.dd");
	
	
	
	}

}
