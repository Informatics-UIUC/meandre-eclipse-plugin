/*
 * @(#) BaseClassComponent.java @VERSION@
 * 
 * Copyright (c) 2009+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.test.components;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Tester", description="This is a test base component", 
		name="BaseClassComponent", 
		tags="Here is a base component")
public class BaseConcreteClassComponent implements ExecutableComponent {

	@ComponentInput(description="input value1", name="INPUT1")
	protected
	final static String  DATA_IN_1="INPUT1";
	
	@ComponentInput(description="input value2", name="INPUT2_ARRAY")
	protected
	final static String  DATA_IN_2="INPUT2_ARRAY";
	
	
	@ComponentOutput(description="output value1", name="OUTPUT1")
	protected final static String  DATA_OUT_1="OUTPUT1";
	
	@ComponentProperty(defaultValue="propValue1", description="property value one", name="PROP1")
	protected final static String DATA_PROP_1 ="PROP1";
	
	
	public void dispose(ComponentContextProperties ccp) {
		// TODO Auto-generated method stub

	}

	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {
	String in=(String)cc.getDataComponentFromInput(DATA_IN_1);
	String inArray[]=(String[])cc.getDataComponentFromInput(DATA_IN_2);
	String propertyValue = cc.getProperty(DATA_PROP_1);
	System.out.println(in + " -- " + propertyValue + inArray.length);
	cc.pushDataComponentToOutput(DATA_OUT_1, "Hello World from Base Concrete class");	
	}

	public void initialize(ComponentContextProperties ccp) {
		// TODO Auto-generated method stub

	}

}
