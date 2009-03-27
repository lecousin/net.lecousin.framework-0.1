package net.lecousin.framework.media.ui;

import java.net.URI;

import net.lecousin.framework.media.Media;
import net.lecousin.framework.media.MediaPlayer;
import net.lecousin.framework.media.MediaPlayerListener;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class MediaPlayerControl {
	
	public MediaPlayerControl(Composite parent) {
		panel = new Composite(parent, SWT.NONE);
		panel.setBackground(ColorUtil.getBlack());
		GridLayout layout = UIUtil.gridLayout(panel, 1);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		SashForm sash = new SashForm(panel, SWT.HORIZONTAL | SWT.SMOOTH);
		sash.SASH_WIDTH = 2;
		//sash.setForeground(ColorUtil.getOrange());
		sash.setBackground(controlSeparatorColor);
		sash.setLayoutData(UIUtil.gridData(1, true, 1, true));
		visualPanel = new Composite(sash, SWT.NONE);
		//visualPanel.setLayoutData(UIUtil.gridData(1, true, 1, true));
		visualPanel.setBackground(ColorUtil.getBlack());
		layout = UIUtil.gridLayout(visualPanel, 1);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		playlist = createPlayList(sash);
//		playlist.getControl().setLayoutData(UIUtil.gridData(1, false, 1, true));
		sash.setWeights(new int[] { 75, 25 });
		controls = createControls();
		UIUtil.gridDataHorizFill(controls);
		panel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (player != null) player.free(true);
			}
		});
	}

	private Composite panel;
	private Composite visualPanel;
	private PlayList playlist;
	private PlayerControls controls;
	private Color controlSeparatorColor = ColorUtil.get(0, 100, 180);
	
	private String plugin = null;
	private MediaPlayer player = null;
	private Control visual = null;
	private PlayerListener playerListener = new PlayerListener();
	
	public Composite getControl() { return panel; }
	public PlayList getPlayList() { return playlist; }
	public PlayerControls getControls() { return controls; }
	
	protected PlayList createPlayList(Composite parent) {
		return new PlayList(parent, this);
	}
	protected PlayerControls createControls() {
		return new PlayerControls(this);
	}
	
	public Color getControlSeparatorColor() { return controlSeparatorColor; }
	
	void start(URI uri, String plugin) {
		initPlugin(plugin);
		if (player != null) {
			//List<Media> medias = player.getMedias();
			Media m = player.addMedia(uri);
			player.start(m);
			//for (Media media : medias)
			//	player.removeMedia(media);
		}
	}
	
	private void initPlugin(String id) {
		if (plugin != null && plugin.equals(id)) {
			if (player == null) return;
			player.removeAllMedias();
		} else {
			plugin = id;
			if (visual != null)
				visual.dispose();
			visual = null;
			if (player != null)
				player.free();
			player = MediaPlayer.create(id);
			player.addListener(playerListener);
			controls.setPlayer(player);
			if (player == null) return;
		}
		if (visual == null) {
			visual = player.createVisual(visualPanel);
			if (visual != null)
				visual.setLayoutData(UIUtil.gridData(1, true, 1, true));
			visualPanel.layout(true, true);
		}
	}
	
	void stop() {
		if (player != null) {
			player.stop();
			player.removeAllMedias();
		}
	}
	
	private class PlayerListener implements MediaPlayerListener {
		public void mediaAdded(Media media) {
		}
		public void mediaRemoved(Media media) {
		}
		public void mediaStarted(Media media) {
		}
		public void mediaPaused(Media currentMedia) {
		}
		public void mediaEnded(Media media) {
			if (panel.isDisposed()) return;
			panel.getDisplay().asyncExec(new Runnable() {
				public void run() {
					playlist.next();
				}
			});
		}
		public void stopped(Media lastMediaPlayed) {
		}
		public void mediaPositionChanged(Media media) {
		}
		public void mediaTimeChanged(Media media, long time) {
		}
		public void volumeChanged(double volume) {
		}
	}
	
	/*
	public void setPlugin(String pluginID) {
		player = MediaPlayer.create(pluginID);
		if (player == null) return;
		visual = player.createVisual(playerPanel);
		visual.setLayoutData(UIUtil.gridData(1, true, 1, true));
		playlist.setPlayer(player);
		controls.setPlayer(player);
	}
	
	public Media add(URI uri) {
		return playlist.add(uri);
	}
	
	public Media start() {
		Media media = playlist.start();
		playerPanel.layout(true, true);
		return media;
	}
	
	public Point getSizeForVisualSize(int x, int y) {
		Point listSize = playlist.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point ctrlSize = controls.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point size = new Point(x + listSize.x, y + ctrlSize.y);
		if (ctrlSize.x > size.x) size.x = ctrlSize.x;
		if (listSize.y > size.y) size.y = listSize.y;
		return size;
	}*/
}
