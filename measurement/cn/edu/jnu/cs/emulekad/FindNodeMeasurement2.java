/**
 * 
 */
package cn.edu.jnu.cs.emulekad;

import il.technion.ewolf.kbr.KeyFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import cn.edu.jnu.cs.emulekad.net.OpCodes;
import cn.edu.jnu.cs.emulekad.op.EMuleFindNodeOperation;
import cn.edu.jnu.cs.emulekad.util.NodesDatFile;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class FindNodeMeasurement2 {
	private Injector injector;
	private KeyFactory keyFactory;
	private EMuleKad eMuleKad;

	private static Logger logger = LoggerFactory
			.getLogger(FindNodeMeasurement2.class);

	public FindNodeMeasurement2(long timeout) throws IOException{
		injector = Guice
				.createInjector(new EMuleKadModule()
						.setProperty("openkad.refresh.enable", false + "")
						.setProperty("openkad.bootstrap.do_rendom_findnode",
								false + "")
						.setProperty("openkad.net.udp.port", "10000")
						.setProperty("openkad.net.timeout", timeout+"")
						.setProperty("openkad.bootstrap.ping_befor_insert",
								false + ""));
		keyFactory = injector.getInstance(KeyFactory.class);
		eMuleKad = injector.getInstance(EMuleKad.class);
		eMuleKad.create();
		NodesDatFile nodesDatFile = injector.getInstance(NodesDatFile.class);
		eMuleKad.joinNode(nodesDatFile.readNodeFromFile());
	}

	static class FindNodeStatistic {
		double costTime = 0;
		double nrQueried = 0;
		double longestCommonProfixLength = 0;
		String requestType = null;

		public String toString() {
			return String.format(
					"requestType=%s, average costTime=%.3f seconds, "
							+ "nrQueried=%.3f, longestCommonProfixLength=%.3f",
					requestType,costTime,
					nrQueried, longestCommonProfixLength);
		}
	}

	public FindNodeStatistic doFindNode(byte findNoteType, int nrRequest) {
		FindNodeStatistic statistic = new FindNodeStatistic();
		switch (findNoteType) {
		case OpCodes.FIND_NODE:
			statistic.requestType = "FIND_NODE";
			break;
		case OpCodes.FIND_VALUE:
			statistic.requestType = "FIND_VALUE";
			break;
		default:
			statistic.requestType = "STORE";
		}
		for (int i = 0; i < nrRequest; i++) {
			EMuleFindNodeOperation op = injector
					.getInstance(EMuleFindNodeOperation.class);
			op.setRequestType(findNoteType).setKey(keyFactory.generate())
					.doFindNode();
			statistic.costTime += TimeUnit.MILLISECONDS.toSeconds(op
					.getCostTime());
			statistic.nrQueried += op.getNrQueried();
			statistic.longestCommonProfixLength += op
					.getLongestCommonPrefixLength();
		}

		statistic.costTime /= nrRequest;
		statistic.nrQueried /= nrRequest;
		statistic.longestCommonProfixLength /= nrRequest;
		return statistic;
	}

	public void shutdown(){
		eMuleKad.shutdown();
	}
	
	public static void main(String[] args) throws IOException {

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator config = new JoranConfigurator();
		lc.reset();
		config.setContext(lc);
		try {
			config.doConfigure(FindNodeMeasurement2.class.getClassLoader()
					.getResourceAsStream("findNode-logback.xml"));
		} catch (JoranException e) {
			e.printStackTrace();
		}
//		StatusPrinter.print(lc);
		int nrFind=100;
		FindNodeMeasurement2 measurement;
		for (int i = 1; i <= 5; i++) {
			measurement=new FindNodeMeasurement2(TimeUnit.SECONDS.toMillis(i));
			logger.info("\ntimeout={}", TimeUnit.SECONDS.toMillis(i));
			FindNodeStatistic fns1 = measurement.doFindNode(OpCodes.FIND_NODE, nrFind);
			logger.info("{}", fns1);
			
			FindNodeStatistic fns2 = measurement.doFindNode(OpCodes.STORE, nrFind);
			logger.info("{}", fns2);
			
			FindNodeStatistic fns3 = measurement.doFindNode(OpCodes.FIND_VALUE, nrFind);
			logger.info("{}", fns3);
			
			measurement.shutdown();
		}

	}

}
