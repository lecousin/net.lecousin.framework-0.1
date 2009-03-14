package net.lecousin.framework.ui.eclipse.progress;

public class SmartTimeEstimation {

	public SmartTimeEstimation(int total) {
		this.startTime = System.currentTimeMillis();
		this.total = total;
		prevTime = time_20seconds = time_40seconds = time_1minute = startTime;
	}
	
	private long startTime;
	private int total;
	private int prevPos = 0;
	private long prevTime;
	
	private double progress_20seconds = 0;
	private double progress_40seconds = 0;
	private double progress_1minute = 0;
	
	private long time_20seconds;
	private int pos_20seconds = 0;
	private long time_40seconds;
	private int pos_40seconds = 0;
	private long time_1minute;
	private int pos_1minute = 0;
	
	public void signalProgress(int pos, int total) {
		if (pos == prevPos && total == this.total) return;
		long time = System.currentTimeMillis();
		if (pos != prevPos) {
			if (time - time_20seconds >= 20000) {
				progress_20seconds = (double)((pos-pos_20seconds)*1000)/(double)((time-time_20seconds));
				time_20seconds = time;
				pos_20seconds = pos;
			}
			if (time - time_40seconds >= 40000) {
				progress_40seconds = (double)((pos-pos_40seconds)*1000)/(double)((time-time_40seconds));
				time_40seconds = time;
				pos_40seconds = pos;
			}
			if (time - time_1minute >= 60000) {
				progress_1minute = (double)((pos-pos_1minute)*1000)/(double)((time-time_1minute));
				time_1minute = time;
				pos_1minute = pos;
			}
		}
		this.total = total;
		this.prevPos = pos;
		this.prevTime = time;
	}
	
	public long getEstimationTotalTime() {
		long result = 0;
		int factor = 0;
		if (prevPos == 0) return 0;

		int factorTotal;
		int factor20s;
		int factor40s;
		int factor1m;

		if (prevTime - startTime >= 120000) {
			factorTotal = 1;
			factor1m = 10;
			factor40s = 5;
			factor20s = 4;
		} else if (prevTime - startTime >= 100000) {
			factorTotal = 1;
			factor1m = 0;
			factor40s = 5;
			factor20s = 5;
		} else if (prevTime - startTime >= 80000) {
			factorTotal = 1;
			factor1m = 0;
			factor40s = 4;
			factor20s = 5;
		} else if (prevTime - startTime >= 60000) {
			factorTotal = 1;
			factor1m = 0;
			factor40s = 0;
			factor20s = 4;
		} else if (prevTime - startTime >= 40000) {
			factorTotal = 1;
			factor1m = 0;
			factor40s = 0;
			factor20s = 5;
		} else {
			factorTotal = 1;
			factor1m = 0;
			factor40s = 0;
			factor20s = 0;
		}
		
		result += factorTotal*((prevTime-startTime)*total/prevPos);
		factor += factorTotal;
		
		if (progress_20seconds > 0.01) {
			result += factor20s*((prevTime-startTime)+(total-prevPos)*1000/progress_20seconds);
			factor += factor20s;
		}
		if (progress_40seconds > 0.01) {
			result += factor40s*((prevTime-startTime)+(total-prevPos)*1000/progress_40seconds);
			factor += factor40s;
		}
		if (progress_1minute > 0.01) {
			result += factor1m*((prevTime-startTime)+(total-prevPos)*1000/progress_1minute);
			factor += factor1m;
		}
		return result / factor;
	}
	
	public long getEstimationRemainingTime() {
		if (total == 0) return 0;
		return getEstimationTotalTime()-(prevTime-startTime);
	}
	
	public long getStartTime() { return startTime; }
}
