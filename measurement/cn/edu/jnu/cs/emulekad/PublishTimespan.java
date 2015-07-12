/**
 * 
 */
package cn.edu.jnu.cs.emulekad;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.msg.PublishAndSearchType;

import com.google.inject.Inject;

/**
 * @author Zhike Chan (zk.chan007@gmail.com)
 * $create on: 2012-5-14
 */
public class PublishTimespan {
	private final EMuleKad eMuleKad;
	private final KeyFactory keyFactory;
	private final PublishHelper publishHelper;
	//当record连续vanishTolerance次搜索结果都失败时，record才被定义为消失。
	private final PublishAndSearchType type;
	private int vanishTolerance;
	private List<Timer> timers;
	private long searchInterval;
	private AtomicInteger nrPublished;
	private AtomicInteger nrVanished;
	private Random rnd;

	private BlockingQueue<PublishRecord> publishRecords = new LinkedBlockingQueue<PublishRecord>();

	private static Logger logger = LoggerFactory
			.getLogger(PublishTimespan.class);

	@Inject
	public PublishTimespan(EMuleKad eMuleKad, KeyFactory keyFactory,PublishAndSearchType type,
			PublishHelper publishHelper,int vanishTolerance,int nrSearchTimer,long searchInterval) {
		this.eMuleKad = eMuleKad;
		this.keyFactory = keyFactory;
		this.type=type;
		this.publishHelper = publishHelper;
		this.vanishTolerance=vanishTolerance;
		this.nrPublished = new AtomicInteger(0);
		this.nrVanished = new AtomicInteger(0);
		this.rnd = new Random();
		this.timers=new ArrayList<Timer>();
		for (int i = 1; i <= nrSearchTimer; i++) {
			this.timers.add(new Timer("SearchTimer"+i,true));
		}
		this.searchInterval=searchInterval;
	}

	public void doPublish(final int nrPublish, int nrThread) {
		final String vanishString = "publish timespan measurement";
		final CountDownLatch latch = new CountDownLatch(nrThread);
		for (int i = 0; i < nrThread; i++) {
			Thread thread = new Thread() {
				public void run() {
					for (int i = 0; i < nrPublish; i++) {
						Key targetKey = keyFactory.generate();
						int nrCompleted =0;
						switch(type){
						case KEYWORD:
							nrCompleted=eMuleKad.publishKeyword(targetKey,
								Arrays.asList(publishHelper.makeKeywordEntry(vanishString)));
							break;
						case NOTE:
							nrCompleted=eMuleKad.publishNote(targetKey,publishHelper.makeNoteEntry(vanishString));
							break;
						case SOURCE:
							nrCompleted=eMuleKad.publishSource(targetKey,publishHelper.makeSourceEntry(vanishString));
							break;
						}
						if (nrCompleted > 0) {
							PublishRecord record = new PublishRecord(targetKey,
									vanishString, System.currentTimeMillis(),
									nrCompleted);
							publishRecords.add(record);
							nrPublished.incrementAndGet();
							TimerTask searchTask=createTimerTask(record);
							timers.get(rnd.nextInt(timers.size())).schedule(searchTask, searchInterval, searchInterval);
						}
					}
					latch.countDown();
				}
			};
			thread.start();
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("nrPublished={}", nrPublished);
	}

	public void waitForAllRecordsVanished() throws InterruptedException {
		if(nrVanished.get()<nrPublished.get()){
			synchronized(this){
				wait();
			}
		}
		for (Timer timer : timers) {
			timer.cancel();
		}

		double averageTimespan = 0;
		for (PublishRecord record : publishRecords) {
			averageTimespan += record.getTimespan(TimeUnit.HOURS);
		}
		averageTimespan /= nrPublished.get();
		logger.info("nrPublished={}", nrPublished);
		logger.info("average timespan={}\n", averageTimespan);
	}
	
	private TimerTask createTimerTask(final PublishRecord r) {
		return new TimerTask() {
			PublishRecord record = r;

			public void run() {
				boolean isVanished = checkVanishStatus();

				if (isVanished) {
					logger.info("{}", record);
					nrVanished.incrementAndGet();
					// logger.debug("nrVanished={}",
					// vanishedRecords.size());
					if(nrVanished.get()>=nrPublished.get()){
						synchronized(PublishTimespan.this){
							PublishTimespan.this.notify();
						}
					}
					this.cancel();
				}
			}

			private boolean checkVanishStatus() {
				record.nrSearch++;
				record.lastSearchTime = System.currentTimeMillis();
				List<Entry> entries=null;
				switch(type){
				case KEYWORD:
					entries = eMuleKad.searchKeyword(record.targetKey);
					break;
				case NOTE:
					entries = eMuleKad.searchNote(record.targetKey);
					break;
				case SOURCE:
					entries = eMuleKad.searchSource(record.targetKey);
					break;
				}

				for (Entry entry : entries) {
					String retrievedString = publishHelper
							.getVanishString(entry);
					if (record.vanishString.equals(retrievedString)) {
						record.nrContinuousVanish=0;
						return false;
					}
				}
				record.nrContinuousVanish++;
				//记录第一次取回密钥失败的时间
				if(record.nrContinuousVanish==1){
					record.vanishTime = System.currentTimeMillis();
				}
				//当record连续vanishTolerance次取回密钥都失败时，record才被定义为消失。
				if(record.nrContinuousVanish < vanishTolerance){
					return false;
				}else{
					return true;
				}
			}
		};

	}

	public BlockingQueue<PublishRecord> getPublishRecords() {
		return publishRecords;
	}
}
