/**
 * 
 */
package cn.edu.jnu.cs.emulekad.indexer;

import il.technion.ewolf.kbr.Key;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class Indexer {
	private Map<Key, EntryList> sources = new ConcurrentHashMap<Key, EntryList>();
	private Map<Key, EntryList> keywords = new ConcurrentHashMap<Key, EntryList>();
	private Map<Key, EntryList> notes = new ConcurrentHashMap<Key, EntryList>();

	// dependencies
	private final int MAX_SOURCE_PER_FILE;
	private final int MAX_NOTE_PER_FILE;
	private final int MAX_KEYWORD_ENTRIES;
	private final int MAX_KEYWORD_INDEXES;
	private final long SOURCE_PUBLISH_TIMESPAN;
	private final long KEYWORD_PUBLISH_TIMESPAN;
	private final long NOTE_PUBLISH_TIMESPAN;
	private final long cleanTaskFirstDelay;
	private final long cleanTaskInterval;
	private final Timer timer;

	private AtomicInteger totalKeywordEntries = new AtomicInteger(0);
	private AtomicInteger totalKeywordIndexes = new AtomicInteger(0);

	@Inject
	public Indexer(
			@Named("openkad.index.max_source_per_file") int MAX_SOURCE_PER_FILE,
			@Named("openkad.index.max_note_per_file") int MAX_NOTE_PER_FILE,
			@Named("openkad.index.max_keyword_entries") int MAX_KEYWORD_ENTRIES,
			@Named("openkad.index.max_keyword_indexes") int MAX_KEYWORD_INDEXES,
			@Named("openkad.index.source.publish.timespan") long SOURCE_PUBLISH_TIMESPAN,
			@Named("openkad.index.keyword.publish.timespan") long KEYWORD_PUBLISH_TIMESPAN,
			@Named("openkad.index.note.publish.timespan") long NOTE_PUBLISH_TIMESPAN,
			@Named("openkad.index.cleantask.first_delay") long cleanTaskFirstDelay,
			@Named("openkad.index.cleantask.interval") long cleanTaskInterval){
//			@Named("openkad.timer") Timer timer) {

		this.MAX_SOURCE_PER_FILE = MAX_SOURCE_PER_FILE;
		this.MAX_NOTE_PER_FILE = MAX_NOTE_PER_FILE;
		this.MAX_KEYWORD_ENTRIES = MAX_KEYWORD_ENTRIES;
		this.MAX_KEYWORD_INDEXES = MAX_KEYWORD_INDEXES;
		this.SOURCE_PUBLISH_TIMESPAN = SOURCE_PUBLISH_TIMESPAN;
		this.KEYWORD_PUBLISH_TIMESPAN = KEYWORD_PUBLISH_TIMESPAN;
		this.NOTE_PUBLISH_TIMESPAN = NOTE_PUBLISH_TIMESPAN;
		this.cleanTaskFirstDelay = cleanTaskInterval;
		this.cleanTaskInterval = cleanTaskInterval;
//		this.timer = timer;
		this.timer=new Timer("IndexerCleanTaskTimer",true);
	}

	public int addSource(Key sourceKey, Entry entry) {
		EntryList entryList = sources.get(sourceKey);
		if (entryList == null) {
			entryList = new EntryList();
			sources.put(sourceKey, entryList);
		}

		int i = -1;
		if ((i = entryList.indexOf(entry)) >= 0) {
			Entry oldEntry = entryList.get(i);
			synchronized (oldEntry) {
				oldEntry = entry;
			}
		} else if (entryList.size() > MAX_SOURCE_PER_FILE) {
			return 100;
		} else {
			entryList.add(entry);
		}
		return 100 * entryList.size() / MAX_SOURCE_PER_FILE;
	}

	public int addKeyword(Key keywordKey, Entry entry) {
		EntryList entryList = keywords.get(keywordKey);
		if (entryList == null) {
			if (totalKeywordIndexes.get() > MAX_KEYWORD_INDEXES) {
				return 100;
			}
			entryList = new EntryList();
			keywords.put(keywordKey, entryList);
			totalKeywordIndexes.incrementAndGet();
		}

		int i = -1;
		if ((i = entryList.indexOf(entry)) >= 0) {
			Entry oldEntry = entryList.get(i);
			synchronized (oldEntry) {
				oldEntry = entry;
			}
		} else if (totalKeywordEntries.get() > MAX_KEYWORD_ENTRIES) {
			return 100;
		} else {
			entryList.add(entry);
			totalKeywordEntries.incrementAndGet();
		}
		return 100 * totalKeywordIndexes.get() / MAX_KEYWORD_INDEXES;
	}

	public int addNote(Key noteKey, Entry entry) {
		EntryList entryList = sources.get(noteKey);
		if (entryList == null) {
			entryList = new EntryList();
			sources.put(noteKey, entryList);
		}

		int i = -1;
		if ((i = entryList.indexOf(entry)) >= 0) {
			Entry oldEntry = entryList.get(i);
			synchronized (oldEntry) {
				oldEntry = entry;
			}
		} else if (entryList.size() > MAX_NOTE_PER_FILE) {
			return 100;
		} else {
			entryList.add(entry);
		}
		return 100 * entryList.size() / MAX_NOTE_PER_FILE;
	}

	public List<Entry> getSources(Key targetKey) {
		List<Entry> result = sources.get(targetKey);
		return result == null ? null : Collections.unmodifiableList(result);
	}

	public List<Entry> getKeywords(Key targetKey) {
		List<Entry> result = keywords.get(targetKey);
		return result == null ? null : Collections.unmodifiableList(result);
	}

	public List<Entry> getNotes(Key targetKey) {
		List<Entry> result = notes.get(targetKey);
		return result == null ? null : Collections.unmodifiableList(result);
	}

	private void cleanTimeOutEntries() {
		for (Key key : sources.keySet()) {
			sources.get(key).cleanTimeOutEntry(SOURCE_PUBLISH_TIMESPAN);
		}
		for (Key key : keywords.keySet()) {
			keywords.get(key).cleanTimeOutEntry(KEYWORD_PUBLISH_TIMESPAN,
					totalKeywordEntries);
		}
		for (Key key : notes.keySet()) {
			notes.get(key).cleanTimeOutEntry(NOTE_PUBLISH_TIMESPAN);
		}
	}

	public void scheduleCleanTask() {
		TimerTask cleanTask = new TimerTask() {
			public void run() {
				cleanTimeOutEntries();
			}
		};
		timer.schedule(cleanTask, this.cleanTaskFirstDelay,
				this.cleanTaskInterval);
	}
}
