/**
 * 
 */
package cn.edu.jnu.cs.emulekad.op;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.jnu.cs.emulekad.msg.BootstrapRequest;
import cn.edu.jnu.cs.emulekad.msg.BootstrapResponse;
import cn.edu.jnu.cs.emulekad.net.filter.SrcMessageFilter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;
import il.technion.ewolf.kbr.openkad.KBuckets;
import il.technion.ewolf.kbr.openkad.KadNode;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import il.technion.ewolf.kbr.openkad.net.filter.TypeMessageFilter;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class BootstrapOperation implements CompletionHandler<KadMessage, Void> {

	private Collection<Node> bootstrap;
	private CountDownLatch latch;

	// dependencies
	private final KBuckets kBuckets;
	private final Provider<MessageDispatcher<Void>> msgDispatcherProvider;
	private final Provider<KadNode> kadNodeProvider;
	// testing
	AtomicInteger nrCompleted = new AtomicInteger(0);
	AtomicInteger nrReceivedNode = new AtomicInteger(0);
	
	private static Logger logger = LoggerFactory.getLogger(BootstrapOperation.class);
	
	
	@Inject
	public BootstrapOperation(KBuckets kBuckets,
			Provider<MessageDispatcher<Void>> msgDispatcherProvider,
			@Named("openkad.scheme.name")String udpScheme,
			Provider<KadNode> kadNodeProvider) {
		super();
		this.kBuckets = kBuckets;
		this.msgDispatcherProvider = msgDispatcherProvider;
		this.bootstrap = Collections.emptySet();
		this.kadNodeProvider=kadNodeProvider;
	}

	@Override
	public void completed(KadMessage msg, Void nothing) {
		BootstrapResponse res=(BootstrapResponse) msg;
		kBuckets.insert(kadNodeProvider.get().setNode(res.getSrc()));
//		logger.debug(res.getSrc().getKey());
		nrCompleted.addAndGet(1);
		for(Node node:res.getNodes()){
			kBuckets.insert(kadNodeProvider.get().setNode(node));
//			logger.debug(node);
		}
		nrReceivedNode.addAndGet(res.getNodes().size());
		latch.countDown();
	}

	public void doBootstrap() {
		latch = new CountDownLatch(bootstrap.size());
		for (Node n : bootstrap) {
			sendBootstrap(n);
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.debug("bootstrap completed, nrComplete="
				+ nrCompleted.get() + ", nrReceivedNode="
				+ nrReceivedNode.get());
	}

	void sendBootstrap(Node to) {
		BootstrapRequest bootstrapRequest = new BootstrapRequest();
		msgDispatcherProvider.get()
				.addFilter(new TypeMessageFilter(BootstrapResponse.class))
				.addFilter(new SrcMessageFilter(to))
				.setConsumable(true).setCallback(null, this)
				.send(to, bootstrapRequest);
	}

	@Override
	public void failed(Throwable exc, Void nothing) {
		if(exc instanceof TimeoutException){
			logger.debug(exc.getMessage());
		}else{
			logger.error("{}",exc);
		}
		latch.countDown();
	}

	public BootstrapOperation setBootstrap(Collection<Node> bootstrap) {
		if (bootstrap != null)
			this.bootstrap = bootstrap;
		return this;
	}

}
