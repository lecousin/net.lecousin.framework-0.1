package net.lecousin.framework.ui.eclipse.progress;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.Local;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.ImageAndTextButton;
import net.lecousin.framework.ui.eclipse.control.LCProgressBar;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class EmbeddedWorkProgressControl implements Listener<WorkProgress> {

	public EmbeddedWorkProgressControl(WorkProgress progress) {
		this(progress, ColorUtil.get(60, 60, 240), ColorUtil.get(60, 60, 240));
	}
	public EmbeddedWorkProgressControl(WorkProgress progress, Color mainProgressBarColor, Color subProgressBarsColor) {
		this.progress = progress;
		this.mainBarColor = mainProgressBarColor;
		this.subBarsColor = subProgressBarsColor;
		progress.addProgressListener(this);
	}
	
	private WorkProgress progress;
	private Color mainBarColor, subBarsColor;
	private Composite container;
	private Composite panel;
	private Resizer resizer;
	private Label mainLabel;
	private LCProgressBar mainBar;
	private Label subLabel;
	private Label timingLabel;
	private SubWorkPanel subWorksPanel;
	private ScrolledComposite scroll;
	private Composite customPanel;
	private Control customSepBegin, customSepEnd;
	
	public Composite getControl() { return panel; }

	public static interface Resizer {
		public void resize();
	}
	
	public Composite create(Composite container, Resizer resizer) {
		this.container = container;
		this.resizer = resizer;
		panel = new Composite(container, SWT.NONE);
		UIUtil.gridLayout(panel, 1);
		GridData gd;
		
		mainLabel = UIUtil.newLabel(panel, progress.getDescription() + "...");
		UIUtil.gridDataHorizFill(mainLabel);
		mainBar = new LCProgressBar(panel, LCProgressBar.Style.ROUND, mainBarColor);
		UIUtil.gridDataHorizFill(mainBar);
		mainBar.setMinimum(0);
		mainBar.setMaximum(progress.getAmount());
		subLabel = new Label(panel, SWT.NONE);
		gd = UIUtil.gridDataHorizFill(subLabel);
		if (progress.getSubDescription() != null) {
			subLabel.setText(progress.getSubDescription());
		} else {
			gd.exclude = true;
		}
		timingLabel = new Label(panel, SWT.NONE);
		gd = UIUtil.gridDataHorizFill(timingLabel);
		gd.exclude = true;

		Label sep = new Label(panel, SWT.SEPARATOR | SWT.HORIZONTAL);
		gd = UIUtil.gridData(1, true, 1, false);
		gd.verticalIndent = 10;
		sep.setLayoutData(gd);
		
		scroll = new ScrolledComposite(panel, SWT.V_SCROLL);
		subWorksPanel = new SubWorkPanel(scroll, progress, subBarsColor, false);
		scroll.setContent(subWorksPanel);
		gd = UIUtil.gridData(1, true, 1, true);
		gd.verticalIndent = 10;
		scroll.setLayoutData(gd);
		scroll.setExpandHorizontal(true);
		
		customSepBegin = UIUtil.newSeparator(panel, true, true);
		customPanel = new Composite(panel, SWT.NONE);
		UIUtil.gridLayout(customPanel, 1);
		customSepEnd = UIUtil.newSeparator(panel, true, true);
		gd = (GridData)customSepBegin.getLayoutData();
		gd.exclude = true;
		customSepBegin.setLayoutData(gd);
		gd = new GridData();
		gd.exclude = true;
		gd.horizontalAlignment = SWT.CENTER;
		customPanel.setLayoutData(gd);
		gd = (GridData)customSepEnd.getLayoutData();
		gd.exclude = true;
		customSepEnd.setLayoutData(gd);
		
		resize();
		
		if (progress.isCancellable()) {
			ImageAndTextButton button = new ImageAndTextButton(panel, SharedImages.getImage(SharedImages.icons.x16.basic.CANCEL), "Cancel");
			button.addClickListener(new Listener<MouseEvent>() {
				public void fire(MouseEvent event) {
					progress.cancel();
				}
			});
			button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		}
		
		fire(progress);
		
		return panel;
	}

	private boolean refreshFired = false;
	private long lastTimeRefreshed = 0;
	public void fire(WorkProgress event) {
		if (container.isDisposed()) return;
		if (timeEstimate == null)
			timeEstimate = new SmartTimeEstimation(progress.getAmount());
		if (Thread.currentThread() == container.getDisplay().getThread()) {
			refresher.run();
		} else {
			if (lastTimeRefreshed == 0 || System.currentTimeMillis() - lastTimeRefreshed > 5000)
				UIUtil.runPendingEvents(container.getDisplay());
			synchronized (this) {
				if (refreshFired) return;
				refreshFired = true;
			}
			container.getDisplay().asyncExec(new Runnable() {
				public void run() {
					synchronized(EmbeddedWorkProgressControl.this) {
						refreshFired = false;
						lastTimeRefreshed = System.currentTimeMillis();
					}
					refresher.run();
				}				
			});
		}
	}
	
	public Composite getCustomizePanel() { return customPanel; }
	public void ensureCustomPanelVisibleIfNeeded() {
		if (customPanel.getChildren().length == 0) return;
		if (!customShown) {
			if (customPanel.getChildren().length != 0) {
				customShown = true;
				GridData gd;
				gd = (GridData)customPanel.getLayoutData(); gd.exclude = false;
				gd = (GridData)customSepBegin.getLayoutData(); gd.exclude = false;
				gd = (GridData)customSepEnd.getLayoutData(); gd.exclude = false;
				resize();
			}
		}
	}
	
	public void forceRefresh() {
		startTime = 0;
		if (container.isDisposed()) return;
		if (Thread.currentThread() == container.getDisplay().getThread()) {
			refresher.run();
		} else {
			container.getDisplay().asyncExec(new Runnable() {
				public void run() {
					refresher.run();
				}				
			});
		}
	}
	
	private boolean customShown = false;
	
	private long startTime = 0;
	private SmartTimeEstimation timeEstimate;
	private long[] refreshUpdate = new long[] { 5*1000, 15*1000, 30*1000, 60*1000 };
	private long[] mainRefreshTime = new long[] { 0, 200, 500, 750, 1000 };
	private long[] subRefreshTime = new long[] { 100, 400, 1000, 1500, 3000 };
	private int refreshPos = 0;
	private Runnable refresher = new Runnable() {
		private long lastRefreshMain = 0;
		private long lastRefreshSub = 0;
		public void run() {
			if (mainLabel.isDisposed()) return;
			long time = System.currentTimeMillis();
			if (startTime == 0) {
				startTime = time;
				lastRefreshSub = 0;
			} else {
				if (refreshPos < 4 && time > startTime + refreshUpdate[refreshPos])
					refreshPos++;
				if (time - lastRefreshMain < mainRefreshTime[refreshPos]) return;
			}
			if (!customShown) {
				if (customPanel.getChildren().length != 0) {
					customShown = true;
					GridData gd;
					gd = (GridData)customPanel.getLayoutData(); gd.exclude = false;
					gd = (GridData)customSepBegin.getLayoutData(); gd.exclude = false;
					gd = (GridData)customSepEnd.getLayoutData(); gd.exclude = false;
					resize();
				}
			} else {
				if (customPanel.getChildren().length == 0) {
					customShown = false;
					GridData gd;
					gd = (GridData)customPanel.getLayoutData(); gd.exclude = true;
					gd = (GridData)customSepBegin.getLayoutData(); gd.exclude = true;
					gd = (GridData)customSepEnd.getLayoutData(); gd.exclude = true;
					resize();
				}
			}
			boolean needResize = false;
			String txt = progress.getDescription() + "... " + (progress.getAmount() > 0 ? (progress.getPosition()*100/progress.getAmount()) : 0) + "%";
			if (!mainLabel.getText().equals(txt)) {
				mainLabel.setText(txt);
				needResize = true;
			}
			int amount = progress.getAmount();
			if (mainBar.getMaximum() != amount)
				mainBar.setMaximum(amount);
			int pos = progress.getPosition(); 
			if (mainBar.getPosition() != pos)
				mainBar.setPosition(pos);
			txt = progress.getSubDescription();
			GridData gd = (GridData)subLabel.getLayoutData();
			if (txt == null) {
				if (!gd.exclude) {
					subLabel.setText("");
					gd.exclude = true;
				}
			} else {
				if (gd.exclude) {
					subLabel.setText(txt);
					gd.exclude = false;
				} else {
					if (!subLabel.getText().equals(txt)) {
						subLabel.setText(txt);
						needResize = true;
					}
				}
			}
			timeEstimate.signalProgress(pos, amount);
			if (time - timeEstimate.getStartTime() > 10*1000) {
				gd = (GridData)timingLabel.getLayoutData();
				if (gd.exclude) needResize = true;
				gd.exclude = false;
				String text = 
					Local.Elapsed_time+": " + DateTimeUtil.getTimeMinimalString(time-timeEstimate.getStartTime()) +
					", " +
					Local.Estimated_remaining_time+": " + DateTimeUtil.getTimeMinimalString(timeEstimate.getEstimationRemainingTime())
				;
				timingLabel.setText(text);
			}
			lastRefreshMain = time;
			if (time - lastRefreshSub > subRefreshTime[refreshPos]) {
				if (subWorksPanel.refresh()) {
					subWorksPanel.layout(true, true);
					needResize = true;
				}
				lastRefreshSub = time;
			}
			if (needResize) resize();
			UIUtil.runPendingEvents(container.getDisplay());
		}
	};
	
	private int maxWidth = 0;

	public void resize() {
		Point size = subWorksPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		subWorksPanel.setSize(size);
		GridData gd = (GridData)scroll.getLayoutData();
		gd.heightHint = size.y > 300 ? 300 : size.y;
		gd.widthHint = size.x < 300 ? 300 : size.x > 800 ? 800 : size.x;
		if (gd.widthHint < maxWidth)
			gd.widthHint = maxWidth;
		else if (gd.widthHint > maxWidth)
			maxWidth = gd.widthHint;
		resizer.resize();
	}
	
	private static class SubWorkPanel extends Composite {
		SubWorkPanel(Composite parent, WorkProgress progress, Color progressBarColor, boolean shown) {
			super(parent, SWT.NONE);
			this.progress = progress;
			this.progressBarColor = progressBarColor;
			GridLayout layout = UIUtil.gridLayout(this, 2);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 0;
			GridData gd;

			
			if (shown) {
				header_icon = new Label(this, SWT.NONE);
				header_icon.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.WAIT_CLOCK));
				gd = new GridData();
				gd.verticalSpan = 2;
				gd.verticalAlignment = SWT.TOP;
				header_icon.setLayoutData(gd);
			
				header = new Composite(this, SWT.NONE);
				layout = UIUtil.gridLayout(header, 1);
				layout.marginHeight = 0;
				layout.marginWidth = 0;
				layout.marginBottom = 3;
				layout.verticalSpacing = 0;
				gd = UIUtil.gridData(1, true, 1, false);
				header.setLayoutData(gd);
			}
			
			body = new Composite(this, SWT.NONE);
			layout = UIUtil.gridLayout(body, 1);
			layout.marginLeft = 0;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.verticalSpacing = 3;
			layout.horizontalSpacing = 0;
			body.setSize(0, 0);
			gd = UIUtil.gridData(1, true, 1, false);
			gd.heightHint = 1;
			body.setLayoutData(gd);
			
			if (shown) {
				header_text = new Label(header, SWT.NONE);
				header_text.setText(progress.getDescription());
				header_text.setLayoutData(UIUtil.gridDataHoriz(1, true));
				UIControlUtil.increaseFontSize(header_text, -1);
				header_bar = new LCProgressBar(header, LCProgressBar.Style.ROUND, progressBarColor);
				header_bar.setMinimum(0);
				header_bar.setMaximum(progress.getAmount());
				gd = new GridData();
				gd.exclude = true;
				gd.horizontalAlignment = SWT.FILL;
				gd.grabExcessHorizontalSpace = true;
				gd.heightHint = 10;
				header_bar.setLayoutData(gd);
				header_subtext = new Label(header, SWT.NONE);
				gd = new GridData();
				gd.exclude = true;
				gd.horizontalAlignment = SWT.FILL;
				gd.grabExcessHorizontalSpace = true;
				header_subtext.setLayoutData(gd);
				UIControlUtil.increaseFontSize(header_subtext, -1);
			}
		}
		private WorkProgress progress;
		private Color progressBarColor;
		private Composite header = null;
		private Composite body;
		private Label header_icon;
		private Label header_text;
		private LCProgressBar header_bar;
		private Label header_subtext;
		private boolean lastWaiting = true;
		private boolean lastFinished = false;
		private int lastPos = 0;
		private boolean refresh() {
			boolean changed = false;
			List<SubWorkPanel> subPanels = new LinkedList<SubWorkPanel>();
			for (Control c : body.getChildren())
				if (c instanceof SubWorkPanel)
					subPanels.add((SubWorkPanel)c);
			SubWorkPanel previous = null;
			for (WorkProgress subWork : progress.getSubWorks()) {
				if (subWork.getDescription() == null) continue;
				SubWorkPanel panel = null;
				for (SubWorkPanel p : subPanels)
					if (p.progress == subWork) {
						panel = p;
						break;
					}
				if (panel == null) {
					panel = new SubWorkPanel(body, subWork, progressBarColor, true);
					GridData gd = (GridData)body.getLayoutData();
					gd.heightHint = SWT.DEFAULT;
					UIUtil.gridDataHorizFill(panel);
					if (previous != null)
						panel.moveBelow(previous);
					else
						panel.moveAbove(null);
					changed = true;
				} else
					subPanels.remove(panel);
				if (panel.refresh())
					changed = true;
				previous = panel;
			}
			if (!subPanels.isEmpty()) changed = true;
			for (SubWorkPanel p : subPanels)
				p.dispose();
			
			if (header != null) {
				if (progress.isStarted() && !progress.isFinished()) {
					if (lastWaiting) {
						lastWaiting = false;
						header_icon.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.PROCESSING));
						GridData gd = (GridData)header_bar.getLayoutData();
						gd.exclude = false;
						String s = progress.getSubDescription();
						if (s != null) {
							gd = (GridData)header_subtext.getLayoutData();
							gd.exclude = false;
							header_subtext.setText(s);
							changed = true;
						}
					} else {
						int amount = progress.getAmount();
						if (header_bar.getMaximum() != amount)
							header_bar.setMaximum(amount);
						int pos = progress.getPosition();
						if (lastPos != pos) {
							lastPos = pos;
							header_bar.setPosition(pos);
						}
						String s = progress.getSubDescription();
						GridData gd = (GridData)header_subtext.getLayoutData();
						if (s == null) {
							if (!gd.exclude) {
								gd.exclude = true;
								changed = false;
							}
						} else {
							if (gd.exclude) {
								gd.exclude = false;
								changed = true;
							}
							if (!header_subtext.getText().equals(s)) {
								header_subtext.setText(s);
								changed = true;
							}
						}
					}
				} else if (progress.isFinished()) {
					if (!lastFinished) {
						lastFinished = true;
						header_icon.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.VALIDATE));
						GridData gd = (GridData)header_bar.getLayoutData();
						gd.exclude = true;
						gd = (GridData)header_subtext.getLayoutData();
						gd.exclude = true;
						header_bar.setVisible(false);
						gd = (GridData)body.getLayoutData();
						gd.heightHint = 0;
						changed = true;
					}
				} else {
					if (!lastWaiting) {
						lastWaiting = true;
						header_icon.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.WAIT_CLOCK));
						GridData gd = (GridData)header_bar.getLayoutData();
						gd.exclude = true;
						gd = (GridData)header_subtext.getLayoutData();
						gd.exclude = true;
					}
				}
			}
			
			return changed;
		}
	}
}
