/*
 * @(#) TypeUtils.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.utils;

/**Utility Class that returns the type information 
 * based on a string.
 * 
 * @author Amit Kumar
 * Created on Apr 16, 2008 10:39:41 AM
 *
 */
public class TypeUtils {
	static final private char booleanType ='Z'; 
	static final private char charType ='C'; 
	static final char byteType ='B'; 
	static final char shortType ='S'; 
	static final char intType ='I'; 
	static final char floatType ='F'; 
	static final char longType ='J'; 
	static final char doubleType ='D'; 
	static final char objectType ='L';
	static final char arrayType ='['; 
	
	
	public static String getDataType(String signature){
		boolean isArrayType = Boolean.FALSE;
		boolean isDblArray = Boolean.FALSE;
		if(signature==null){
			return null;
		}
		if(signature.length()==0){
			return null;
		}
		if(signature.charAt(0) == arrayType){
			signature = signature.substring(1);
			isArrayType=true;
			if(signature.charAt(0)==arrayType){
				isDblArray = Boolean.TRUE;
				signature = signature.substring(1);
			}
		}
		//check if this is a primary type... 
		// get the next token
		String dataType;
		char primary=signature.charAt(0);
		if(primary==byteType){
			dataType= "byte";
		}else if(primary==charType){
			dataType= "char";
		}else if(primary==shortType){
			dataType= "short";
		}else if(primary==intType){
			dataType= "int";
		}else if(primary==floatType){
			dataType= "float";
		}else if(primary==longType){
			dataType= "long";
		}else if(primary==doubleType){
			dataType= "double";
		}else if(primary == objectType ){
			dataType = signature.substring(1);
			dataType = dataType.replaceAll("/",".");
		}else if(primary == booleanType ){
			dataType = "boolean";
		}else{
			dataType = signature.replaceAll("/", ".");
		}
		
		
		if(isDblArray){
			dataType = dataType +" " + "[][]";
		}else if(isArrayType){
			dataType = dataType +" " + "[]";
		}
		dataType = dataType.replaceAll(";","");
		return dataType;
	}

}
