/*
 * @(#) ReadComponentRDFDescriptor.java @VERSION@
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.tools.components;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;


import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Runnable;

/**This class reads a component descriptor
 *
 * @author Amit Kumar
 * Created on Dec 23, 2007 12:39:51 AM
 *
 */

public class ReadComponentDescriptor {

	final static String encoding = "UTF-8";

	private String name;
	private String baseUrl;
	private String description;
	private String rights;
	private String sRightsOther;
	private String creator;
	private String tags ;
	private String firingPolicy;
	private String format;
	private String runnable;

	private Component componentAnnotation = null;
	private ArrayList<ComponentOutput> outputPortAnnotations= new ArrayList<ComponentOutput> (5);
	private ArrayList<ComponentInput> inputPortAnnotations= new ArrayList<ComponentInput> (5);
	private ArrayList<ComponentProperty> propertyAnnotations= new ArrayList<ComponentProperty> (5);
	private Class<?> klazz =null;




	/**Resets the object to process another component
	 *
	 * @param klazz
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public void init(Class klazz){
			this.klazz = klazz;
			outputPortAnnotations.clear();
			inputPortAnnotations.clear();
			propertyAnnotations.clear();
	}




	/**Populates the the arraylist with component information
	 *
	 */
	public void process(){
		componentAnnotation =klazz.getAnnotation(Component.class);
		if(componentAnnotation!= null){
			System.out.println("Processing: " + klazz.getName());
			//System.out.println("componentAnnotation... " + componentAnnotation.baseURL());
		}else{
			//System.out.println("componentAnnotation is null");
			return;
		}

		System.out.println("Name: " + ((Component)componentAnnotation).name());
		System.out.println("Location: " + klazz.getName());
		System.out.println("Creator: " + ((Component)componentAnnotation).creator());

		Field fields[] =klazz.getDeclaredFields();
		for(int i=0; i < fields.length; i++){
		Annotation[] annField=	fields[i].getAnnotations();
		int annotationLocation = -1;
		if((annotationLocation = contains(annField, "ComponentOutput"))!=-1){
			outputPortAnnotations.add((ComponentOutput)annField[annotationLocation]);
		}else if((annotationLocation = contains(annField, "ComponentInput"))!=-1){
			inputPortAnnotations.add((ComponentInput)annField[annotationLocation]);
		}else if((annotationLocation = contains(annField, "ComponentProperty"))!=-1){
			propertyAnnotations.add((ComponentProperty)annField[annotationLocation]);
		}
		}

	if(componentAnnotation == null){
		System.out.println("Error Component Annotation is missing.");
		System.exit(1);
	}

	name = componentAnnotation.name();
	baseUrl = componentAnnotation.baseURL();
	description = componentAnnotation.description();
	rights = getRights(componentAnnotation.rights());
	sRightsOther =componentAnnotation.rightsOther();
	creator = componentAnnotation.creator();
	tags = componentAnnotation.tags();
	firingPolicy = getFiringPolicy(componentAnnotation.firingPolicy());
	rights = (rights.equals("Other"))?sRightsOther:rights;
    format = componentAnnotation.format();
	runnable =getRunnable(componentAnnotation.runnable());

	}



	// return the firing policy
	private String getFiringPolicy(FiringPolicy firingPolicy) {
		return firingPolicy.toString();
	}




	// return the full rights information
	private  String getRights(Licenses rights) {
		String rightsString = "Others";
		switch(rights){
		case UofINCSA:
			rightsString = "University of Illinois/NCSA Open Source License";
			break;
		case ASL_2:
			rightsString ="Apache License 2.0";
			break;
		default:
		rightsString ="Others";
		break;
		}
		return rightsString;
	}




	// return the index where a particular annotation was found
	private int contains(Annotation[] annField, String annotationType) {
		int countAnnotations= annField.length;
		if(countAnnotations==0){
			return -1;
		}
		for(int thisAnnotation = 0; thisAnnotation < countAnnotations; thisAnnotation++){
			if(annField[thisAnnotation].annotationType().getCanonicalName().endsWith(annotationType))
			{
				return thisAnnotation;
			}
		}

		return -1;
		}




	/**
	 * @return the firingPolicy
	 */
	public String getFiringPolicy() {
		return firingPolicy;
	}




	/**
	 * @param firingPolicy the firingPolicy to set
	 */
	public void setFiringPolicy(String firingPolicy) {
		this.firingPolicy = firingPolicy;
	}




	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}




	/**
	 * @return the baseUrl
	 */
	public String getBaseUrl() {
		return baseUrl;
	}




	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}




	/**
	 * @return the rights
	 */
	public String getRights() {
		return rights;
	}




	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}




	/**
	 * @return the tags
	 */
	public String getTags() {
		return tags;
	}


	public String getFormat(){
		return format;
	}

	   private String getRunnable(Runnable runnable){
	    	String runnableString = "java";
	    	switch(runnable){
	    	case java:
	    		runnableString="java";
	    		break;
	    	case python:
	    		runnableString="python";
	    		break;
	    	case lisp:
	    		runnableString="lisp";
	    		break;
	    	default:
	    		break;
	    	}
	    	
	    	return runnableString;
	    }
	    

	/** Returns the descriptive information about the component
	 *
	 */
	public String toString(){
	StringBuilder sbuilder = new StringBuilder();
	sbuilder.append("Component Name: <b>" + this.getName() + " </b><br/>\n");
	sbuilder.append("Created by: <i>"+this.getCreator()+"</i><br/>\n");
	sbuilder.append("Description: "+ this.getDescription() +"<br/>");
	sbuilder.append("Rights: "+this.getRights() + "<br/> \n");
	sbuilder.append("Tags: <b>"+this.getTags()+"</b><br/> \n");
	return sbuilder.toString();
	}
}
