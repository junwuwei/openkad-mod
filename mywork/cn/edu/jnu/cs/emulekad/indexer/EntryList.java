package cn.edu.jnu.cs.emulekad.indexer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class EntryList extends CopyOnWriteArrayList<Entry> {
	private static final long serialVersionUID = 2090624259697003505L;

	public void cleanTimeOutEntry(long timespan) {
		cleanTimeOutEntry(timespan,null);
	}

	public void cleanTimeOutEntry(long timespan,AtomicInteger totalCount){
		List<Entry> needClean=new LinkedList<Entry>();
		for(Entry entry:this){
			if(entry.getCreationTime()+timespan>System.currentTimeMillis()){
				needClean.add(entry);
			}
		}
		if(removeAll(needClean) && totalCount!=null){
			int expect;
			do {
				expect=totalCount.get();
			} while (!totalCount.compareAndSet(expect, expect-needClean.size()));
		}
	}
}
