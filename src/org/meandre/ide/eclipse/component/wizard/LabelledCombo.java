/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */

package org.meandre.ide.eclipse.component.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * 
 * @author Amit Kumar
 * Created on Jun 5, 2008 10:23:10 PM
 *
 */
public class LabelledCombo {
    private Combo text;

    public LabelledCombo(Composite parent, int style, String labelText,String defaultText, String[] options) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(labelText);
        text = new Combo(parent, SWT.READ_ONLY|SWT.DROP_DOWN|SWT.SINGLE | SWT.BORDER);
        text.setItems(options);
        
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 200;
        text.setLayoutData(data);
    }
    
    public Combo getText() {
        return text;
    }
}
