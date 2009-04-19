package net.lecousin.framework.ui.eclipse.control.list;

import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.Local;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LCGroup;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProvider;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class LCTable_SelectColumnsDialog<T> extends FlatDialog {

	public LCTable_SelectColumnsDialog(Shell parent, LCTable<T> table, List<Pair<String,List<ColumnProvider<T>>>> allColumns, List<String> columnsShown) {
		super(parent, Local.Select_columns.toString(), false, false);
		this.table = table;
		this.allColumns = allColumns;
		this.columnsShown = columnsShown;
	}

	private LCTable<T> table;
	private List<Pair<String,List<ColumnProvider<T>>>> allColumns;
	private List<String> columnsShown;
	
	@Override
	protected void createContent(Composite container) {
		UIUtil.gridLayout(container, 1);
		for (Pair<String,List<ColumnProvider<T>>> p : allColumns) {
			LCGroup group = new LCGroup(container, p.getValue1());
			UIUtil.gridLayout(group.getInnerControl(), 1);
			for (ColumnProvider<T> c : p.getValue2()) {
				UIUtil.newCheck(group.getInnerControl(), c.getTitle(), new Listener<Pair<Boolean,ColumnProvider<T>>>() {
					public void fire(Pair<Boolean, ColumnProvider<T>> event) {
						if (event.getValue1()) {
							table.addColumn(event.getValue2());
							columnsShown.add(event.getValue2().getTitle());
						} else {
							table.removeColumn(event.getValue2());
							columnsShown.remove(event.getValue2().getTitle());
						}
					}
				}, c).setSelection(columnsShown.contains(c.getTitle()));
			}
		}
	}
}
