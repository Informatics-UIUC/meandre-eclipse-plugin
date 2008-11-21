/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.webapp.proxy.beans.repository;

import java.util.HashSet;
import java.util.Set;

/** Suports tagging for components.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class TagsDescription {

	/** The set of tags linked to a component */
	private Set<String> setTags = null;

	/** Creates a tag description object.
	 * 
	 * @param setTags The set of tags 
	 */
	public TagsDescription ( Set<String> setTags ) {
		this.setTags = setTags;
	}
	
	/** Creates an empty tag description object.
	 * 
	 * 
	 */
	public TagsDescription ( ) {
		this.setTags = new HashSet<String>();
	}
	
	/** Returns the set of tags.
	 * 
	 * @return The set of tags
	 */
	public Set<String> getTags () {
		return setTags;
	}
	
	/** Returns a stringfied version of the tags.
	 * 
	 * @return The tag list
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		for ( String sTag:setTags )
			sb.append(sTag+" ");
		
		return sb.toString().trim();
	}
}
