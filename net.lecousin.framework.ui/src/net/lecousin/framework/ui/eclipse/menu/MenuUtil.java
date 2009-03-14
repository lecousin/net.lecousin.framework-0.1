package net.lecousin.framework.ui.eclipse.menu;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class MenuUtil
{
  public static void addAction(Menu menu, IAction action) {
    MenuItem item = new MenuItem(menu, SWT.NONE);
    item.setText(action.getText());
    item.addSelectionListener(new ActionMenuListener(action));
  }
  private static class ActionMenuListener implements SelectionListener {
    ActionMenuListener(IAction action) {
      this.action = action;
    }
    private IAction action;
    public void widgetDefaultSelected(SelectionEvent e)
    {
      // not used
    }
    public void widgetSelected(SelectionEvent e)
    {
      action.run();
    }
  }
}
