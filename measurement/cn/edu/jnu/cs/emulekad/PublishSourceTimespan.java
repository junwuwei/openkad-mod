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
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.util.NodesDatFile;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 *
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class PublishSourceTimespan {
	private final EMuleKad eMuleKad;
	private final KeyFactory keyFactory;
	private final PublishHelper publishHelper;

	private BlockingQueue<PublishRecord> publishRecords = new LinkedBlockingQueue<PublishRecord>();
	private AtomicInteger nrVanished = new AtomicInteger(0);
	private CountDownLatch latch;
	private int nrPublished;

	private static Logger logger = LoggerFactory
			.getLogger(PublishSourceTimespan.class);

	@Inject
	public PublishSourceTimespan(EMuleKad eMuleKad,
			KeyFactory keyFactory, PublishHelper publishHelper) {
		this.eMuleKad = eMuleKad;
		this.keyFactory = keyFactory;
		this.publishHelper = publishHelper;
	}

	public void doPublish(int nrPublish) {
		String vanishString = "publish source timespan measurement";
		Entry entry = publishHelper.makeSourceEntry(vanishString);

		for (int i = 0; i < nrPublish; i++) {
			Key targetKey = keyFactory.generate();
			int nrCompleted = eMuleKad.publishSource(targetKey, entry);
			if (nrCompleted > 0) {
				PublishRecord record = new PublishRecord(targetKey,
						vanishString, System.currentTimeMillis(), nrCompleted);
				publishRecords.add(record);
			}
		}
	}

	public void doSearchTillAllVanish(int nrThread) {
		nrPublished = publishRecords.size();
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
		for (PublishRecord record : publishRecords) {
			logger.info("{}", record);
			averageTimespan += record.getTimespan(TimeUnit.HOURS);
		}
		averageTimespan /= nrPublished;
		logger.info("\naverage timespan={}", averageTimespan);
	}

	private Thread creatSearchThread() {
		return new Thread() {
			public void run() {
				while (nrVanished.get() < nrPublished) {
					PublishRecord record = null;
					try {
						record = publishRecords.take();
					} catch (InterruptedException e) {
						logger.error("{}", e);
					}
					if (record != null && record.vanishTime == 0) {
						List<Entry> entries = eMuleKad
								.searchSource(record.targetKey);
						boolean vanished = true;
						for (Entry entry : entries) {
							String retrievedString = publishHelper
									.getVanishString(entry);
							if (record.vanishString.equals(retrievedString)) {
								vanished = false;
								break;
							}
						}
						if (vanished) {
							record.vanishTime = System.currentTimeMillis();
							nrVanished.incrementAndGet();
							latch.countDown();
						}
						publishRecords.add(record);
					}
				}
			}
		};

	}

	public BlockingQueue<PublishRecord> getPublishRecords() {
		return publishRecords;
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator config = new JoranConfigurator();
		lc.reset();
		config.setContext(lc);
		try {
			config.doConfigure(PublishSourceTimespan.class.getClassLoader()
					.getResourceAsStream("publishSource-logback.xml"));
		} catch (JoranException e) {
			e.printStackTrace();
		}

		StatusPrinter.print(lc);

		Injector injector = Guice
				.createInjector(new EMuleKadModule()
						.setProperty("openkad.refresh.enable", false + "")
						.setProperty("openkad.bootstrap.do_rendom_findnode",
								false + "")
						.setProperty("openkad.net.udp.port", "10000")
						.setProperty("openkad.bootstrap.ping_befor_insert",
								false + "").setProperty("openkad.seed", "0"));

		EMuleKadNet eMuleKad = injector.getInstance(EMuleKadNet.class);
		eMuleKad.create();
		NodesDatFile nodesDatFile = injector.getInstance(NodesDatFile.class);
		eMuleKad.joinNode(nodesDatFile.readNodeFromFile());

		PublishSourceTimespan measurement = injector
				.getInstance(PublishSourceTimespan.class);
		
		measurement.doPublish(100);
		TimeUnit.HOURS.sleep(2);
		measurement.doSearchTillAllVanish(10);
		
		eMuleKad.shutdown();
	}

}
