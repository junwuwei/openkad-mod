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
public class FindNodeMeasurement {
	private static Injector injector;

	private static Logger logger = LoggerFactory
			.getLogger(FindNodeMeasurement.class);

	static {
		injector = Guice
				.createInjector(new EMuleKadModule()
						.setProperty("openkad.refresh.enable", false + "")
						.setProperty("openkad.bootstrap.do_rendom_findnode",
								false + "")
						.setProperty("openkad.net.udp.port", "10000")
						.setProperty("openkad.bootstrap.ping_befor_insert",
								false + ""));
	}

	public static void main(String[] args) throws IOException {

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator config = new JoranConfigurator();
		lc.reset();
		config.setContext(lc);
		try {
			config.doConfigure(FindNodeMeasurement.class.getClassLoader()
					.getResourceAsStream("findNode-logback.xml"));
		} catch (JoranException e) {
			e.printStackTrace();
		}
		
//		StatusPrinter.print(lc);

		EMuleKadNet eMuleKad = injector.getInstance(EMuleKadNet.class);
		eMuleKad.create();
		NodesDatFile nodesDatFile = injector.getInstance(NodesDatFile.class);
		eMuleKad.joinNode(nodesDatFile.readNodeFromFile());
		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
		int costTime = 0;
		int nrQueried = 0;
		int n = 500;

		for (int i = 0; i < n; i++) {
			EMuleFindNodeOperation op = injector
					.getInstance(EMuleFindNodeOperation.class);
			op.setRequestType(OpCodes.FIND_NODE).setKey(keyFactory.generate())
					.doFindNode();
			costTime += TimeUnit.MILLISECONDS.toSeconds(op.getCostTime());
			nrQueried += op.getNrQueried();
		}

		double costTime_FIND_NODE = 1.0 * costTime / n;
		double nrQueried_FIND_NODE = 1.0 * nrQueried / n;

		costTime = 0;
		nrQueried = 0;

		for (int i = 0; i < n; i++) {
			EMuleFindNodeOperation op = injector
					.getInstance(EMuleFindNodeOperation.class);
			op.setRequestType(OpCodes.STORE).setKey(keyFactory.generate())
					.doFindNode();
			costTime += TimeUnit.MILLISECONDS.toSeconds(op.getCostTime());
			nrQueried += op.getNrQueried();
		}

		double costTime_STORE = 1.0 * costTime / n;
		double nrQueried_STORE = 1.0 * nrQueried / n;

		costTime = 0;
		nrQueried = 0;

		for (int i = 0; i < n; i++) {
			EMuleFindNodeOperation op = injector
					.getInstance(EMuleFindNodeOperation.class);
			op.setRequestType(OpCodes.FIND_VALUE).setKey(keyFactory.generate())
					.doFindNode();
			costTime += TimeUnit.MILLISECONDS.toSeconds(op.getCostTime());
			nrQueried += op.getNrQueried();
		}

		double costTime_FIND_VALUE = 1.0 * costTime / n;
		double nrQueried_FIND_VALUE = 1.0 * nrQueried / n;

		logger.info(
				"FIND_NODE findeRequest average cost {} seconds, queried {} nodes.",
				costTime_FIND_NODE, nrQueried_FIND_NODE);
		logger.info(
				"FIND_NODE findeRequest average cost {} seconds, queried {} nodes.",
				costTime_STORE, nrQueried_STORE);
		logger.info(
				"FIND_NODE findeRequest average cost {} seconds, queried {} nodes.",

				costTime_FIND_VALUE, nrQueried_FIND_VALUE);
		eMuleKad.shutdown();

	}
}
