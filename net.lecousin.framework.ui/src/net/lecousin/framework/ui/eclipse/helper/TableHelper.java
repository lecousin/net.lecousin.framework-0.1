package net.lecousin.framework.ui.eclipse.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.lecousin.framework.ui.eclipse.event.SelectionListenerWithData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableHelper {

	public static void makeColumnSortable(TableColumn col, Comparator<String> comparator) {
		col.addSelectionListener(new SelectionListenerWithData<Comparator<String>>(comparator) {
			public void widgetSelected(SelectionEvent e) {
				TableColumn col = (TableColumn)e.widget;
				if (col.getParent().getSortColumn() == col) {
					if (col.getParent().getSortDirection() == SWT.UP)
						col.getParent().setSortDirection(SWT.DOWN);
					else
						col.getParent().setSortDirection(SWT.UP);
				}
				col.getParent().setSortColumn(col);
				sort(col, col.getParent().getSortDirection() == SWT.UP, data());
			}
		});
	}
	
	public static void refreshSorting(Table table, Comparator<String>[] comparators) {
		TableColumn col = table.getSortColumn();
		if (col == null) return;
		boolean asc = table.getSortDirection() == SWT.UP;
		Comparator<String> comparator = comparators[getColumnIndex(col)];
		if (comparator == null) return;
		sort(col, asc, comparator);
	}
	
	public static void sort(TableColumn col, boolean ascending, Comparator<String> comparator) {
		if (col == null) return;
		Table table = col.getParent();
		sort(table.getItems(), getColumnIndex(col), ascending, comparator);
	}
	
	public static int getColumnIndex(TableColumn col) {
		return col.getParent().indexOf(col);
	}
	
	public static void sort(TableItem[] items, int colIndex, boolean ascending, Comparator<String> comparator) {
		if (colIndex < 0) return;
		if (items.length == 0) return;
		ArrayList<TableItem> list = new ArrayList<TableItem>(items.length);
		for (int i = 0; i < items.length; ++i)
			list.add(items[i]);
		Collections.sort(list, new ItemComparator(colIndex, ascending, comparator));
		List<TempItem> newList = makeTemp(list);
		Table table = items[0].getParent();
		table.removeAll();
		for (TempItem old : newList) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(old.texts);
			item.setData(old.data);
			item.setImage(old.images);
		}
	}
	
	private static class TempItem {
		String[] texts;
		Image[] images;
		Object data;
		Font[] fonts;
	}
	private static List<TempItem> makeTemp(List<TableItem> items) {
		ArrayList<TempItem> result = new ArrayList<TempItem>();
		for (TableItem item : items)
			result.add(makeTemp(item));
		return result;
	}
	private static TempItem makeTemp(TableItem item) {
		TempItem temp = new TempItem();
		temp.texts = getTexts(item);
		temp.images = getImages(item);
		temp.data = item.getData();
		temp.fonts = getFonts(item);
		return temp;
	}
	
	public static TableItem moveItem(TableItem item, int index) {
		Table table = item.getParent();
		if (table.indexOf(item) == index) return item;
		TempItem tmp = makeTemp(item);
		item.dispose();
		TableItem newItem = new TableItem(table, SWT.NONE, index);
		newItem.setText(tmp.texts);
		newItem.setData(tmp.data);
		newItem.setImage(tmp.images);
		for (int i = 0; i < table.getColumnCount(); ++i) {
			newItem.setFont(i, tmp.fonts[i]);
		}
		return newItem;
	}
	
	public static String[] getTexts(TableItem item) {
		String[] result = new String[item.getParent().getColumnCount()];
		for (int i = 0; i < result.length; ++i)
			result[i] = item.getText(i);
		return result;
	}
	public static Image[] getImages(TableItem item) {
		Image[] result = new Image[item.getParent().getColumnCount()];
		for (int i = 0; i < result.length; ++i)
			result[i] = item.getImage(i);
		return result;
	}
	public static Font[] getFonts(TableItem item) {
		Font[] result = new Font[item.getParent().getColumnCount()];
		for (int i = 0; i < result.length; ++i)
			result[i] = item.getFont(i);
		return result;
	}
	
	public static class ItemComparator implements Comparator<TableItem> {
		public ItemComparator(int colIndex, boolean ascending, Comparator<String> comparator) {
			this.colIndex = colIndex;
			this.ascending = ascending;
			this.comparator = comparator;
		}
		private int colIndex;
		private boolean ascending;
		private Comparator<String> comparator;
		public int compare(TableItem o1, TableItem o2) {
			String str1 = o1.getText(colIndex);
			String str2 = o2.getText(colIndex);
			return ascending ? comparator.compare(str1, str2) : -comparator.compare(str1, str2);
		}
	}
}
