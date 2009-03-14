package net.lecousin.framework.ui.eclipse.control.list;

import java.util.List;

import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LabelButton;
import net.lecousin.framework.ui.eclipse.control.list.LCTableWithControls.Provider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class DoubleListControl<T> extends Composite {

	/** list1Name and list2Name can be null */
	public DoubleListControl(Composite parent, 
			Provider<T> provider1, String list1Name, boolean list1Orderable,
			Provider<T> provider2, String list2Name, boolean list2Orderable
			) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		UIUtil.gridLayout(this, 3);
		
		this.lists[0] = new LCTableWithControls<T>(this, list1Name, provider1, true, list1Orderable, false, false);
		createMove();
		this.lists[1] = new LCTableWithControls<T>(this, list2Name, provider2, false, list2Orderable, false, false);
	}

	@SuppressWarnings("unchecked")
	private LCTableWithControls<T>[] lists = new LCTableWithControls[2];
	
	private void createMove() {
		Composite panel = UIUtil.newGridComposite(this, 0, 0, 1);
		panel.setLayoutData(UIUtil.gridDataVert(1, true));
		LabelButton button;
		button = UIUtil.newImageButton(panel, SharedImages.getImage(SharedImages.icons.x16.arrows.LEFT), new Listener<Object>() {
			public void fire(Object event) {
				left();
			}
		}, null);
		button.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		button = UIUtil.newImageButton(panel, SharedImages.getImage(SharedImages.icons.x16.arrows.RIGHT), new Listener<Object>() {
			public void fire(Object event) {
				right();
			}
		}, null);
		button.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
	}
	
	private void left() {
		List<T> sel = lists[1].getSelection();
		if (sel == null || sel.isEmpty()) return;
		for (T element : sel) {
			lists[0].add(element);
			lists[1].remove(element);
		}
	}
	private void right() {
		List<T> sel = lists[0].getSelection();
		if (sel == null || sel.isEmpty()) return;
		for (T element : sel) {
			lists[1].add(element);
			lists[0].remove(element);
		}
	}
	
	public List<T> getList1() { return lists[0].getElements(); }
	public List<T> getList2() { return lists[1].getElements(); }
}
