/*
 * @(#) DetectAnnotationTest.java @VERSION@
 * 
 * Copyright (c) 2009+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.test.components;


import java.lang.annotation.Annotation;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meandre.annotations.DetectDefaultComponentAnnotations;

public class DetectAnnotationTest {

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
	public void testSuperClassComponentExtendsConcreteAnnotation(){
		System.out.println("In ClassComponentExtendsConcreteAnnotation");
		SuperClassComponentExtendsConcrete sc = new SuperClassComponentExtendsConcrete();
		DetectDefaultComponentAnnotations dca = new DetectDefaultComponentAnnotations();
		HashMap<String,Object> hmList=dca.getComponentClassAnnotationMap(sc.getClass(), org.meandre.annotations.Component.class);
		assert(hmList.size()>0);
		for(String key: hmList.keySet()){
			System.out.println(key +"  " + hmList.get(key));
		}
	}
	
	@Test
	public void testSuperClassComponentExtendsConcreteOverridesComponentAnnotation(){
		System.out.println("In SuperClassComponentExtendsConcreteOverridesComponentAnnotation");
		SuperClassComponentExtendsConcreteOverridesComponentAnnotation sc = new SuperClassComponentExtendsConcreteOverridesComponentAnnotation();
		DetectDefaultComponentAnnotations dca = new DetectDefaultComponentAnnotations();
		HashMap<String,Object> hmList=dca.getComponentClassAnnotationMap(sc.getClass(), org.meandre.annotations.Component.class);
		assert(hmList.size()>0);
		for(String key: hmList.keySet()){
			System.out.println(key +"  " + hmList.get(key));
		}
	}
	
	
	@Test
	public void testSuperClassComponentExtendsAbstractAnnotation(){
		System.out.println("In ClassComponentExtendsAbstractAnnotation");
		SuperClassComponentExtendsAbstract sc = new SuperClassComponentExtendsAbstract();
		DetectDefaultComponentAnnotations dca = new DetectDefaultComponentAnnotations();
		HashMap<String,Object> hmList=dca.getComponentClassAnnotationMap(sc.getClass(), org.meandre.annotations.Component.class);
		assert(hmList.size()>0);
		for(String key: hmList.keySet()){
			System.out.println(key +"  " + hmList.get(key));
		}
	}
	
	@Test
	public void testSuperClassComponentOverridesInputAnnotation(){
		System.out.println("In Input Override test");
		SuperClassComponentExtendsConcrete sc = new SuperClassComponentExtendsConcrete();
		DetectDefaultComponentAnnotations dca = new DetectDefaultComponentAnnotations();
		HashMap<String,Annotation> hmList=dca.getComponentFieldAnnotations(sc.getClass(), org.meandre.annotations.ComponentInput.class);
		assert(hmList.size()==2);
		for(String key: hmList.keySet()){
			System.out.println(key +"  " + hmList.get(key));
		}
	}
	
	@Test
	public void testSuperClassComponentOutputAnnotation(){
		System.out.println("In Output test");
		SuperClassComponentExtendsConcrete sc = new SuperClassComponentExtendsConcrete();
		DetectDefaultComponentAnnotations dca = new DetectDefaultComponentAnnotations();
		HashMap<String,Annotation> hmList=dca.getComponentFieldAnnotations(sc.getClass(), org.meandre.annotations.ComponentOutput.class);
		assert(hmList.size()==2);
		for(String key: hmList.keySet()){
			System.out.println(key +"  " + hmList.get(key));
		}
	}
	
	@Test
	public void testSuperClassComponentPropertiesAnnotation(){
		System.out.println("In Properties test");
		SuperClassComponentExtendsConcrete sc = new SuperClassComponentExtendsConcrete();
		DetectDefaultComponentAnnotations dca = new DetectDefaultComponentAnnotations();
		HashMap<String,Annotation> hmList=dca.getComponentFieldAnnotations(sc.getClass(), org.meandre.annotations.ComponentProperty.class);
		assert(hmList.size()==2);
		for(String key: hmList.keySet()){
			System.out.println(key +"  " + hmList.get(key));
		}
	}
	
	
	@Test
	public void testNatureAnnotation(){
	System.out.println("In NatureAnnotation test");	
	SuperClassComponentExtendsConcrete sc = new SuperClassComponentExtendsConcrete();
	DetectDefaultComponentAnnotations dca = new DetectDefaultComponentAnnotations();
	HashMap<String, Object> hmList=dca.getComponentClassAnnotationMap(sc.getClass(), org.meandre.annotations.ComponentNature.class);
	assert(hmList.size()>0);
	for(String key: hmList.keySet()){
		System.out.println(key +"  " + hmList.get(key));
	}
	
	}
	
	@Test
	public void testNaturesAnnotation(){
	System.out.println("In NaturesAnnotation test");	
	SuperClassComponentExtendsConcrete sc = new SuperClassComponentExtendsConcrete();
	DetectDefaultComponentAnnotations dca = new DetectDefaultComponentAnnotations();
	HashMap<String, Object> hmList=dca.getComponentClassAnnotationMap(sc.getClass(), 
									org.meandre.annotations.ComponentNatures.class);
	assert(hmList.size()>0);
	for(String key: hmList.keySet()){
		System.out.println(key +"-" + hmList.get(key));
	}
	
	}
	
	
}
