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
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ExecutableComponent;

@Component(creator="Tester", description="This is a test base component", name="BaseClassComponent", tags="Here is a base component")
public abstract class BaseAbstractClassComponent implements ExecutableComponent {

	@ComponentInput(description="input value1", name="INPUT1")
	public final static String  DATA_IN_1="INPUT1";
	
	@ComponentOutput(description="output value1", name="OUTPUT1")
	public final static String  DATA_OUT_1="OUTPUT1";
	
	@ComponentProperty(defaultValue="propValue1", description="property value one", name="PROP1")
	public final static String DATA_PROP_1 ="PROP1";
	
	
	public void dispose(ComponentContextProperties ccp) {
		// TODO Auto-generated method stub

	}

	

	public void initialize(ComponentContextProperties ccp) {
		// TODO Auto-generated method stub

	}

}
