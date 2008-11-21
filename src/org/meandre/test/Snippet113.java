/*
 * @(#) Snippet113.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.test;

/*
 * Table example snippet: detect a selection or check event in a table (SWT.CHECK)
 *
 * For a list of all SWT example snippets see
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/dev.html#snippets
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class Snippet113 {

  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    Table table = new Table(shell, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
        | SWT.H_SCROLL);
    for (int i = 0; i < 12; i++) {
      TableItem item = new TableItem(table, SWT.NONE);
      item.setText("Item " + i);
    }
    table.setSize(100, 100);
    table.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        String string = event.detail == SWT.CHECK ? "Checked"
            : "Selected";
        System.out.println(event.item + " " + string);
      }
    });
    shell.setSize(200, 200);
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }
}

      