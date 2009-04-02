package net.lecousin.framework.media;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lecousin.framework.Pair;
import net.lecousin.framework.eclipse.extension.EclipsePluginExtensionUtil;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.media.internal.EclipsePlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class MediaPlayer {

	private MediaPlayer(MediaPlayerPlugin plugin) {
		this.plugin = plugin;
		plugin.started().addListener(new Listener<Media>() {
			public void fire(Media event) {
				current = event;
				playing = true;
				for (MediaPlayerListener listener : listeners)
					listener.mediaStarted(event);
			}
		});
		plugin.paused().addListener(new Listener<Media>() {
			public void fire(Media event) {
				playing = false;
				for (MediaPlayerListener listener : listeners)
					listener.mediaPaused(event);
			}
		});
		plugin.ended().addListener(new Listener<Media>() {
			public void fire(Media event) {
				for (MediaPlayerListener listener : listeners)
					listener.mediaEnded(event);
				try { next(); } catch (UnsupportedFormatException e) {}
			}
		});
		plugin.stopped().addListener(new Listener<Media>() {
			public void fire(Media event) {
				playing = false;
				for (MediaPlayerListener listener : listeners)
					listener.stopped(event);
			}
		});
		plugin.positionChanged().addListener(new Listener<Media>() {
			public void fire(Media event) {
				for (MediaPlayerListener listener : listeners)
					listener.mediaPositionChanged(event);
			}
		});
		plugin.timeChanged().addListener(new Listener<Pair<Media,Long>>() {
			public void fire(Pair<Media,Long> event) {
				for (MediaPlayerListener listener : listeners)
					listener.mediaTimeChanged(event.getValue1(), event.getValue2());
			}
		});
		plugin.volumeChanged().addListener(new Listener<Double>() {
			public void fire(Double event) {
				for (MediaPlayerListener listener : listeners)
					listener.volumeChanged(event);
			}
		});
		plugin.muteChanged().addListener(new Listener<Boolean>() {
			public void fire(Boolean event) {
				for (MediaPlayerListener listener : listeners)
					listener.muteChanged(event);
			}
		});
	}
	
	private MediaPlayerPlugin plugin;
	private LinkedList<Media> medias = new LinkedList<Media>();
	private Media current = null;
	private boolean playing = false;
	private List<MediaPlayerListener> listeners = new LinkedList<MediaPlayerListener>();
	
	public synchronized final void addListener(MediaPlayerListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	public synchronized final void removeListener(MediaPlayerListener listener) {
		listeners.remove(listener);
	}
	
	public synchronized Media addMedia(URI uri) throws UnsupportedFormatException {
		Media media = plugin.newMedia(uri);
		medias.add(media);
		for (MediaPlayerListener listener : listeners)
			listener.mediaAdded(media);
		return media;
	}
	public synchronized void removeMedia(URI uri) {
		for (Media media : medias)
			if (media.getURI().equals(uri)) {
				removeMedia(media);
				break;
			}
	}
	public synchronized void removeMedia(Media media) {
		if (current == media) {
			int index = medias.indexOf(current);
			if (index == medias.size()-1) {
				if (index > 0)
					current = medias.get(index-1);
				else
					current = null;
			} else
				current = medias.get(index+1);
			playing = false;
		}
		plugin.freeMedia(media);
		medias.remove(media);
		for (MediaPlayerListener listener : listeners)
			listener.mediaRemoved(media);
	}
	public synchronized void removeMedia(int index) {
		removeMedia(medias.get(index));
	}
	public synchronized void removeAllMedias() {
		for (Media media : getMedias())
			removeMedia(media);
	}
	
	public synchronized List<Media> getMedias() { return new ArrayList<Media>(medias); }

	private abstract class Run implements Runnable {
		Run(Media current) { this.current = current; }
		protected Media current;
	}
	private abstract class Run2 implements Runnable {
		Run2(Media current1, Media current2) { this.current1 = current1; this.current2 = current2; }
		protected Media current1;
		protected Media current2;
	}
	private abstract class RunD implements Runnable {
		RunD(Media current, double d) { this.current = current; this.d = d; }
		protected Media current;
		protected double d;
	}
	private abstract class RunL implements Runnable {
		RunL(Media current, long l) { this.current = current; this.l = l; }
		protected Media current;
		protected long l;
	}
	private void run(boolean background, Runnable run) {
		if (background) {
			new Thread(run).start();
		} else
			run.run();
	}
	
	public synchronized Media start() throws UnsupportedFormatException {
		return start(false);
	}
	public synchronized Media start(boolean background) throws UnsupportedFormatException {
		if (current == null) {
			if (medias.isEmpty()) return null;
			current = medias.getFirst();
		}
		start(current, background);
		return current;
	}
	public synchronized void start(Media media) throws UnsupportedFormatException {
		start(media, true);
	}
	private synchronized void start(Media media, boolean background) throws UnsupportedFormatException {
		current = media;
		if (background)
			run(background, new Run(current) {
				public void run() { try { plugin.start(current); } catch (UnsupportedFormatException e) {} }
			});
		else
			plugin.start(current);
	}
	public synchronized void pause() {
		pause(false);
	}
	public synchronized void pause(boolean background) {
		if (current == null) return;
		run(background, new Run(current) {
			public void run() { plugin.pause(current); }
		});
	}
	public synchronized void stop() {
		stop(false);
	}
	public synchronized void stop(boolean background) {
		if (current == null) return;
		run(background, new Run(current){
			public void run() { plugin.stop(current); }
		});
		playing = false;
	}
	public boolean isPlaying() {
		return playing;
	}
	
	public Media next() throws UnsupportedFormatException {
		return next(false);
	}
	public Media next(boolean background) throws UnsupportedFormatException {
		if (current == null) {
			if (medias.isEmpty()) return null;
			current = medias.getFirst();
			if (background)
				run(background, new Run(current) {
					public void run() { try { plugin.start(current); } catch (UnsupportedFormatException e) {} }
				});
			else
				plugin.start(current);
			return current;
		}
		int index = medias.indexOf(current);
		if (index == medias.size() - 1) return null;
		
		Media prev = current;
		current = medias.get(index+1);
		if (background)
			run(background, new Run2(prev, current) {
				public void run() {
					plugin.stop(current1);
					try { plugin.start(current2); } catch (UnsupportedFormatException e) {}
				}
			});
		else {
			plugin.stop(prev);
			plugin.start(current);
		}
		return current;
	}
	
	public Media getCurrentMedia() { return current; }
	
	public double getPosition() { 
		if (current == null) return 0;
		return plugin.getPosition(current); 
	}
	public void setPosition(double pos) {
		setPosition(pos, false);
	}
	public void setPosition(double pos, boolean background) {
		if (current == null) return;
		run(background, new RunD(current, pos) {
			public void run() { plugin.setPosition(current, d); }
		});
	}
	/** im milliseconds */
	public long getTime() {
		if (current == null) return 0;
		return plugin.getTime(current);
	}
	/** im milliseconds */
	public void setTime(long time) {
		setTime(time, false);
	}
	public void setTime(long time, boolean background) {
		if (current == null) return;
		run(background, new RunL(current, time) {
			public void run() { plugin.setTime(current, l); } 
		});
	}
	
	
	public boolean getMute() { return plugin.getMute(); }
	public void setMute(boolean value) { plugin.setMute(value); }
	public double getVolume() { return plugin.getVolume(); }
	public void setVolume(double volume) { 
		plugin.setVolume(volume); 
		for (MediaPlayerListener listener : listeners)
			listener.volumeChanged(plugin.getVolume());
	}
	
	
	public Control createVisual(Composite parent) {
		return plugin.createVisual(parent);
	}
	
	
	public void free() {
		free(false);
	}
	public void free(boolean background) {
		if (current != null && playing)
			plugin.stop(current);
		run(background, new Runnable() {
			public void run() {
				for (Media media : medias) {
//					System.out.println("free: " + media.getURI().toString() + " [" + media.toString() + "]");
					plugin.freeMedia(media);
				}
				medias.clear();
				plugin.free();
			}
		});
	}
	
	
	/* static part */

	private static Map<String,IConfigurationElement> plugins = null;
	
	public static Set<String> getPluginsID() {
		if (plugins == null) loadPlugins();
		return plugins.keySet();
	}
	public static String getPluginName(String id) {
		if (plugins == null) loadPlugins();
		IConfigurationElement ext = plugins.get(id);
		if (ext == null) return null;
		return ext.getAttribute("name");
	}
	public static MediaPlayer create(String id) {
		if (plugins == null) loadPlugins();
		IConfigurationElement ext = plugins.get(id);
		if (ext == null) {
			if (Log.warning(MediaPlayer.class))
				Log.warning(MediaPlayer.class, "No MediaPlayer plug-in id '" + id + "'.");
			return null;
		}
		try {
			MediaPlayerPlugin plugin = EclipsePluginExtensionUtil.createInstance(MediaPlayerPlugin.class, ext, "class", new Object[][] { new Object[] { } });
			return new MediaPlayer(plugin);
		} catch (InstantiationException e) {
			Log.error(MediaPlayer.class, "Unable to instantiate MediaPlayer plug-in id " + id + ": the class is not instantiable.", e);
		} catch (IllegalAccessException e) {
			Log.error(MediaPlayer.class, "Unable to instantiate MediaPlayer plug-in id " + id + ": the class is not accessible.", e);
		} catch (InvocationTargetException e) {
			Log.error(MediaPlayer.class, "Unable to instantiate MediaPlayer plug-in id " + id + ": the constructor thrown an exception.", e.getTargetException());
		} catch (ClassNotFoundException e) {
			Log.error(MediaPlayer.class, "Unable to instantiate MediaPlayer plug-in id " + id + ": the specified class cannot be found.", e);
		}
		return null;
	}
	
	private static void loadPlugins() {
		plugins = new HashMap<String,IConfigurationElement>();
		for (IConfigurationElement ext : EclipsePluginExtensionUtil.getExtensionsSubNode(EclipsePlugin.ID, "mediamanager", "plugin")) {
			if (Log.debug(MediaPlayer.class))
				Log.debug(MediaPlayer.class, "Loaded MediaPlayer plug-in: " + ext.getAttribute("id"));
			plugins.put(ext.getAttribute("id"), ext);
		}
	}
}
