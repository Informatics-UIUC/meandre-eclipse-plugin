/**
 * @(#) ComponentExecuteMethodTransformer.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.tools.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.TreeMap;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/** ASM transformer to detect the argument data types for the
 * ComponentContext.* methods inside the ExecutableComponent.execute 
 * function
 * 
 * @author Amit Kumar
 * Created on Apr 13, 2008 9:37:07 AM
 * -modified on June 1st -support for MethodInsnNode in getInputMethodData
 *
 */
public class ComponentExecuteMethodTransformer extends ClassTransformer {
	boolean isTransormed = Boolean.FALSE;
	
	private HashMap<String,MethodDataType> componentDataTypeHashMap;
	private HashMap<String,String> classFieldsDataTypeHashMap;
	private HashMap<String,Object> classFieldsDataValueHashMap;
	
	TreeMap<Integer,String> localVarMap = new TreeMap<Integer,String>();
	TreeMap<String,String> localVarMapDataType =new TreeMap<String,String>();
	
	
	
	
	public ComponentExecuteMethodTransformer(ClassTransformer ct) {
		super(ct);
		componentDataTypeHashMap = new HashMap<String,MethodDataType>(4);
		classFieldsDataTypeHashMap = new HashMap<String,String>(4);
		classFieldsDataValueHashMap = new HashMap<String,Object>(4);

	}
	
	
	@Override 
	public void transform(ClassNode cn) { 
		if(cn.fields!=null){
			Iterator itf=cn.fields.iterator();
			FieldNode fn=null;
			while(itf.hasNext()){
				fn = (FieldNode)itf.next();
				classFieldsDataTypeHashMap.put(fn.name, fn.desc);
			}
		}
		Iterator i = cn.methods.iterator(); 
		while (i.hasNext()) { 
		MethodNode mn = (MethodNode) i.next(); 
		
		if("<clinit>".equals(mn.name) ||"<init>".equals(mn.name) ){
			getFieldValues(mn);
		}
	
		if (("handle".equals(mn.name)) || ("execute".equals(mn.name)) || ("emptyRequest".equals(mn.name)) ) {
			
			isTransormed = Boolean.TRUE;
			Iterator itv=mn.localVariables.iterator();
			LocalVariableNode lvn=null;
			System.out.println("FUNCTION: " + mn.name);
			while(itv.hasNext()){
			lvn=	((LocalVariableNode)itv.next());
				//System.out.println("Loc var: "+lvn.index +" "+lvn.name + "  "+ lvn.desc + "  "+ lvn.signature + "  "
				//		+((LabelNode)lvn.start).getLabel() + "  "+ lvn.end);
				localVarMap.put(lvn.index, lvn.name);
				localVarMapDataType.put(lvn.name, lvn.desc);
			}
			
			//System.out.println("===> " + mn.instructions.size() +"  " + mn.localVariables.size());
			
			InsnList il = mn.instructions;
			ListIterator lit=il.iterator();
			
			while(lit.hasNext()){
				AbstractInsnNode in = (AbstractInsnNode) lit.next(); 
				int op = in.getOpcode();
				//System.out.println(in+" op is: " + op +"  " + AbstractInsnNode.METHOD_INSN);
				if(in instanceof MethodInsnNode ){
					MethodInsnNode min=(MethodInsnNode )in;
					if(min.owner.equals("org/meandre/core/ComponentContext") && 
							min.name.equals("pushDataComponentToOutput")){
						/* All methods that are called on the ComponentContext Object*/
						MethodDataType mdt = getOutputMethodData(min);
						if(mdt!=null){
						componentDataTypeHashMap.put(mdt.getArg1()+"_" +mdt.getMethodName(), mdt);
						}
					
					}else if(min.owner.equals("org/meandre/core/ComponentContext") && 
							min.name.equals("getDataComponentFromInput")){
						@SuppressWarnings("unused")
						MethodDataType mdt = getInputMethodData(min,"getDataComponentFromInput");
						if(mdt!=null){
						componentDataTypeHashMap.put(mdt.getArg1()+"_" +mdt.getMethodName(), mdt);
						}
					}
					
					
				}
				

			}
			
			
			
		} 
		
		
		
		} 
		super.transform(cn); 
		}


	
	
	
	
