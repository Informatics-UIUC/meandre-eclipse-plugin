/*
 * @(#) TestListener.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.ide.eclipse.component.wizard.dependency;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class TestListener implements Listener {

	public void handleEvent(Event event) {
		System.out.println("HERE IS EVENT: " + event);

	}

}
