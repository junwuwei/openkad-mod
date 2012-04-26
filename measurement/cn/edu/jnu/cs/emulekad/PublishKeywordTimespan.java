/**
 * 
 */
package cn.edu.jnu.cs.emulekad;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.util.NodesDatFile;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class PublishKeywordTimespan {
	private final EMuleKad eMuleKad;
	private final KeyFactory keyFactory;
	private final PublishHelper publishHelper;

	private BlockingQueue<PublishRecord> publishRecords = new LinkedBlockingQueue<PublishRecord>();
	private BlockingQueue<PublishRecord> vanishedRecords = new LinkedBlockingQueue<PublishRecord>();
	private CountDownLatch latch;
	private int nrPublished;

	private static Logger logger = LoggerFactory
			.getLogger(PublishKeywordTimespan.class);

	@Inject
	public PublishKeywordTimespan(EMuleKad eMuleKad, KeyFactory keyFactory,
			PublishHelper publishHelper) {
		this.eMuleKad = eMuleKad;
		this.keyFactory = keyFactory;
		this.publishHelper = publishHelper;
	}

	public void doPublish(final int nrPublish, int nrThread) {
		final String vanishString = "publish keyword timespan measurement";
		final Entry entry = publishHelper.makeKeywordEntry(vanishString);
		final CountDownLatch latch = new CountDownLatch(nrThread);
		for (int i = 0; i < nrThread; i++) {
			Thread thread = new Thread() {
				public void run() {
					for (int i = 0; i < nrPublish; i++) {
						Key targetKey = keyFactory.generate();
						int nrCompleted = eMuleKad.publishKeyword(targetKey,
								Arrays.asList(entry));
						if (nrCompleted > 0) {
							PublishRecord record = new PublishRecord(targetKey,
									vanishString, System.currentTimeMillis(),
									nrCompleted);
							publishRecords.add(record);
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
		nrPublished = publishRecords.size();
		logger.info("nrPublished={}", nrPublished);
	}

	public void doSearchTillAllVanish(int nrThread, long step, long period) {
		latch = new CountDownLatch(nrPublished);
		Timer[] timers = new Timer[nrThread];
		for (int i = 0; i < nrThread; i++) {
			timers[i] = new Timer(true);
		}
		int i = 0;
		for (PublishRecord record : publishRecords) {
			TimerTask task = createTimerTask(record);
			timers[++i % nrThread].schedule(task, i * step, period);
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for(Timer timer:timers){
			timer.cancel();
		}

		double averageTimespan = 0;
		for (PublishRecord record : vanishedRecords) {
			averageTimespan += record.getTimespan(TimeUnit.HOURS);
		}
		averageTimespan /= nrPublished;
		logger.info("\naverage timespan={}", averageTimespan);
	}

	private TimerTask createTimerTask(final PublishRecord r) {
		return new TimerTask() {
			PublishRecord record = r;

			public void run() {
				boolean isVanished = checkVanishStatus();
				if (isVanished) {
					try {
						logger.info("{}", record);
						vanishedRecords.put(record);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// logger.debug("nrVanished={}",
					// vanishedRecords.size());
					latch.countDown();
					this.cancel();
				}
			}

			private boolean checkVanishStatus() {
				record.nrSearch++;
				record.lastSearchTime = System.currentTimeMillis();
				List<Entry> entries = eMuleKad.searchKeyword(record.targetKey);

				for (Entry entry : entries) {
					String retrievedString = publishHelper
							.getVanishString(entry);
					if (record.vanishString.equals(retrievedString)) {
						return false;
					}
				}

				record.vanishTime = System.currentTimeMillis();
				return true;
			}
		};

	}

	public BlockingQueue<PublishRecord> getPublishRecords() {
		return publishRecords;
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator config = new JoranConfigurator();
		lc.reset();
		config.setContext(lc);
		try {
			config.doConfigure(PublishKeywordTimespan.class.getClassLoader()
					.getResourceAsStream("publishKeyword-logback.xml"));
		} catch (JoranException e) {
			e.printStackTrace();
		}

		// StatusPrinter.print(lc);

		Injector injector = Guice
				.createInjector(new EMuleKadModule()
						.setProperty("openkad.refresh.enable", false + "")
						.setProperty("openkad.bootstrap.do_rendom_findnode",
								false + "")
						.setProperty("openkad.net.udp.port", "10001")
						.setProperty("openkad.bootstrap.ping_befor_insert",
								false + "").setProperty("openkad.seed", "0"));

		EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
		// FakeEMuleKad eMuleKad = new FakeEMuleKad(OperationResult.SUCCESS);
		eMuleKad.create();
		NodesDatFile nodesDatFile = injector.getInstance(NodesDatFile.class);
		eMuleKad.joinNode(nodesDatFile.readNodeFromFile());

		PublishKeywordTimespan measurement = injector
				.getInstance(PublishKeywordTimespan.class);
		// KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
		// PublishHelper publishHelper =
		// injector.getInstance(PublishHelper.class);
		// PublishKeywordTimespan measurement = new
		// PublishKeywordTimespan(eMuleKad,
		// keyFactory, publishHelper);

		measurement.doPublish(50, 2);
		 TimeUnit.MINUTES.sleep(1);
		// eMuleKad.setOperationResult(OperationResult.FAIL);
		measurement.doSearchTillAllVanish(2,TimeUnit.SECONDS.toMillis(3),TimeUnit.MINUTES.toMillis(2));

		eMuleKad.shutdown();
	}
}
