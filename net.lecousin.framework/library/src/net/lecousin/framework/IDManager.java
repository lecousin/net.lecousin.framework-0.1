package net.lecousin.framework;

import java.util.ArrayList;

import net.lecousin.framework.log.Log;

public class IDManager {

	public IDManager() {
		freeRanges.add(new Pair<Long,Long>((long)0, Long.MAX_VALUE));
	}
	
	private ArrayList<Pair<Long,Long>> freeRanges = new ArrayList<Pair<Long,Long>>();
	
	public synchronized long allocate() {
		if (Log.debug(this))
			Log.debug(this, "allocate: " + freeRanges.toString());
		Pair<Long,Long> r = freeRanges.get(0);
		long id = r.getValue1();
		if (r.getValue2() == id)
			freeRanges.remove(0);
		else
			r.setValue1(id+1);
		if (Log.debug(this))
			Log.debug(this, "allocate => " + id + " => " + freeRanges.toString());
		return id;
	}
	
	public synchronized void allocate(long id) {
		for (int i = 0; i < freeRanges.size(); ++i) {
			Pair<Long,Long> p = freeRanges.get(i);
			if (id < p.getValue1() || id > p.getValue2()) continue;
			if (id == p.getValue1()) {
				if (id == p.getValue2())
					freeRanges.remove(i);
				else
					p.setValue1(id+1);
				break;
			}
			if (id == p.getValue2()) {
				if (id == p.getValue1())
					freeRanges.remove(i);
				else
					p.setValue2(id-1);
				break;
			}
			Pair<Long,Long> p2 = new Pair<Long,Long>(id+1, p.getValue2());
			p.setValue2(id-1);
			freeRanges.add(i+1, p2);
			break;
		}
	}
	
	public synchronized boolean isFree(long id) {
		for (Pair<Long,Long> range : freeRanges)
			if (id >= range.getValue1()) {
				if (id <= range.getValue2())
					return true;
			} else
				return false;
		return false;
	}
	
	public synchronized void free(long id) {
		if (Log.debug(this))
			Log.debug(this, "free " + id + ": " + freeRanges.toString());
		try {
			for (int i = 0; i < freeRanges.size(); ++i) {
				Pair<Long,Long> p = freeRanges.get(i);
				if (id >= p.getValue1() && id <= p.getValue2())
					throw new RuntimeException("ID " + id + " is already free !");
				if (id < p.getValue1()) {
					if (id == p.getValue1()-1) {
						if (i > 0 && freeRanges.get(i-1).getValue2() == id-1) {
							Pair<Long,Long> p2 = freeRanges.get(i-1);
							p2.setValue2(p.getValue2());
							freeRanges.remove(i);
							return;
						}
						p.setValue1(id);
						return;
					}
					Pair<Long,Long> p2 = new Pair<Long,Long>(id,id);
					freeRanges.add(i, p2);
					return;
				}
				if (id == p.getValue2()+1) {
					if (i < freeRanges.size()-1 && freeRanges.get(i+1).getValue1() == id+1) {
						Pair<Long,Long> p2 = freeRanges.get(i+1);
						p.setValue2(p2.getValue2());
						freeRanges.remove(i+1);
						return;
					}
					p.setValue2(id);
					return;
				}
			}
			Pair<Long,Long> p = new Pair<Long,Long>(id,id);
			freeRanges.add(p);
		} finally {
			if (Log.debug(this)) {
				Log.debug(this, "free => " + freeRanges.toString());
				/*try {
					throw new RuntimeException();
				} catch (Throwable t) {
					Log.debug(this, "Stack=", t);
				}*/
			}
		}
	}
}
