package net.lecousin.framework.ui.eclipse.draw2d;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;

public class Draw2DAdvancedMouseListener {
	
	public void connect(IFigure fig) {
		fig.addMouseListener(mouseListener);
	}
	
	protected boolean doubleClick(IFigure figure, int x, int y, int button) {
		return false;
	}
	protected boolean mousePressed(IFigure figure, int x, int y, int button) {
		return false;
	}
	protected boolean mouseReleased(IFigure figure, int x, int y, int button) {
		return false;
	}
	protected boolean click(IFigure figure, int x, int y, int button) {
		return false;
	}
	
	private IFigure mousePressedFigure;
	private int mousePressedX;
	private int mousePressedY;
	private int mousePressedButton;
	private MouseListener mouseListener = new MouseListener() {
		public void mouseDoubleClicked(MouseEvent me) {
			doubleClick((IFigure)me.getSource(), me.x, me.y, me.button);
		}
		public void mousePressed(MouseEvent me) {
			mousePressedFigure = (IFigure)me.getSource();
			mousePressedX = me.x;
			mousePressedY = me.y;
			mousePressedButton = me.button;
			Draw2DAdvancedMouseListener.this.mousePressed((IFigure)me.getSource(), me.x, me.y, me.button);
		}
		public void mouseReleased(MouseEvent me) {
			if (Draw2DAdvancedMouseListener.this.mouseReleased((IFigure)me.getSource(), me.x, me.y, me.button))
				me.consume();
			else if (Draw2DAdvancedMouseListener.this.click((IFigure)me.getSource(), me.x, me.y, me.button))
				me.consume();
		}
	};
}
