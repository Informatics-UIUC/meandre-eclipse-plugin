/**
 * @(#) MethodDataType.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.tools.asm;

public class MethodDataType {
	private String arg1;
	private String methodName;
	private String variableName;
	private String variableDataType;
	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}
	/**
	 * @param methodName the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	/**
	 * @return the variableName
	 */
	public String getVariableName() {
		return variableName;
	}
	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	/**
	 * @return the variableDataType
	 */
	public String getVariableDataType() {
		return variableDataType;
	}
	/**
	 * @param variableDataType the variableDataType to set
	 */
	public void setVariableDataType(String variableDataType) {
		this.variableDataType = variableDataType;
	}
	/**
	 * @return the arg1
	 */
	public String getArg1() {
		return arg1;
	}
	/**
	 * @param arg1 the arg1 to set
	 */
	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}
	
	public String toString(){
		return this.variableDataType + " : " + this.methodName + " : " + this.variableName +" : "+ this.arg1;
	}

}
