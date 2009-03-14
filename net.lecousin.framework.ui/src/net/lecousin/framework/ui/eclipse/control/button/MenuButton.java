package net.lecousin.framework.ui.eclipse.control.button;

import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class MenuButton extends Composite {

	/** image and/or text may be null */
	public MenuButton(Composite parent, Image image, String text, boolean showArrow, MenuProvider menuProvider) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 2;
		layout.numColumns = 0;
		if (image != null) layout.numColumns++;
		if (text != null) layout.numColumns++;
		if (showArrow) layout.numColumns+=2;
		setLayout(layout);
		
		if (image != null)
			UIUtil.newImage(this, image);
		if (text != null)
			UIUtil.newLabel(this, text);
		if (showArrow) {
			UIUtil.newSeparator(this, false, true);
			Canvas canvas = new Canvas(this, SWT.NONE);
			canvas.setBackground(getBackground());
			GridData gd = new GridData();
			gd.widthHint = 9;
			gd.heightHint = 6;
			gd.verticalAlignment = SWT.CENTER;
			canvas.setLayoutData(gd);
			canvas.addPaintListener(new ArrowPainter());
		}
		
		ButtonStyle style = new ButtonStyle();
		new ButtonStyleApply(this, style);
		UIControlUtil.recursiveMouseListener(this, new ButtonMouseListener(menuProvider), true);
	}
	
	public static interface MenuProvider {
		public String getTitle();
		public void fill(FlatPopupMenu menu);
	}
	
	private void showMenu(MenuProvider provider) {
		FlatPopupMenu dlg = new FlatPopupMenu(this, provider.getTitle(), false, true, false, true);
		provider.fill(dlg);
		dlg.show(this, FlatPopupMenu.Orientation.BOTTOM, true);
	}
	
	private static class ArrowPainter implements PaintListener {
		public void paintControl(PaintEvent e) {
			Point size = ((Canvas)e.widget).getSize();
			e.gc.setForeground(ColorUtil.get(0, 0, 60));
			int y = size.y/2-1;
			e.gc.drawLine(1, y, 7, y);
			e.gc.drawLine(2, y+1, 6, y+1);
			e.gc.drawLine(3, y+2, 5, y+2);
			e.gc.drawPoint(4, y+3);
		}
	}
	
	private class ButtonMouseListener implements MouseListener {
		ButtonMouseListener(MenuProvider menuProvider) { this.menuProvider = menuProvider; }
		private MenuProvider menuProvider;
		public void mouseDoubleClick(MouseEvent e) {
		}
		public void mouseDown(MouseEvent e) {
		}
		public void mouseUp(MouseEvent e) {
			if (e.button == 1) {
				showMenu(menuProvider);
			}
		}
	}
}
