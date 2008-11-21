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
import java.util.Set;

/** This is a wrapper for properties definition and the description obtained from
 * the metadata.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class PropertiesDescription {

	/** The property key. */
	private Hashtable<String,String> htValues = null;
	
	/** Create an empty property description.
	 * 
	 * @param htValues The values
	 */
	public PropertiesDescription () {
		this.htValues = new Hashtable<String,String>();
	}
	
	/** Create a property description.
	 * 
	 * @param htValues The values
	 */
	public PropertiesDescription ( Hashtable<String,String> htValues ) {
		this.htValues = htValues;
	}
	
	/** Returns the keys of the properties.
	 * 
	 * @return The keys
	 */
	public Set<String> getKeys () {
		return htValues.keySet();
	}
	
	/** Returns the values of the property.
	 * 
	 * @return The values
	 */
	public Collection<String> getValues () {
		return htValues.values();
	}
	
	/** Get the value for a given property value.
	 * 
	 * @param sKey The key of the property to retrieve
	 * @return The value
	 */
	public String getValue ( String sKey ) {
		return htValues.get(sKey);
	}
	
	/** Gets a map for the property values.
	 * 
	 * @return The value map
	 */
	public Map<String,String> getValueMap () {
		return htValues;
	}

	/** Add a property to the properties.
	 * 
	 * @param sKey The key
	 * @param sValue The value
	 */
	public void add ( String sKey, String sValue ) {
		htValues.put(sKey, sValue);
	}
	

	/** Remove a property from the properties.
	 * 
	 * @param sKey The key
	 */
	public void remove ( String sKey ) {
		htValues.remove(sKey);
	}
}
