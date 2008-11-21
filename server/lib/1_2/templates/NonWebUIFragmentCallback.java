package _PACKAGE_;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.monkproject.meandre.annotations.Component;
import org.monkproject.meandre.annotations.ComponentOutput;
import org.monkproject.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextProperties;

/* This sample executable component pushes a string to the output
 *
 * @author 
 *
 */
@Component(
	    creator="Author",
	    description="Pushes the string specified in the property to the output",
	    name="_CLASS_",
	    tags="push string")
public class _CLASS_ implements ExecutableComponent {

	@ComponentProperty(description = "String to be pushed", name = "string", defaultValue = "hello world")
	final static String DATA_PROPERTY_STRING = "string";

	@ComponentOutput(description = "Output string", name = "output_string")
	final static String DATA_OUTPUT_OUTPUT_STRING = "output_string";

	public void initialize(ComponentContextProperties ccp) {
	}

	public void execute(ComponentContext context)
		throws ComponentExecutionException, ComponentContextException {

		String strInput = context.getProperty(DATA_PROPERTY_STRING);
		context.pushDataComponentToOutput(DATA_OUTPUT_OUTPUT_STRING, strInput);
	}

	public void dispose(ComponentContextProperties ccp) {
	}

}