	/**Looks for the input method data types
	 * 
	 * @param min
	 * @param methodName
	 * @return
	 */
	private MethodDataType getInputMethodData(MethodInsnNode min, String methodName) {
	String argname=getArgument1Name(min);
	System.out.println(min.desc + " " + min.name+ " " + argname);
	AbstractInsnNode in=	min.getNext();
	//System.out.println(in);
	MethodDataType mdt = new MethodDataType();
	mdt.setMethodName(methodName);
	mdt.setArg1(argname);
//	/mdt.setVariableName(variableName)
	
	if(in instanceof VarInsnNode){
		VarInsnNode vin = (VarInsnNode)in;
		//System.out.println("VarInsnNode: "+ vin.var+ "  " + vin.getOpcode());
		int k=0;
		ArrayList<String> castDataType= getCast(in, vin.var);
		String variableDataType = "";
		if(castDataType.size()!=0){
		for(int i=0; i < castDataType.size(); i++){
			if(i==0){
				variableDataType = castDataType.get(i);
			}else{
				variableDataType= variableDataType+","+	castDataType.get(i);
			}
			//System.out.println("VarInsnNode ====>"+ castDataType.get(i));
		}
		}else{
			variableDataType =  Type.getReturnType(min.desc).toString();
		}
		mdt.setVariableDataType(variableDataType);
		mdt.setVariableName(getNthKey(vin.var));
		
		}else if(in instanceof InsnNode){
		InsnNode isn = (InsnNode) in;
		//System.out.println("InsnNode: "+ isn.getType()+ "  " + isn.getOpcode());
		mdt.setVariableDataType( Type.getReturnType(min.desc).toString());
	}else if(in instanceof TypeInsnNode){
		TypeInsnNode tin = (TypeInsnNode)in;
		//System.out.println("Know the DataType: " +tin.desc);
		mdt.setVariableDataType(tin.desc);
	}else if(in instanceof MethodInsnNode){
		MethodInsnNode  methodNode = (MethodInsnNode)in;
		mdt.setVariableDataType(methodNode.desc);
	}
		return mdt;
	}

	/** This function goes down the nodes looking for the 
	 *  CHECKCAST that may have been applied to the variable defined
	 *  by the vartype
	 * 
	 * @param isn
	 * @param vartype
	 * @return ArrayList of the variable types
	 */
	private ArrayList<String> getCast(AbstractInsnNode isn, int vartype) {
		AbstractInsnNode in = isn.getNext();
		ArrayList<String> alist = new ArrayList<String>(2);
		while(in.getNext()!=null){
			if(in instanceof TypeInsnNode){
				TypeInsnNode tin = (TypeInsnNode)in;
				//System.out.println(tin.desc + " " + vartype);
				AbstractInsnNode intmp = tin.getPrevious();
				if(intmp instanceof VarInsnNode){
					if(((VarInsnNode)intmp).var == vartype){
						alist.add(tin.desc);
					}
				}
			}else{
			//	System.out.println("process: "+ in.toString());
			}
			in = in.getNext();
		}
		return alist;
	}


