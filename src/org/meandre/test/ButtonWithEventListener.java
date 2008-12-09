package org.meandre.test;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class ButtonWithEventListener {
  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setLayout(new GridLayout());

    Button button = new Button(shell, SWT.NONE);
    button.setText("Click and check the console");
    button.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        switch (e.type) {
        case SWT.Selection:
          System.out.println("Button pressed");
          break;
        }
      }
    });

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
  }
}