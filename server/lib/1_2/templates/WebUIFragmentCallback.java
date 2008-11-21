package _PACKAGE_;

import org.meandre.core.ExecutableComponent;
import org.monkproject.meandre.annotations.Component;
import org.monkproject.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import javax.servlet.http.HttpServletResponse;
import org.meandre.webui.WebUIException;
import javax.servlet.http.HttpServletRequest;
import org.meandre.webui.WebUIFragmentCallback;

import org.monkproject.meandre.annotations.ComponentInput;


/**
 * @author Author
 */

@Component(creator="author",
           description="description of the component",
           name="_CLASS_",
           tags="tag1 tag2 tag3")

public final class _CLASS_  implements ExecutableComponent, WebUIFragmentCallback {
    @ComponentProperty(defaultValue="hello world",
                       description=" ",
                       name="message)
    final static String DATA_PROPERTY= "message";

    /** The blocking semaphore */
    private Semaphore sem = new Semaphore(1, true);

    /** The instance ID */
    private String sInstanceID = null;

    private String message = "testing";


    /** This method gets call when a request with no parameters is made to a
     * component WebUI fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem encountered during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException {
        try {
            response.getWriter().println(getViz());
        } catch (IOException e) {
            throw new WebUIException(e);
        }
    }

    /** A simple message.
     *
     * @return The HTML containing the page
     */
    private String getViz() {
        StringBuffer sb = new StringBuffer();
        sb.append("<html>\n");
        sb.append("<body>\n");
        sb.append("<p ALIGN=center >\n"+message);
        sb.append("</p>\n");
        sb.append("<br /><br />\n");
        sb.append("<div align=\"center\">\n ");
        sb.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
                  sInstanceID + "?done=true\">DONE</a></font></table>\n");
        sb.append("</div>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        return sb.toString();
    }


    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem occurred during the call back
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws
            WebUIException {
        String sDone = request.getParameter("done");
        if (sDone != null)
            sem.release();
        else
            emptyRequest(response);
    }

    /** When ready for execution.
     *
     * @param cc The component context
     * @throws ComponentExecutionException An exeception occurred during execution
     * @throws ComponentContextException Illigal access to context
     */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
            ComponentContextException {
        message = (String)cc.getProperty(DATA_PROPERTY);



        sInstanceID = cc.getExecutionInstanceID();

        try {
            sem.acquire();
            cc.startWebUIFragment(this);
            sem.acquire();
            sem.release();
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }

        cc.stopWebUIFragment(this);
        System.out.flush();
    }

    public void initialize(ComponentContextProperties ccp) {}
    public void dispose(ComponentContextProperties ccp) {}
}
