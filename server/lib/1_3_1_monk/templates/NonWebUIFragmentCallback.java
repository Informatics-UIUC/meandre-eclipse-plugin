package _PACKAGE_;

import java.util.Random;
import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

/** This executable component just pushes a string to the output.
 *
 * @author Joe
 *
 */
@Component(creator="Joe", description="Sample Component", 
		name="MyComponent",
		tags="test print hello")
public class _CLASS_ implements ExecutableComponent {
	
	@ComponentProperty(defaultValue="5", description="number of iteration", name="repeat")
	private static final String DATA_PROP_1 = "repeat";
	
	@ComponentOutput(description="ouput message", name="message")
	private static final String DATA_OUT_1 = "message";
	
	
	private Logger _logger;

	public void initialize(ComponentContextProperties ccp)
	throws ComponentExecutionException, ComponentContextException {
		this._logger = ccp.getLogger();
	}	
	
	
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {
		String _repeat = cc.getProperty(DATA_PROP_1);
		Integer repeat = Integer.parseInt(_repeat);
		_logger.info("In execute: " + repeat );
		for(int i=0; i < repeat; i++)
		cc.pushDataComponentToOutput(DATA_OUT_1, "Hello World!");
	}

	public void dispose(ComponentContextProperties ccp)
	throws ComponentExecutionException, ComponentContextException {
		_logger.info("disposing "+ this.getClass().getName());

	}

	

}
