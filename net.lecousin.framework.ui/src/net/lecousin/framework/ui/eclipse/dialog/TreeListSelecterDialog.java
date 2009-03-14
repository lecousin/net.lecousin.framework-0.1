package net.lecousin.framework.ui.eclipse.dialog;

import java.util.Collection;
import java.util.Iterator;

import net.lecousin.framework.ui.eclipse.control.BorderControl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public abstract class TreeListSelecterDialog<T> extends MyDialog {

    protected Tree tree;
    private Button button_ok, button_cancel;
    private T selection = null;
    private Text searchText;
    private Table searchList;
    private Group searchGroup;
    private Composite rootPanel;
    private Class<T> dataType;

    public TreeListSelecterDialog(Class<T> dataType, Shell parent) {
        super(parent);
        this.dataType = dataType;
    }

    protected T open(T preSelected, String dialogTitle, String objectToSearch) {
    	selection = preSelected;
    	this.objectToSearch = objectToSearch;
    	super.open(dialogTitle, FLAGS_MODAL_DIALOG);
        return selection;
    }
    private String objectToSearch;
    
    @Override
    protected Composite createControl(Composite container) {
        Composite panel = new Composite(container, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        panel.setLayout(layout);
        rootPanel = panel;
        
        Label label;
        GridData gd;

        searchGroup = new Group(panel, SWT.SHADOW_IN);
        searchGroup.setText(objectToSearch + " search");
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        searchGroup.setLayoutData(gd);
        layout = new GridLayout();
        layout.numColumns = 1;
        searchGroup.setLayout(layout);
        label = new Label(searchGroup, SWT.NONE);
        label.setText("Enter the " + objectToSearch + " to search:");
        searchText = new Text(searchGroup, SWT.BORDER);
        gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        searchText.setLayoutData(gd);
        searchList = new Table(searchGroup, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        //searchList = new List(searchGroup, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.heightHint = 70;
        searchList.setLayoutData(gd);
        searchText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateSearch();
            }
        });
        searchText.addKeyListener(new KeyListener() {
        	public void keyPressed(KeyEvent e) {
        		// nothing to do
        	}
        	public void keyReleased(KeyEvent e) {
        		if (e.character == '\r' && button_ok.isEnabled())
        			ok();
        	}
        });
        searchList.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                selectData();
            }
        });
        searchList.addMouseListener(new MouseListener() {
        	public void mouseDoubleClick(MouseEvent e) {
        		handleListDoubleClick();
        	}
        	public void mouseDown(MouseEvent e) {
        		// nothing to do
        	}
        	public void mouseUp(MouseEvent e) {
        		// nothing to do
        	}
        });

        
        
        label = new Label(panel, SWT.NONE);
        gd = new GridData();
        gd.horizontalSpan = 2;
        label.setLayoutData(gd);
        panel.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                Rectangle bounds = searchGroup.getBounds();
                int y = bounds.y + bounds.height;
                Point size = rootPanel.getSize();
                e.gc.drawLine(5, y + 10, size.x - 5, y + 10);
            }
        });
        
        
        label = new Label(panel, SWT.NONE);
        label.setText("Select a " + objectToSearch + ":");
        gd = new GridData();
        gd.horizontalSpan = 2;
        label.setLayoutData(gd);
        
        BorderControl border = new BorderControl(panel);
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.widthHint = 350;
        gd.heightHint = 175;
        border.setLayoutData(gd);
        tree = new Tree(border, SWT.NONE);
        TreeItem sel = buildTree(tree, selection);
        
        button_ok = new Button(panel, SWT.NONE);
        button_ok.setText("Ok");
        button_ok.setEnabled(false);
        button_ok.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ok();
            }
        });
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.CENTER;
        button_ok.setLayoutData(gd);
        button_cancel = new Button(panel, SWT.NONE);
        button_cancel.setText("Cancel");
        button_cancel.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                cancel();
            }
        });
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.CENTER;
        button_cancel.setLayoutData(gd);

        if (sel != null) {
            tree.setSelection(new TreeItem[] { sel });
            select();
        } else
            tree.setSelection(new TreeItem[] {});
        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                select();
            }
        });
        tree.addMouseListener(new MouseListener() {
        	public void mouseDoubleClick(MouseEvent e) {
        		handleTreeDoubleClick();
        	}
        	public void mouseDown(MouseEvent e) {
        		// nothing to do
        	}
        	public void mouseUp(MouseEvent e) {
        		// nothing to do
        	}
        });
        
        
        
        if (selection != null) {
        	searchText.setText(getText(selection));
        	searchText.selectAll();
        	updateSearch();
        	String s = getSearchListText(selection);
        	TableItem[] items = searchList.getItems();
        	for (int i = 0; i < items.length; ++i) {
        		if (items[i].getText().equals(s)) {
        			searchList.select(i);
        			break;
        		}
        	}
        }

        
        //Point size = panel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        //Rectangle rect = shell.computeTrim(0, 0, size.x, size.y);
        //shell.setSize(rect.width, rect.height);
        
        return panel;
    }
    
    /** builds the tree and returns the pre-selected item */
    protected abstract TreeItem buildTree(Tree tree, T preSelected);
    
    @SuppressWarnings("unchecked")
    private void ok() {
        TreeItem[] sel = tree.getSelection();
        selection = (T)sel[0].getData();
        close();
    }
    
    private void cancel() {
        selection = null;
        close();
    }
    
    /** returns true if the given data is selectable */
    protected abstract boolean isSelectable(Object data);
    
    private void select() {
        TreeItem[] sel = tree.getSelection();
        if (sel.length > 0) {
            Object data = sel[0].getData();
            button_ok.setEnabled(data != null && dataType.isAssignableFrom(data.getClass()) && isSelectable(data));
            if (data != null) {
            	String full = sel[0].getText();
            	String s = searchText.getText();
            	if (s.length() < full.length() && full.startsWith(s)) {
            		searchText.setText(full);
            		searchText.setSelection(s.length(), full.length());
            	}
            }
        } else
            button_ok.setEnabled(false);
    }
    
    /** returns the pre-filled text according to the given pre-selected data */
    protected abstract String getText(T preSelected);
    /** returns the list of data compatible with the given search text */
    protected abstract Collection<T> search(String begin);
    /** returns the text to put in the list for the given data. Must be unique by data. */
    protected abstract String getSearchListText(T data);
    
    private boolean isUpdating = false;
    private void updateSearch() {
    	if (isUpdating) return;
    	isUpdating = true;
        TableItem[] sel = searchList.getSelection();
        searchList.removeAll();
        String str = searchText.getText();
        if (str.length() == 0) {
        	isUpdating = false;
        	return;
        }
        Collection<T> result = search(str);
        int iSel = -1;
        for (Iterator<T> it = result.iterator(); it.hasNext(); ) {
            T data = it.next();
            String s = getSearchListText(data);
            int i = searchList.getItemCount();
            TableItem item = new TableItem(searchList, SWT.NONE);
            item.setText(s);
            if (iSel == -1 && sel.length > 0 && sel[0].equals(s))
                iSel = i;
        }
        if (iSel != -1)
            searchList.select(iSel);
        else if (searchList.getItemCount() > 0)
            searchList.select(0);
        else {
        	isUpdating = false;
        	return;
        }
        selectData();
        isUpdating = false;
    }
    private void selectData() {
        tree.setSelection(new TreeItem[] {});
        TableItem[] sels = searchList.getSelection();
        if (sels.length == 0) return;
        String sel = sels[0].getText();
        TreeItem item = searchItem(sel);
        if (item != null) {
            tree.setSelection(new TreeItem[] { item });
            tree.showSelection();
        }
        select();
    }
    protected abstract TreeItem searchItem(String searchListText);
    
    private void handleListDoubleClick() {
        TableItem[] sel = searchList.getSelection();
    	if (sel.length != 1) return;
    	ok();
    }
    
    private void handleTreeDoubleClick() {
        TreeItem[] sel = tree.getSelection();
    	if (sel.length != 1) return;
    	ok();
    }
}
