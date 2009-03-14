package net.lecousin.framework.ui.eclipse.control.text;

import net.lecousin.framework.ui.eclipse.control.UIControlUtil;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class AutoresizeText extends Text {

    public AutoresizeText(Composite parent, int style) {
        super(parent, style);
        addModifyListener(new ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                if (lastText.equals(getText())) {
                    setSelection(lastPosition);
                    return;
                }
                int i = getCaretPosition();
                UIControlUtil.autoresize(getParent());
                setSelection(0);
                setSelection(i);
                lastText = getText();
                lastPosition = i;
            }
        });
    }
    
    private String lastText = "";
    private int lastPosition;

    protected void checkSubclass() {
        // allow subclassing
    }
    
    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point pt = super.computeSize(wHint, hHint, changed);
        if (getText().length() == 0)
            pt.x = 10;
        else
            pt.x += 8;
        return pt;
    }
}
