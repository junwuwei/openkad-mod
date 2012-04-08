/**
 * 
 */
package cn.edu.jnu.cs.emulekad;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.Node;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.msg.PublishAndSearchType;
import cn.edu.jnu.cs.emulekad.op.EMuleFindValueOperation;
import cn.edu.jnu.cs.emulekad.op.PublishOperation;
import cn.edu.jnu.cs.emulekad.op.SearchOperation;
import cn.edu.jnu.cs.emulekad.util.NodesDatFile;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * @author Zhike Chan (zk.chan007@gmail.com)
 * $create on: 2012-4-7
 */
public class PublishAndSearchCostTime {
	private final Injector injector;
	private final KeyFactory keyFactory;
	private final EMuleKad eMuleKad;
	private final PublishHelper publishHelper;

	private static Logger logger = LoggerFactory
			.getLogger(PublishAndSearchCostTime.class);

	@Inject
	public PublishAndSearchCostTime(long timeout) throws IOException {
		injector = Guice
				.createInjector(new EMuleKadModule()
						.setProperty("openkad.refresh.enable", false + "")
						.setProperty("openkad.bootstrap.do_rendom_findnode",
								false + "")
						.setProperty("openkad.net.udp.port", "10005")
						.setProperty("openkad.net.timeout", timeout+"")
						.setProperty("openkad.bootstrap.ping_befor_insert",
								false + ""));
		keyFactory = injector.getInstance(KeyFactory.class);
		eMuleKad = injector.getInstance(EMuleKad.class);
		eMuleKad.create();
		NodesDatFile nodesDatFile = injector.getInstance(NodesDatFile.class);
		eMuleKad.joinNode(nodesDatFile.readNodeFromFile());
		publishHelper=injector.getInstance(PublishHelper.class);
	}

	static class Statistic {
		double publishCostTime = 0;
		double searchCostTime = 0;
		double nrCompleted=0;
		PublishAndSearchType type = null;

		public String toString() {
			return String.format(
					"Type=%s, average publish costTime=%.3f seconds, nrCompleted=%.3f, search costTime=%.3f seconds.",type,publishCostTime,nrCompleted,searchCostTime);
		}
	}
	
	public Statistic doPublishAndSearch(PublishAndSearchType type,int nrPublish) {
		String vanishString = "publish and search costtime measurement";
		Entry entry=null;
		switch(type){
		case NOTE:
			entry = publishHelper.makeNoteEntry(vanishString);
			break;
		case SOURCE:
			entry = publishHelper.makeSourceEntry(vanishString);
			break;
		case KEYWORD:
			entry = publishHelper.makeKeywordEntry(vanishString);
			break;
		}
		
		double nrCompletedSum=0;
		long startTime=0;
		long endTime=0;
		double publishTimeSum=0;
		double searchTimeSum=0;
		
		for (int i = 0; i < nrPublish; i++) {
			Key targetKey = keyFactory.generate();
			EMuleFindValueOperation findNodeOp=injector.getInstance(EMuleFindValueOperation.class);
			List<Node> nodes=findNodeOp.setKey(targetKey).doFindValue();
			PublishOperation publishOp=injector.getInstance(PublishOperation.class);
			
			startTime=System.currentTimeMillis();
			nrCompletedSum+=publishOp.setRecipients(nodes).setTargetKey(targetKey).setPublishType(type).addEntry(entry).doPublish();
			endTime=System.currentTimeMillis();
			publishTimeSum+=1.0*(endTime-startTime)/TimeUnit.SECONDS.toMillis(1);
			
			SearchOperation SearchOp=injector.getInstance(SearchOperation.class);
			startTime=System.currentTimeMillis();
			SearchOp.setRecipients(nodes).setTargetKey(targetKey).setSearchType(type).doSearch();
			endTime=System.currentTimeMillis();
			searchTimeSum+=1.0*(endTime-startTime)/TimeUnit.SECONDS.toMillis(1);
		}
		Statistic s=new Statistic();
		s.publishCostTime=publishTimeSum/nrPublish;
		s.searchCostTime=searchTimeSum/nrPublish;
		s.nrCompleted=nrCompletedSum/nrPublish;
		s.type=type;
		return s;
	}

	public void shutdown(){
		eMuleKad.shutdown();
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator config = new JoranConfigurator();
		lc.reset();
		config.setContext(lc);
		try {
			config.doConfigure(PublishNoteTimespan.class.getClassLoader()
					.getResourceAsStream("publishAndSearch-logback.xml"));
		} catch (JoranException e) {
			e.printStackTrace();
		}

		// StatusPrinter.print(lc);
		
		for (int i = 1; i <=5; i++) {
			PublishAndSearchCostTime measurement=new PublishAndSearchCostTime(TimeUnit.SECONDS.toMillis(i));
			logger.info("timeout={}",TimeUnit.SECONDS.toMillis(i));
			Statistic s=measurement.doPublishAndSearch(PublishAndSearchType.NOTE, 100);
			logger.info("{}",s);
			s=measurement.doPublishAndSearch(PublishAndSearchType.SOURCE, 100);
			logger.info("{}",s);
			s=measurement.doPublishAndSearch(PublishAndSearchType.KEYWORD, 100);
			logger.info("{}",s);
			measurement.shutdown();
		}
		
	}
}
