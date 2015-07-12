/**
 * 
 */
package cn.edu.jnu.cs.emulekad;

import il.technion.ewolf.kbr.KeyFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import cn.edu.jnu.cs.emulekad.msg.PublishAndSearchType;
import cn.edu.jnu.cs.emulekad.util.NodesDatFile;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class PublishKeywordTimespan {
	
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
								false + "")
						.setProperty("openkad.seed", "0"));

		EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
//		 FakeEMuleKad eMuleKad = new FakeEMuleKad(OperationResult.SUCCESS);
		eMuleKad.create();
		NodesDatFile nodesDatFile = injector.getInstance(NodesDatFile.class);
		eMuleKad.joinNode(nodesDatFile.readNodeFromFile());

//		PublishTimespan measurement = injector
//				.getInstance(PublishKeywordTimespan.class);
		 KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
		 PublishHelper publishHelper =
		 injector.getInstance(PublishHelper.class);
		 PublishTimespan measurement = new
		 PublishTimespan(eMuleKad,
		 keyFactory, PublishAndSearchType.KEYWORD,publishHelper,3,3,TimeUnit.MINUTES.toMillis(2));

		measurement.doPublish(50, 2);
//		TimeUnit.MINUTES.sleep(1);
//		eMuleKad.setOperationResult(OperationResult.FAIL);
		measurement.waitForAllRecordsVanished();

		eMuleKad.shutdown();
	}
}
