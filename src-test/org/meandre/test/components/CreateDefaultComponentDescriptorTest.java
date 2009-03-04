/*
 * @(#) CreateDefaultComponentDescriptorTest.java @VERSION@
 * 
 * Copyright (c) 2009+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.test.components;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meandre.annotations.CreateDefaultComponentDescriptor;
import org.meandre.core.repository.CorruptedDescriptionException;

public class CreateDefaultComponentDescriptorTest {

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
	public void testProcessConcrete() {
		SuperClassComponentExtendsConcrete sc = new SuperClassComponentExtendsConcrete();
		CreateDefaultComponentDescriptor cd = new CreateDefaultComponentDescriptor();
		try {
			String rdf=cd.process(sc.getClass());
			assert(rdf!=null);
			assert(rdf.contains("meandre://seasr.org/components/baseclasscomponent"));
		} catch (CorruptedDescriptionException e) {
			fail(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testProcessAbstract() {
		SuperClassComponentExtendsAbstract sc = new SuperClassComponentExtendsAbstract();
		CreateDefaultComponentDescriptor cd = new CreateDefaultComponentDescriptor();
		try {
			String rdf=cd.process(sc.getClass());
			assert(rdf!=null);
			assert(rdf.contains("meandre://seasr.org/components/baseclasscomponent"));
		} catch (CorruptedDescriptionException e) {
			fail(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	
	@Test
	public void testProcessOverrideComponentAnnotation() {
		SuperClassComponentExtendsConcreteOverridesComponentAnnotation sc = new SuperClassComponentExtendsConcreteOverridesComponentAnnotation();
		CreateDefaultComponentDescriptor cd = new CreateDefaultComponentDescriptor();
		try {
			String rdf=cd.process(sc.getClass());
			assert(rdf!=null);
			assert(rdf.contains("meandre://seasr.org/components/SuperClassComponentExtendsConcreteOverridesComponentAnnotation".toLowerCase()));
		} catch (CorruptedDescriptionException e) {
			fail(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
}
