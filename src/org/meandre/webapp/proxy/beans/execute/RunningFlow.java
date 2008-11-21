/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.webapp.proxy.beans.execute;

/** The information about a running flow.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class RunningFlow {

	/** The flow ID */
	private String sFlowID;
	
	/** The webui link */
	private String sWebUIURL;
	
	/** Create a bean with the information about a running flow.
	 * 
	 * @param sID The flow ID
	 * @param sURL The webUI URL
	 */
	public RunningFlow ( String sID, String sURL ) {
		sFlowID = sID;
		sWebUIURL = sURL;
	}
	
	/** Gets the flow ID.
	 * 
	 * @return The flow ID
	 */
	public String getID () {
		return sFlowID;
	}
	
	/** Returns the web UI URL
	 * 
	 * @return The web UI URL
	 */
	public String getWebUIURL ()  {
		return sWebUIURL;
	}
}
