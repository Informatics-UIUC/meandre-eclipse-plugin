 /** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */

package org.meandre.ide.eclipse.component.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.meandre.ide.eclipse.component.Activator;
import org.meandre.server.MeandreEngineServicesConstants;

/**
 * @author Amit Kumar
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_OVERWRITE, true);
		store.setDefault(PreferenceConstants.P_EMBED, false);
		store.setDefault(PreferenceConstants.P_PACKAGE_COMPONENT_AS_JAR, true);
		store.setDefault(PreferenceConstants.P_USE_COMPONENT_VERSIONING, true);
		store.setDefault(PreferenceConstants.P_PING_SERVER, false);
		store.setDefault(PreferenceConstants.P_HAS_ASPECT_J, false);
		store.setDefault(PreferenceConstants.P_INCLUDE_SOURCE, true);
		store.setDefault(PreferenceConstants.P_CONTINUE_WITH_MISSING_RESOURCE, false);
		
		store.setDefault(PreferenceConstants.P_CREATE_PACKAGE_PATH, true);
		//supported only in recent servers 1.4.7 and beyond
		store.setDefault(PreferenceConstants.P_SEND_JARS_THAT_CHANGED, true);
		
		
		store.setDefault(PreferenceConstants.P_SERVER, MeandreEngineServicesConstants.DEFAULT_SERVER);
		store.setDefault(PreferenceConstants.P_PORT, MeandreEngineServicesConstants.DEFAULT_PORT);
		store.setDefault(PreferenceConstants.P_LOGIN, MeandreEngineServicesConstants.DEFAULT_LOGIN);
		store.setDefault(PreferenceConstants.P_PASSWORD, MeandreEngineServicesConstants.DEFAULT_PASSWORD);
		store.setDefault(PreferenceConstants.P_DESC_DIR, System.getProperty("user.home"));
		store.setDefault(PreferenceConstants.P_FILTERJAR, "meandre*,icu*,driver*,servlet-api*," +
				"jarjar*,jetty*,jsp-api*,icu4j*");
		
	
		
		
		store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
		store.setDefault(PreferenceConstants.P_STRING,
				"Default value");	}

}
