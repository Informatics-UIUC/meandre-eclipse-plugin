/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.webapp.proxy.beans.repository;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/** The definition of a property description.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class PropertiesDescriptionDefinition extends PropertiesDescription {

	/** The property description. */
	private Hashtable<String,String> htDescriptions = null;
	
	/** Create a property description definition.
	 * 
	 * @param htValues The values
	 * @param htDescriptions The descriptions
	 */
	public PropertiesDescriptionDefinition(Hashtable<String,String> htValues, Hashtable<String,String> htDescriptions) {
		super(htValues);
		this.htDescriptions = htDescriptions;
	}

	
	/** Returns the description of the property.
	 * 
	 * @return The descriptions of the stored properties
	 */
	public Collection<String> getDescriptions () {
		return htDescriptions.values();
	}
	
	/** Return the description for a given key.
	 * 
	 * @param sKey The key of the property to retrieve
	 * @return The description value
	 */
	public String getDescription ( String sKey ) {
		return htDescriptions.get(sKey);
	}
	
	/** Returns the description map
	 * 
	 * @return The description map
	 */
	public Map<String,String> getDescriptionMap () {
		return htDescriptions;
	}
}
