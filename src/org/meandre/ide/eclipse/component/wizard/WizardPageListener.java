/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.wizard;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * @author Joni Suominen
 */
public interface WizardPageListener {
    void pageEntered(IWizardPage page);
    void pageExited(IWizardPage page);
}
