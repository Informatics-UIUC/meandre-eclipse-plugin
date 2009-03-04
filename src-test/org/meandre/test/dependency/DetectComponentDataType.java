/*
 * @(#) DetectComponentDependency.java @VERSION@
 * 
 * Copyright (c) 2009+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.test.dependency;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meandre.ide.eclipse.utils.ComponentJarUtils;
import org.meandre.tools.asm.ComponentExecuteMethodTransformer;
import org.meandre.tools.asm.MethodDataType;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class DetectComponentDataType{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testComponentDataType(){
		String filePath ="/tmp/BaseConcreteClassComponent.class";
		ClassNode cn = new ClassNode(); 
		ClassReader cr;
		try {
			cr = new ClassReader(getClassBytes(filePath));
			cr.accept(cn, 0); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ComponentExecuteMethodTransformer cet = new ComponentExecuteMethodTransformer(null);
		cet.transform(cn);
		HashMap<String,MethodDataType> cd=cet.getComponentDataTypeHashMap();
		for(String key: cd.keySet()){
			MethodDataType mdt = cd.get(key);
			System.out.println(key+"--->"+mdt.getVariableDataType());
		}
	}
	
	
	
	private byte[] getClassBytes(String filepath) throws IOException {
		File file = new File(filepath);
		InputStream is = new FileInputStream(file);
		long length = file.length();

		if (length > Integer.MAX_VALUE) {
			// File is too large

		}

		byte[] bytes = new byte[(int)length];

		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
	
}
