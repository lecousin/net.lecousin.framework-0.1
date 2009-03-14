package net.lecousin.framework.ui.eclipse.dialog;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.Local;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.CollapsableSection;
import net.lecousin.framework.ui.eclipse.control.LabelButton;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class FlatPagedListDialog<T> extends FlatDialog {

	public FlatPagedListDialog(Shell parent, String title, List<T> data, int elementsByPage, Provider<T> provider, Filter<T>[] filters) {
		super(parent, title, true, true);
		this.allData = data;
		this.provider = provider;
		this.filters = filters;
		byPage = elementsByPage;
		if (filters != null)
			for (Filter<T> filter : filters)
				filter.setDialog(this);
	}
	
	private List<T> allData;
	private Provider<T> provider;
	private List<T> filteredData = new LinkedList<T>();
	private Filter<T>[] filters;
	
	private int byPage;
	private int page = 1;

	private Composite contentPanel;
	private Composite pageButtonsPanels[] = new Composite[2];
	private LabelButton leftButtons[] = new LabelButton[2];
	private LabelButton rightButtons[] = new LabelButton[2];
	private Label texts[] = new Label[2];
	
	public static interface Provider<T> {
		public Control createControl(Composite parent, T element);
	}
	public static abstract class TextProvider<T> implements Provider<T> {
		public Control createControl(Composite parent, T element) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(getText(element));
			return label;
		}
		protected abstract String getText(T element);
	}
	
	public static interface Filter<T> {
		public String getName();
		public void fillPanel(Composite panel);
		public boolean accept(T element);
		public void setDialog(FlatPagedListDialog<T> dialog);
	}
	public static abstract class FilterListPossibilities<T,TPos> implements Filter<T> {
		public FilterListPossibilities(Iterable<TPos> possibilities) {
			this.possibilities = possibilities;
		}
		private Iterable<TPos> possibilities;
		private List<Button> checks = new LinkedList<Button>();
		protected FlatPagedListDialog<T> dialog;
		public final void setDialog(FlatPagedListDialog<T> dialog) {
			this.dialog = dialog;
		}
		public final void fillPanel(Composite panel) {
			UIUtil.gridLayout(panel, 1, 0, 0).verticalSpacing = 1;
			Composite buttons = UIUtil.newComposite(panel);
			RowLayout layout = new RowLayout(SWT.HORIZONTAL);
			layout.wrap = true;
			layout.marginBottom = layout.marginHeight = layout.marginTop = 0;
			layout.marginLeft = layout.marginRight = layout.marginWidth = 0;
			buttons.setLayout(layout);
			UIUtil.gridDataHorizFill(buttons);
			UIUtil.newImageTextButton(buttons, SharedImages.getImage(SharedImages.icons.x16.basic.ADD), "Select all", new Listener<Object>() {
				public void fire(Object event) {
					for (Button b : checks)
						b.setSelection(true);
					dialog.refreshFilter();
				}
			}, null);
			UIUtil.newImageTextButton(buttons, SharedImages.getImage(SharedImages.icons.x16.basic.DEL), "Unselect all", new Listener<Object>() {
				public void fire(Object event) {
					for (Button b : checks)
						b.setSelection(false);
					dialog.refreshFilter();
				}
			}, null);

			buttons = UIUtil.newComposite(panel);
			layout = new RowLayout(SWT.HORIZONTAL);
			layout.wrap = true;
			buttons.setLayout(layout);
			UIUtil.gridDataHorizFill(buttons);
			for (TPos pos : possibilities) {
				Button b = UIUtil.newCheck(buttons, getName(pos), new Listener<Object>() {
					public void fire(Object event) {
						dialog.refreshFilter();
					}
				}, null);
				b.setData(pos);
				b.setSelection(true);
				checks.add(b);
			}
		}
		@SuppressWarnings("unchecked")
		public final boolean accept(T element) {
			for (Button b : checks)
				if (b.getSelection() && accept(element, (TPos)b.getData())) return true;
			return false;
		}
		protected abstract boolean accept(T element, TPos possibility);
		protected abstract String getName(TPos possibility);
	}
	
	private int getNbPages() {
		int nb = filteredData.size()/byPage + (filteredData.size()%byPage > 0 ? 1 : 0);
		if (nb == 0) nb = 1;
		return nb;
	}
	
	@Override
	protected void createContent(Composite container) {
		setMaxWidth(800);
		setMaxHeight(500);
		UIUtil.gridLayout(container, 1);
		container.setBackground(ColorUtil.getWhite());
		if (filters != null && filters.length > 0) {
			CollapsableSection section = new CollapsableSection(container);
			UIUtil.gridDataHorizFill(section);
			section.setText(Local.Filters.toString());
			Composite panel = UIUtil.newComposite(section);
			UIUtil.gridLayout(panel, 1, 0, 0);
			for (Filter<T> filter : filters) {
				Group group = new Group(panel, SWT.SHADOW_IN);
				group.setBackground(section.getBackground());
				group.setText(filter.getName());
				filter.fillPanel(group);
			}
			section.setBody(panel);
		}
		createPageControls(container, 0);
		contentPanel = UIUtil.newComposite(container);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.numColumns = 1;
		layout.verticalSpacing = 0;
		contentPanel.setLayout(layout);
		UIUtil.gridDataHorizFill(contentPanel);
		createPageControls(container, 1);
		refreshFilter();
	}
	
	private void createPageControls(Composite parent, int index) {
		pageButtonsPanels[index] = UIUtil.newGridComposite(parent, 2, 2, 3);
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.CENTER;
		pageButtonsPanels[index].setLayoutData(gd);
		
		leftButtons[index] = UIUtil.newImageButton(pageButtonsPanels[index], SharedImages.getImage(SharedImages.icons.x16.arrows.LEFT), new Listener<Object>() {
				public void fire(Object event) {
					page--;
					refreshPage();
				}
			}, null);
		texts[index] = UIUtil.newLabel(pageButtonsPanels[index], "");
		rightButtons[index] = UIUtil.newImageButton(pageButtonsPanels[index], SharedImages.getImage(SharedImages.icons.x16.arrows.RIGHT), new Listener<Object>() {
			public void fire(Object event) {
				page++;
				refreshPage();
			}
		}, null);
	}
	
	private static Color rowBgColor1 = ColorUtil.getWhite();
	private static Color rowFgColor1 = ColorUtil.getBlack();
	private static Color rowBgColor2 = ColorUtil.get(220, 220, 255);
	private static Color rowFgColor2 = ColorUtil.getBlack();
	private void refreshPage() {
		int nbPages = getNbPages();
		if (nbPages > 1) {
			leftButtons[0].setEnabled(page > 1);
			leftButtons[1].setEnabled(page > 1);
			rightButtons[0].setEnabled(page < nbPages);
			rightButtons[1].setEnabled(page < nbPages);
		}
		if (texts[0] != null) texts[0].setText(Local.Page+" " + page + " "+Local.on+" " + nbPages);
		if (texts[1] != null) texts[1].setText(Local.Page+" " + page + " "+Local.on+" " + nbPages);
		UIControlUtil.clear(contentPanel);
		for (int i = 0; i < byPage; ++i) {
			int index = (page-1)*byPage+i;
			if (index >= filteredData.size()) break;
			T element = filteredData.get(index);
			Control c = provider.createControl(contentPanel, element);
			c.setBackground((i%2) == 0 ? rowBgColor1 : rowBgColor2);
			c.setForeground((i%2) == 0 ? rowFgColor1 : rowFgColor2);
			UIUtil.gridDataHorizFill(c);
		}
		UIControlUtil.resize(contentPanel);
		resize();
	}
	
	public void refreshFilter() {
		filteredData.clear();
		if (filters == null || filters.length == 0)
			filteredData.addAll(allData);
		else
			for (T data : allData) {
				boolean ok = true;
				for (Filter<T> filter : filters)
					if (!filter.accept(data)) {
						ok = false;
						break;
					}
				if (ok)
					filteredData.add(data);
			}
		
		int nbPages = getNbPages();
		((GridData)pageButtonsPanels[0].getLayoutData()).exclude = nbPages <= 1;
		((GridData)pageButtonsPanels[1].getLayoutData()).exclude = nbPages <= 1;
		pageButtonsPanels[0].setVisible(nbPages > 1);
		pageButtonsPanels[1].setVisible(nbPages > 1);
		if (page > nbPages)
			page = nbPages;
		refreshPage();
	}
	
	public void open() {
		super.openFlatDialog(true, true);
	}
}
