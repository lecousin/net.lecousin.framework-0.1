package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.button.ButtonStyle;
import net.lecousin.framework.ui.eclipse.control.button.HoverStyle;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LabelItem extends RoundedComposite {

	/**
	 * 
	 * @param parent
	 * @param text
	 * @param color
	 * @param removeListener if null, no remove button neither separator will be shown
	 * @param separator
	 */
	public LabelItem(Composite parent, String text, Color color, Listener<LabelItem> removeListener, boolean separator) {
		super(parent, SWT.NONE, color, parent.getBackground());
		GridLayout layout = UIUtil.gridLayout(this, removeListener != null ? separator ? 3 : 2 : 1);
		layout.horizontalSpacing = 3;
		layout.marginHeight = 1;
		layout.marginWidth = 2;
		
		Color color2 = ColorUtil.darkerOrClearer(color, 80);
		
		Label label = new Label(this, SWT.NONE);
		label.setText(text);
		label.setBackground(color);
		if (removeListener != null) {
			if (separator) {
				Separator sep = new Separator(this, false, Separator.Style.SIMPLE_LINE, 2);
				sep.setBackground(color);
				sep.setForeground(color2);
				sep.setLayoutData(UIUtil.gridDataVert(1, true));
			}
			SimpleCrossButton button = new SimpleCrossButton(this, new ButtonStyle().border_normal(BorderStyle.NONE).border_push(BorderStyle.NONE).border_hover(BorderStyle.NONE).hover(HoverStyle.BOLD), 2);
			button.addClickListener(new ListenerData<SimpleCrossButton,Listener<LabelItem>>(removeListener) {
				public void fire(SimpleCrossButton event) {
					data().fire(LabelItem.this);
				}
			});
			button.setBackground(color);
			GridData gd = new GridData();
			gd.verticalAlignment = SWT.CENTER;
			gd.widthHint = 10;
			gd.heightHint = 10;
			button.setLayoutData(gd);
		}
	}

}
