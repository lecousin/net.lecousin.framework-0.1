package net.lecousin.framework.ui.eclipse.control.list;

import java.util.List;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LabelButton;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.LCTableProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.LCTableProvider_SingleColumnText;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LCTableWithControls<T> extends Composite {

	/** title can be null */
	public LCTableWithControls(Composite parent, String title, Provider<T> provider, boolean left, boolean orderable, boolean removable, boolean addable) {
		super(parent, SWT.NONE);
		this.provider = provider;
		setBackground(parent.getBackground());
		UIUtil.gridLayout(this, 2);
		if (title != null) {
			if (left) UIUtil.newLabel(this, "");
			UIUtil.newLabel(this, title);
			if (!left) UIUtil.newLabel(this, "");
		}
		if (left) createControls(orderable, removable, addable);
		createList();
		if (!left) createControls(orderable, removable, addable);
		detailsPanel = UIUtil.newComposite(this);
		UIUtil.gridDataHorizFill(detailsPanel).exclude = true;
	}
	
	private LCTable<T> table;
	private Composite detailsPanel;
	private LabelButton buttonUp, buttonDown, buttonRemove, buttonAdd;
	
	private Provider<T> provider;
	private Event<LCTableWithControls<T>> removeRequested = new Event<LCTableWithControls<T>>();
	private Event<LCTableWithControls<T>> addRequested = new Event<LCTableWithControls<T>>();
	
	public interface Provider<T> extends LCTableProvider<T> {
		public Control createElementDetailsControl(Composite parent, T element);
	}
	public static abstract class Provider_SimpleText<T> extends LCTableProvider_SingleColumnText<T> implements Provider<T> {
		public Provider_SimpleText(List<T> data) { super(data); }
	}
	
	public Event<LCTableWithControls<T>> removeRequested() { return removeRequested; }
	public Event<LCTableWithControls<T>> addRequested() { return addRequested; }
	
	private void createControls(boolean orderable, boolean removable, boolean addable) {
		Composite panel = UIUtil.newGridComposite(this, 0, 0, 1);
		panel.setLayoutData(UIUtil.gridDataVert(1, true));
		if (orderable) {
			buttonUp = UIUtil.newImageButton(panel, SharedImages.getImage(SharedImages.icons.x16.arrows.UP), new Listener<Object>() {
				public void fire(Object event) {
					up();
				}
			}, null);
			buttonUp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			buttonDown = UIUtil.newImageButton(panel, SharedImages.getImage(SharedImages.icons.x16.arrows.DOWN), new Listener<Object>() {
				public void fire(Object event) {
					down();
				}
			}, null);
			buttonDown.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		}
		if (removable) {
			buttonRemove = UIUtil.newImageButton(panel, SharedImages.getImage(SharedImages.icons.x16.basic.DEL), new Listener<Object>() {
				public void fire(Object event) {
					remove();
				}
			}, null);
			buttonRemove.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		}
		if (addable) {
			buttonAdd = UIUtil.newImageButton(panel, SharedImages.getImage(SharedImages.icons.x16.basic.ADD), new Listener<Object>() {
				public void fire(Object event) {
					add();
				}
			}, null);
			buttonAdd.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		}
	}
	
	private void createList() {
		table = new LCTable<T>(this, provider);
		table.getControl().setLayoutData(UIUtil.gridData(1, true, 1, true));
		table.addSelectionChangedListener(new Listener<List<T>>() {
			public void fire(List<T> event) {
				selChanged();
			}
		});
	}
	
	private void up() {
		List<T> sel = table.getSelection();
		if (sel == null || sel.isEmpty()) return;
		int iStart = table.indexOf(sel.get(0));
		if (iStart == 0) return;
		for (int pos = iStart-1, i = 0; i < sel.size(); ++i, ++pos)
			table.move(sel.get(i), pos);
	}
	private void down() {
		List<T> sel = table.getSelection();
		if (sel == null || sel.isEmpty()) return;
		int iStart = table.indexOf(sel.get(sel.size()-1));
		if (iStart == 0) return;
		for (int pos = iStart+1, i = 0; i < sel.size(); ++i, ++pos)
			table.move(sel.get(i), pos);
	}
	
	private void selChanged() {
		List<T> sel = table.getSelection();
		if (sel == null || sel.size() != 1)
			UIControlUtil.clear(detailsPanel);
		else
			provider.createElementDetailsControl(detailsPanel, sel.get(0));
		UIControlUtil.autoresize(detailsPanel);
	}
	
	private void add() {
		addRequested.fire(this);
	}
	private void remove() {
		removeRequested.fire(this);
	}
	
	public void add(T element) {
		table.add(element);
	}
	public void add(List<T> elements) {
		table.add(elements);
	}
	public void remove(T element) {
		table.remove(element);
	}
	public List<T> removeSelected() {
		return table.removeSelected();
	}
	
	public List<T> getSelection() { return table.getSelection(); }
	public List<T> getElements() { return table.getElements(); }
	
	public void setButtonUpToolTip(String text) {
		if (buttonUp != null)
			buttonUp.setToolTipText(text);
	}
	public void setButtonDownToolTip(String text) {
		if (buttonDown != null)
			buttonDown.setToolTipText(text);
	}
	public void setButtonRemoveToolTip(String text) {
		if (buttonRemove != null)
			buttonRemove.setToolTipText(text);
	}
	public void setButtonAddToolTip(String text) {
		if (buttonAdd != null)
			buttonAdd.setToolTipText(text);
	}
}