	/**Returns MethodDataType  for the method
	 * @param min
	 * @return
	 */
	private MethodDataType getOutputMethodData(MethodInsnNode min) {
		String methodName = min.name;
		String methodParamDataType= null;
		String paramName = null;
		//System.out.println(min.name + " " + min.desc + " " +min.owner );	
		AbstractInsnNode prev=	min.getPrevious();
		String arg1Name = getArgument1Name(min);
		//System.out.println("arg1 name: " + arg1Name);
		if(prev instanceof VarInsnNode){
		VarInsnNode vin= (VarInsnNode)prev;
		//System.out.println(vin.var + " " +vin.getType()+ " " + vin.getOpcode() +" " +vin.toString());	
		methodParamDataType = getNthValue(vin.var);
		paramName = getNthKey(vin.var);
		//System.out.println("VarInsnNode: "+paramName + "   " + methodParamDataType);	
		}else if( prev instanceof MethodInsnNode){
			MethodInsnNode mins = (MethodInsnNode)prev;
			//System.out.println(mins.name + " " + mins.owner+ " here: "+mins.desc+ "-----" + Type.getType(mins.desc));
			methodParamDataType = mins.desc;
			paramName=mins.name;
		//	System.out.println("MethodInsnNode: "+paramName + "   " + methodParamDataType);
		}else if(prev instanceof LdcInsnNode){
			 LdcInsnNode lin = ( LdcInsnNode)prev;
			// System.out.println(lin.getType() + " " + lin.cst);
			 methodParamDataType = lin.cst.toString();
			 paramName="constant";
			// System.out.println("LdcInsnNode: "+paramName + "   " + methodParamDataType);
			 
		}else if(prev instanceof FieldInsnNode){
			FieldInsnNode fin = (FieldInsnNode) prev;
			//System.out.println(fin.name+" "+ fin.owner+" "+fin.desc);
			methodParamDataType = fin.desc;
			paramName=fin.name;
		//	System.out.println("FieldInsnNode: "+ paramName + "   " + methodParamDataType);	
		}else {
			System.out.println("prev is: " + prev);
		}
		
		MethodDataType mdt = new MethodDataType();
		mdt.setArg1(arg1Name);
		mdt.setMethodName(methodName);
		mdt.setVariableDataType(methodParamDataType);
		mdt.setVariableName(paramName);
		return mdt;
	}


	private void getFieldValues(MethodNode mn) {
	InsnList insnList=	mn.instructions;
	ListIterator itlist=insnList.iterator();
	AbstractInsnNode in=null;
	Object value=null;
	while(itlist.hasNext()){
	in = (AbstractInsnNode) itlist.next();
	
	if(in instanceof  LdcInsnNode){
	LdcInsnNode lin = (LdcInsnNode)in;
	value=	lin.cst;
	}else if(value != null && in instanceof FieldInsnNode){
	FieldInsnNode fin = (FieldInsnNode)in;
	//System.out.println("Getting Field name Value: " + fin.name + "  " + value);
	classFieldsDataValueHashMap.put(fin.name, value);
	value=null;
	}
	}
	}


	private String getArgument1Name(MethodInsnNode min) {
		/*Starting from the method node go up looking for the ALOAD on componentcontext*/
		boolean notfound= true;
		AbstractInsnNode in =min;
		AbstractInsnNode componentContextNode=null;
		String argumentName=null;
		while(in.getPrevious()!=null && notfound){
			System.out.println(in);
			if(in instanceof VarInsnNode){
				int varindex= ((VarInsnNode)in).var;
				if( getNthValue(varindex).equals("Lorg/meandre/core/ComponentContext;")){
				//	System.out.println("FOUND THE ComponentContext node ");
					notfound= Boolean.FALSE;
					componentContextNode =  ((VarInsnNode)in);
					break;
				}
				
			}else if(in instanceof LdcInsnNode){
				
			}else if(in instanceof FieldInsnNode){
				FieldInsnNode fin = (FieldInsnNode)in;
				String fieldName = fin.name;
				
				if(fin.desc.equals("Lorg/meandre/core/ComponentContext;")){
						notfound= Boolean.FALSE;
						componentContextNode =  (FieldInsnNode)in;
						break;
				}
			}
			
			in = in.getPrevious();
		}
		
		if(componentContextNode!=null){
			in = componentContextNode.getNext();
			if(in instanceof LdcInsnNode){
				argumentName=((LdcInsnNode)in).cst.toString();
			}else if(in instanceof VarInsnNode){
				int varindex= ((VarInsnNode)in).var;
				argumentName=getNthKey(varindex);
			}else if(in instanceof FieldInsnNode){
				FieldInsnNode fin = ((FieldInsnNode)in);
				argumentName = classFieldsDataValueHashMap.get(fin.name).toString();
				
			}else{
				System.out.println("ARG LOOKING: " + in);
			}
		}
		
		
		return argumentName;
	}


	private String getNthKey(int var) {
	String value=  localVarMap.get(var);
	return	value;
	}


	private String getNthValue(int var) {
	String key=  localVarMap.get(var);
	if(key==null){
		return null;
	}
	return localVarMapDataType.get(key);
	}


	/**
	 * @return the componentDataTypeHashMap
	 */
	public HashMap<String, MethodDataType> getComponentDataTypeHashMap() {
		return componentDataTypeHashMap;
	}





}
