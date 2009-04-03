package net.lecousin.framework.media.ui;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.framework.media.Local;
import net.lecousin.framework.media.MediaPlayer;
import net.lecousin.framework.media.UnsupportedFormatException;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class PlayList {

	public PlayList(Composite parent, MediaPlayerControl player) {
		this.player = player;
		panel = new Composite(parent, SWT.NONE);
		panel.setBackground(ColorUtil.getBlack());
		UIUtil.gridLayout(panel, 1, 0, 0, 0, 0);
		customizePanel = new Composite(panel, SWT.NONE);
		GridData gd = UIUtil.gridDataHoriz(1, true);
		if (!createCustomizePanel(customizePanel))
			gd.exclude = true;
		customizePanel.setLayoutData(gd);
		tree = new Tree(panel, SWT.MULTI | SWT.FULL_SELECTION);
		gd = UIUtil.gridData(1, true, 1, true);
		gd.widthHint = 200;
		tree.setLayoutData(gd);
		tree.setBackground(ColorUtil.getBlack());
		
		tree.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				TreeItem item = (TreeItem)e.item;
				if (item != null)
					start(item);
			}
			public void widgetSelected(SelectionEvent e) {
			}
		});
		tree.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
			}
			public void mouseUp(MouseEvent e) {
				if (e.button == 3)
					rightClick();
			}
		});
		addDropSupport();
	}

	private MediaPlayerControl player;
	private Composite panel;
	private Composite customizePanel;
	private Tree tree;
	private Object playing = null;
	
	public MediaPlayerControl getPlayer() { return player; }
	public Composite getControl() { return panel; }
	public Composite getCustomizePanel() { return customizePanel; }
	public Tree getTree() { return tree; }
	
	protected boolean createCustomizePanel(Composite parent) {
		return false;
	}
	
	public void add(Object media) {
		createItem(media, tree.getItemCount());
	}
	public void insert(Object media, int index) {
		createItem(media, index);
	}
	
	public final void start(Object media) {
		TreeItem item = getItem(media);
		if (item == null) return;
		start(item);
	}
	private void start(TreeItem item) {
		if (item.getItemCount() == 0)
			startMedia(item.getData(), item);
		else
			start(item.getItem(0));
	}
	protected void startMedia(Object media, TreeItem item) {
		URI uri;
		try { uri = getURI(media); }
		catch (MediaException e) {
			MessageDialog.openError(item.getParent().getShell(), "Media Player", e.getMessage());
			return;
		}
		if (uri == null) return;
		String plugin = getPlugin(media);
		if (plugin == null) return;
		tree.showItem(item);
		playing(item);
		try { 
			player.start(uri, plugin); 
		} catch (UnsupportedFormatException e) {
			// TODO alternate plugin
		}
		playing = media;
	}
	
	public void next() {
		if (playing == null) return;
		TreeItem item = getItem(playing);
		if (item == null) return;
		next(item);
	}
	private void next(TreeItem item) {
		if (item.getParentItem() != null) {
			int i = item.getParentItem().indexOf(item);
			if (i+1 < item.getParentItem().getItemCount()) {
				start(item.getParentItem().getItem(i+1));
				return;
			}
			next(item.getParentItem());
			return;
		}
		int i = item.getParent().indexOf(item);
		if (i+1 < item.getParent().getItemCount()) {
			start(item.getParent().getItem(i+1));
			return;
		}
	}
	public void previous() {
		if (playing == null) return;
		TreeItem item = getItem(playing);
		if (item == null) return;
		previous(item);
	}
	private void previous(TreeItem item) {
		if (item.getParentItem() != null) {
			int i = item.getParentItem().indexOf(item);
			if (i > 0) {
				start(item.getParentItem().getItem(i-1));
				return;
			}
			previous(item.getParentItem());
			return;
		}
		int i = item.getParent().indexOf(item);
		if (i > 0) {
			start(item.getParent().getItem(i-1));
			return;
		}
	}
	
	public void remove(Object media) {
		if (playing == media)
			player.stop();
		getItem(media).dispose();
	}
	public void remove(List<Object> medias) {
		for (Object media : medias)
			remove(media);
	}
	
	public final void addAndStart(Object media) {
		add(media);
		start(media);
	}
	
	private void playing(TreeItem item) {
		stopPlaying(item.getParent().getItems());
		item.setForeground(ColorUtil.getYellow());
		TreeItem parent = item.getParentItem();
		if (parent != null)
			playing(parent);
	}
	private void stopPlaying(TreeItem[] items) {
		for (TreeItem i : items) {
			if (!ColorUtil.isSame(i.getForeground(), ColorUtil.getYellow())) continue;
			i.setForeground(ColorUtil.getWhite());
			stopPlaying(i.getItems());
		}
	}
	
	protected TreeItem getItem(Object media) {
		return getItem(media, tree.getItems());
	}
	protected TreeItem getItem(Object media, TreeItem[] items) {
		for (TreeItem item : items) {
			if (item.getData() == media)
				return item;
			TreeItem i = getItem(media, item.getItems());
			if (i != null) return i;
		}
		return null;
	}
	
	private final void createItem(Object media, int index) {
		TreeItem item = createCustomizedItem(media, index);
		if (item == null) {
			if (media instanceof File)
				item = createItem((File)media, index);
			else if (media instanceof URI)
				item = createItem((URI)media, index);
		}		
		if (item != null) {
			item.setData(media);
			finalizeItem(item);
		}
	}
	protected TreeItem createCustomizedItem(Object media, int index) {
		return null;
	}
	protected void finalizeItem(TreeItem item) {
		item.setBackground(ColorUtil.getBlack());
		item.setForeground(ColorUtil.getWhite());
	}
	
	private final TreeItem createItem(File file, int index) {
		TreeItem item = new TreeItem(tree, SWT.NONE, index);
		item.setText(file.getName());
		return item;
	}
	private final TreeItem createItem(URI uri, int index) {
		TreeItem item = new TreeItem(tree, SWT.NONE, index);
		String name = uri.getPath();
		int i = name.lastIndexOf('/');
		if (i >= 0) name = name.substring(i+1);
		item.setText(name);
		return item;
	}
	
	protected URI getURI(Object media) throws MediaException {
		if (media instanceof URI)
			return (URI)media;
		if (media instanceof File) {
			File file = (File)media;
			if (!file.exists()) throw new MediaException(Local.process(Local.File__not_found, file.getAbsolutePath()));
			return file.toURI();
		}
		return null;
	}
	
	protected String getPlugin(Object media) {
		Set<String> plugins = MediaPlayer.getPluginsID();
		if (plugins.isEmpty()) return null;
		return plugins.iterator().next();
	}
	
	protected void rightClick() {
		TreeItem[] sel = tree.getSelection();
		if (sel.length == 0) return;
		List<Object> list = new ArrayList<Object>(sel.length);
		for (TreeItem item : sel)
			list.add(item.getData());
		FlatPopupMenu menu = new FlatPopupMenu(tree, null, false, false, false, true);
		fillMenu(menu, list);
		new FlatPopupMenu.Menu(menu, Local.Remove_from_play_list.toString(), SharedImages.getImage(SharedImages.icons.x16.basic.DEL), false, false, new RunnableWithData<List<Object>>(list) {
			public void run() {
				remove(data());
			}
		});
		menu.show(null, FlatPopupMenu.Orientation.BOTTOM, true);
	}
	protected void fillMenu(FlatPopupMenu menu, List<Object> medias) {
		
	}
	
	private void addDropSupport() {
		DropTarget target = new DropTarget(tree, DND.DROP_LINK);
		List<Transfer> list = getDropTransfer();
		target.setTransfer(list.toArray(new Transfer[list.size()]));
		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				drop_dragEnter(event);
			}
			public void dragLeave(DropTargetEvent event) {
				drop_dragLeave(event);
			}
			public void dragOperationChanged(DropTargetEvent event) {
				drop_dragOperationChanged(event);
			}
			public void dragOver(DropTargetEvent event) {
				drop_dragOver(event);
			}
			public void dropAccept(DropTargetEvent event) {
				drop_dropAccept(event);
			}
			public void drop(DropTargetEvent event) {
				TreeItem item = tree.getItem(tree.toControl(new Point(event.x, event.y)));
				drop_drop(event, item);
			}
		});
	}
	protected List<Transfer> getDropTransfer() {
		LinkedList<Transfer> list = new LinkedList<Transfer>();
		list.add(FileTransfer.getInstance());
		list.add(URLTransfer.getInstance());
		return list;
	}
	protected void drop_dragEnter(DropTargetEvent event) {
		TransferData support = null;
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType))
			support = event.currentDataType;
		else {
			for (TransferData d : event.dataTypes)
				if (FileTransfer.getInstance().isSupportedType(d)) {
					support = d;
					break;
				}
			if (URLTransfer.getInstance().isSupportedType(event.currentDataType))
				support = event.currentDataType;
			else {
				for (TransferData d : event.dataTypes)
					if (URLTransfer.getInstance().isSupportedType(d)) {
						support = d;
						break;
					}
			}
		}
		if (support != null)
			event.currentDataType = support;
		event.detail = support != null ? DND.DROP_LINK : DND.DROP_NONE;
	}
	protected void drop_dragLeave(DropTargetEvent event) {
	}
	protected void drop_dragOperationChanged(DropTargetEvent event) {
		drop_dragEnter(event);
	}
	protected void drop_dragOver(DropTargetEvent event) {
	}
	protected void drop_dropAccept(DropTargetEvent event) {
	}
	protected void drop_drop(DropTargetEvent event, TreeItem item) {
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
			String[] paths = (String[])event.data;
			for (String path : paths)
				if (item == null)
					add(new File(path));
				else
					insert(new File(path), tree.indexOf(item));
		} else if (URLTransfer.getInstance().isSupportedType(event.currentDataType)) {
			URI uri;
			try { uri = ((URL)event.data).toURI(); }
			catch (URISyntaxException e) { return; }
			if (item == null)
				add(uri);
			else
				insert(uri, tree.indexOf(item));
		}
	}
}
