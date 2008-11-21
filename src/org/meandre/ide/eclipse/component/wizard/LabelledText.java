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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** -based on WicketIDE
 * @author Joni Freeman
 */
public class LabelledText {
    private Text text;

    public LabelledText(Composite parent, int style, String labelText, String defaultText) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(labelText);
        text = new Text(parent, style);
        text.setText(defaultText);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 200;
        text.setLayoutData(data);
    }
    
    public Text getText() {
        return text;
    }
}
