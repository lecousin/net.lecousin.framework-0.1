package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.strings.StringUtil;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class DoubleEdit extends Text implements ModifyListener {

	public DoubleEdit(Composite parent, int style, double min, double max, int nbDigits, double initialValue) {
		super(parent, style);
		this.min = min;
		this.max = max;
		this.value = initialValue;
		this.nbDigits = nbDigits;
		setText(StringUtil.toStringSep(value, nbDigits));
		addModifyListener(this);
	}

	private double min, max;
	private double value;
	private int nbDigits;
	
	@Override
	protected void checkSubclass() {
	}
	
	public void modifyText(ModifyEvent e) {
		String str = getText();
		int i = str.indexOf(',');
		long lv, dv;
		if (i < 0) {
			try { lv = Long.parseLong(str); } catch(NumberFormatException ex) { lv = 0; }
			dv = 0;
		} else if (i == 0) {
			lv = 0;
			try { dv = str.length() == 1 ? 0 : Long.parseLong(str.substring(1)); } catch(NumberFormatException ex) { dv = 0; }
		} else {
			try { lv = Long.parseLong(str.substring(0,i)); } catch(NumberFormatException ex) { lv = 0; }
			if (str.length() == i+1)
				dv = 0;
			else if (str.length() == i+2)
				try { dv = Long.parseLong(str.substring(i+1))*10; } catch(NumberFormatException ex) { dv = 0; }
			else
				try { dv = Long.parseLong(str.substring(i+1, i+3)); } catch(NumberFormatException ex) { dv = 0; }
		}
		double v = (double)lv + ((double)dv/100);
		if (v < min) v = min;
		if (v > max) v = max;
		str = StringUtil.toStringSep(v, nbDigits);
		if (!str.equals(getText())) setText(str);
	}
	
	public double getValue() { return value; }
}
