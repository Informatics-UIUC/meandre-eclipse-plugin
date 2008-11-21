/*
 * @(#) TestZigZag.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.meandre.zigzag.parser.ParseException;
import org.meandre.zigzag.parser.ZigZag;
import org.meandre.zigzag.semantic.FlowGenerator;

public class TestZigZag {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void testZigZag() throws ParseException{
		String sFileName="/content/runtime-EclipseApplication/Meandre-Monk-Components-trunk/test.zz";
		 FileInputStream fis=null;
		try {
			fis = new FileInputStream(sFileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         ZigZag parser = new ZigZag(fis);
         parser.setFileName(sFileName);//sFileName = sFileName;
         parser.initFlowGenerator();//= new FlowGenerator();
         parser.getFlowGenerator().init(sFileName);
         try {
             parser.start();
             System.out.println();
             parser.getFlowGenerator().generateMAU(sFileName);
         }
         catch ( ParseException pe ) {
             throw pe;
         }
	}
	
}
