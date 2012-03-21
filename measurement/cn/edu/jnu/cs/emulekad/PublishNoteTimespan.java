/**
 * 
 */
package cn.edu.jnu.cs.emulekad;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;

import java.io.IOException;
import java.util.List;
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
public class PublishNoteTimespan {
	private final EMuleKad eMuleKad;
	private final KeyFactory keyFactory;
	private final PublishHelper publishHelper;

	private BlockingQueue<PublishRecord> publishRecords = new LinkedBlockingQueue<PublishRecord>();
	private BlockingQueue<PublishRecord> vanishedRecords = new LinkedBlockingQueue<PublishRecord>();
	private CountDownLatch latch;
	private int nrPublished;

	private static Logger logger = LoggerFactory
			.getLogger(PublishNoteTimespan.class);

	@Inject
	public PublishNoteTimespan(EMuleKad eMuleKad, KeyFactory keyFactory,
			PublishHelper publishHelper) {
		this.eMuleKad = eMuleKad;
		this.keyFactory = keyFactory;
		this.publishHelper = publishHelper;
	}

	public void doPublish(int nrPublish) {
		String vanishString = "publish note timespan measurement";
		Entry entry = publishHelper.makeNoteEntry(vanishString);

		for (int i = 0; i < nrPublish; i++) {
			Key targetKey = keyFactory.generate();
			int nrCompleted = eMuleKad.publishNote(targetKey, entry);
			if (nrCompleted > 0) {
				PublishRecord record = new PublishRecord(targetKey,
						vanishString, System.currentTimeMillis(), nrCompleted);
				publishRecords.add(record);
			}
		}
		nrPublished = publishRecords.size();
		logger.info("nrPublished={}", nrPublished);
	}

	public void doSearchTillAllVanish(int nrThread) {
		latch = new CountDownLatch(nrPublished);
		for (int i = 0; i < nrThread; i++) {
			Thread thread = creatSearchThread();
			thread.start();
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		double averageTimespan = 0;
		for (PublishRecord record : vanishedRecords) {
			logger.info("{}", record);
			averageTimespan += record.getTimespan(TimeUnit.HOURS);
		}
		averageTimespan /= nrPublished;
		logger.info("\naverage timespan={}", averageTimespan);
	}

	private Thread creatSearchThread() {
		return new Thread() {
			public void run() {
				while (vanishedRecords.size() < nrPublished) {
					PublishRecord record = publishRecords.poll();
					if (record == null) {
						continue;
					}
					boolean isVanished = checkVanishStatus(record);

					if (isVanished) {
						try {
							vanishedRecords.put(record);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
//						logger.debug("nrVanished={}", vanishedRecords.size());
						latch.countDown();
					} else {
						try {
							publishRecords.put(record);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}

			private boolean checkVanishStatus(PublishRecord record) {
				List<Entry> entries = eMuleKad.searchNote(record.targetKey);

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
			config.doConfigure(PublishNoteTimespan.class.getClassLoader()
					.getResourceAsStream("publishNote-logback.xml"));
		} catch (JoranException e) {
			e.printStackTrace();
		}

		// StatusPrinter.print(lc);

		Injector injector = Guice
				.createInjector(new EMuleKadModule()
						.setProperty("openkad.refresh.enable", false + "")
						.setProperty("openkad.bootstrap.do_rendom_findnode",
								false + "")
						.setProperty("openkad.net.udp.port", "10002")
						.setProperty("openkad.bootstrap.ping_befor_insert",
								false + "").setProperty("openkad.seed", "0"));

		 EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
//		FakeEMuleKad eMuleKad = new FakeEMuleKad(OperationResult.SUCCESS);
		eMuleKad.create();
		NodesDatFile nodesDatFile = injector.getInstance(NodesDatFile.class);
		eMuleKad.joinNode(nodesDatFile.readNodeFromFile());

		 PublishNoteTimespan measurement = injector
		 .getInstance(PublishNoteTimespan.class);
//		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
//		PublishHelper publishHelper = injector.getInstance(PublishHelper.class);
//		PublishNoteTimespan measurement = new PublishNoteTimespan(eMuleKad,
//				keyFactory, publishHelper);

		measurement.doPublish(500);
		 TimeUnit.HOURS.sleep(18);
//		eMuleKad.setOperationResult(OperationResult.FAIL);
		measurement.doSearchTillAllVanish(20);

		eMuleKad.shutdown();
	}

}
