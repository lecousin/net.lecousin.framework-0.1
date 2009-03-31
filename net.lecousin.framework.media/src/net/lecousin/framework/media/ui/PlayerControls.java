package net.lecousin.framework.media.ui;

import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.lang.MyBoolean;
import net.lecousin.framework.math.Scale;
import net.lecousin.framework.media.Media;
import net.lecousin.framework.media.MediaPlayer;
import net.lecousin.framework.media.MediaPlayerListener;
import net.lecousin.framework.media.internal.EclipsePlugin;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.ImageButton;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class PlayerControls extends Composite {

	public PlayerControls(MediaPlayerControl parent) {
		super(parent.getControl(), SWT.NONE);
		this.parent = parent;
		setBackground(ColorUtil.getBlack());
		GridLayout layout = UIUtil.gridLayout(this, 7);
		layout.horizontalSpacing = 2;
		layout.marginHeight = 0;
		GridData gd;
		
//		scaleRate = new Scale<Double>(0.1, 32.0, 1.0);
		scaleTime = new Scale<Double>(0.0, 1.0, 0.0);
		scaleTime.changed().addListener(new Listener<Double>() {
			public void fire(Double event) {
				if (changingTime.get()) return;
				player.setTime(event.longValue(), true);
			}
		});
		scaleVolume = new Scale<Double>(0.0, 200.0, 100.0);
		scaleVolume.changed().addListener(new Listener<Double>() {
			public void fire(Double event) {
				if (changingVolume.get()) return;
				player.setVolume(event);
				double pos = player.getVolume();
				if (player.getVolume() != event)
					scaleVolume.setPosition(pos);
			}
		});
		
		playPauseButton = new ImageButton(this);
		playPauseButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/play.gif"));
		playPauseButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/play_h.gif"));
		playPauseButton.addClickListener(new PlayPause());
		stopButton = new ImageButton(this);
		stopButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/stop.gif"));
		stopButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/stop_h.gif"));
		stopButton.addClickListener(new Stop());
		/*backwardButton = new ImageButton(this);
		backwardButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/backward.gif"));
		backwardButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/backward_h.gif"));
		backwardButton.addClickListener(new Backward());
		scaleRateControl = new ScaleControl(this, scaleRate, 1.0, true, new ScaleControl.LabelProvider() {
			public String getLabel(double pos, double min, double max) {
				return "x" + pos;
			}
		});
		gd = new GridData();
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.widthHint = 75;
		scaleRateControl.setLayoutData(gd);
		forwardButton = new ImageButton(this);
		forwardButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/forward.gif"));
		forwardButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/forward_h.gif"));
		forwardButton.addClickListener(new Forward());
		*/
		scaleTimeControl = new ScaleControl(this, scaleTime, true, new ScaleControl.LabelProvider() {
			public String getLabel(double pos, double min, double max) {
				return DateTimeUtil.getTimeString((long)pos, true, true, true, false) + '/' + DateTimeUtil.getTimeString((long)max, true, true, true, false);
			}
		});
		gd = new GridData();
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		scaleTimeControl.setLayoutData(gd);
		soundButton = new ImageButton(this);
		soundButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound.gif"));
		soundButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound_h.gif"));
		soundButton.addClickListener(new Sound());
		scaleVolumeControl = new ScaleControl(this, scaleVolume, true, new ScaleControl.LabelProvider() {
			public String getLabel(double pos, double min, double max) {
				return Integer.toString((int)pos) + '%';
			}
		});
		gd = new GridData();
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.widthHint = 100;
		scaleVolumeControl.setLayoutData(gd);
		previousButton = new ImageButton(this);
		previousButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/previous.gif"));
		previousButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/previous_h.gif"));
		previousButton.addClickListener(new Previous());
		nextButton = new ImageButton(this);
		nextButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/next.gif"));
		nextButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/next_h.gif"));
		nextButton.addClickListener(new Next());
	}
	
	private MediaPlayerControl parent;
	
	private ImageButton playPauseButton;
	private ImageButton stopButton;
//	private ImageButton forwardButton;
//	private ScaleControl scaleRateControl;
//	private ImageButton backwardButton;
	private ScaleControl scaleTimeControl;
	private ImageButton soundButton;
	private ScaleControl scaleVolumeControl;
	private ImageButton nextButton;
	private ImageButton previousButton;
	
//	private Scale<Double> scaleRate;
	private Scale<Double> scaleTime;
	private Scale<Double> scaleVolume;
	private MyBoolean changingTime = new MyBoolean(false);
	private MyBoolean changingVolume = new MyBoolean(false);
	
	private MediaPlayer player = null;
	
	void setPlayer(MediaPlayer player) {
		this.player = player;
		if (player == null) return;
		player.addListener(new PlayerListener());
		if (player.getMute()) {
			soundButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound_off.gif"));
			soundButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound_off_h.gif"));
		} else {
			soundButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound.gif"));
			soundButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound_h.gif"));
		}
		scaleVolume.setPosition(player.getVolume());
	}
	
	private class PlayPause implements Listener<MouseEvent> {
		public void fire(MouseEvent event) {
			if (player == null) return;
			if (player.isPlaying())
				player.pause(true);
			else
				player.start(true);
		}
	}
	private class Stop implements Listener<MouseEvent> {
		public void fire(MouseEvent event) {
			if (player == null) return;
			player.stop(true);
		}
	}
//	private class Backward implements Listener<MouseEvent> {
//		public void fire(MouseEvent event) {
//			if (player == null) return;
//		}
//	}
//	private class Forward implements Listener<MouseEvent> {
//		public void fire(MouseEvent event) {
//			if (player == null) return;
//		}
//	}
	private class Sound implements Listener<MouseEvent> {
		public void fire(MouseEvent event) {
			if (player == null) return;
			player.setMute(!player.getMute());
			if (player.getMute()) {
				soundButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound_off.gif"));
				soundButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound_off_h.gif"));
			} else {
				soundButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound.gif"));
				soundButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound_h.gif"));
			}
		}
	}
	private class Previous implements Listener<MouseEvent> {
		public void fire(MouseEvent event) {
			if (player == null) return;
			parent.getPlayList().previous();
		}
	}
	private class Next implements Listener<MouseEvent> {
		public void fire(MouseEvent event) {
			if (player == null) return;
			parent.getPlayList().next();
		}
	}
	
	private class PlayerListener implements MediaPlayerListener {
		public void mediaAdded(Media media) {
		}
		public void mediaRemoved(Media media) {
		}
		public void mediaStarted(Media media) {
			if (playPauseButton.isDisposed() || playPauseButton.getDisplay().isDisposed()) return;
			playPauseButton.getDisplay().asyncExec(new RunnableWithData<Media>(media) {
				public void run() {
					if (playPauseButton.isDisposed()) return;
					scaleTime.setMaximum((double)data().getDuration());
					synchronized (changingTime) { changingTime.set(true); }
					scaleTime.setPosition((double)player.getTime());
					synchronized (changingTime) { changingTime.set(false); }
					playPauseButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/pause.gif"));
					playPauseButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/pause_h.gif"));
					playPauseButton.redraw();
				}
			});
		}
		public void mediaPaused(Media currentMedia) {
			if (playPauseButton.isDisposed() || playPauseButton.getDisplay().isDisposed()) return;
			playPauseButton.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (playPauseButton.isDisposed()) return;
					playPauseButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/play.gif"));
					playPauseButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/play_h.gif"));
					playPauseButton.redraw();
				}
			});
		}
		public void mediaEnded(Media media) {
			if (playPauseButton.isDisposed() || playPauseButton.getDisplay().isDisposed()) return;
			playPauseButton.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (playPauseButton.isDisposed()) return;
					synchronized (changingTime) { changingTime.set(true); }
					scaleTime.setPosition((double)0);
					synchronized (changingTime) { changingTime.set(false); }
					playPauseButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/play.gif"));
					playPauseButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/play_h.gif"));
					playPauseButton.redraw();
				}
			});
		}
		public void stopped(Media lastMediaPlayed) {
			if (playPauseButton.isDisposed() || playPauseButton.getDisplay().isDisposed()) return;
			playPauseButton.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (playPauseButton.isDisposed()) return;
					scaleTime.setPosition((double)0);
					playPauseButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/play.gif"));
					playPauseButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/play_h.gif"));
					playPauseButton.redraw();
				}
			});
		}
		public void mediaPositionChanged(Media media) {
		}
		public void mediaTimeChanged(Media media, long time) {
			if (scaleTimeControl.isEditingPosition()) return;
			if (playPauseButton.isDisposed() || playPauseButton.getDisplay().isDisposed()) return;
			playPauseButton.getDisplay().asyncExec(new RunnableWithData<Pair<Media,Long>>(new Pair<Media,Long>(media,time)) {
				public void run() {
					if (playPauseButton.isDisposed()) return;
					synchronized (changingTime) { changingTime.set(true); }
					if (scaleTime.getMaximum() <= 0)
						scaleTime.setMaximum((double)data().getValue1().getDuration());
					scaleTime.setPosition((double)data().getValue2());
					synchronized (changingTime) { changingTime.set(false); }
				}
			});
		}
		public void volumeChanged(double volume) {
			if (scaleVolumeControl.isEditingPosition()) return;
			if (playPauseButton.isDisposed() || playPauseButton.getDisplay().isDisposed()) return;
			playPauseButton.getDisplay().asyncExec(new RunnableWithData<Double>(volume) {
				public void run() {
					if (playPauseButton.isDisposed()) return;
					synchronized (changingVolume) { changingVolume.set(true); }
					scaleVolume.setPosition(data());
					synchronized (changingVolume) { changingVolume.set(false); }
				}
			});
		}
		public void muteChanged(boolean mute) {
			if (playPauseButton.isDisposed() || playPauseButton.getDisplay().isDisposed()) return;
			playPauseButton.getDisplay().asyncExec(new RunnableWithData<Boolean>(mute) {
				public void run() {
					if (playPauseButton.isDisposed()) return;
					if (data()) {
						soundButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound_off.gif"));
						soundButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound_off_h.gif"));
					} else {
						soundButton.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound.gif"));
						soundButton.setHoverImage(EclipseImages.getImage(EclipsePlugin.ID, "images/player/sound_h.gif"));
					}
				}
			});
		}
	}
}
