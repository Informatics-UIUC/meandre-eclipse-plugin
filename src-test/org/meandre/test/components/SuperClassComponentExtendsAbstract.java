/*
 * @(#) SuperClassComponentExtendsConcrete.java @VERSION@
 * 
 * Copyright (c) 2009+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.test.components;

import org.meandre.annotations.ComponentInput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;

public class SuperClassComponentExtendsAbstract extends BaseAbstractClassComponent{
	@ComponentInput(description="input value2", name="INPUT2")
	public final static String  DATA_IN_2="INPUT2";
	

	
	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {
		String in1=(String)cc.getDataComponentFromInput(DATA_IN_1);
		String in2=(String)cc.getDataComponentFromInput(DATA_IN_2);
		
		String propertyValue = cc.getProperty(DATA_PROP_1);
		System.out.println(in1 + " -- " + propertyValue +  " -- " + in2);
		cc.pushDataComponentToOutput(DATA_OUT_1, "Hello World from SuperClass");	
	}
	
}
