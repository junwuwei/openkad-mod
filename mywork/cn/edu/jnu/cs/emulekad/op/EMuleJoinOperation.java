package cn.edu.jnu.cs.emulekad.op;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;
import il.technion.ewolf.kbr.openkad.KBuckets;
import il.technion.ewolf.kbr.openkad.KadNode;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.msg.PingRequest;
import il.technion.ewolf.kbr.openkad.msg.PingResponse;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import il.technion.ewolf.kbr.openkad.net.filter.TypeMessageFilter;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.jnu.cs.emulekad.net.OpCodes;
import cn.edu.jnu.cs.emulekad.net.filter.SrcMessageFilter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class EMuleJoinOperation {

	// dependencies
	private final Provider<BootstrapOperation> bootstrapOperationProvider;
	private final Provider<EMuleFindNodeOperation> findNodeOperationProvider;
	private final Provider<PingRequest> pingRequestProvider;
	private final Provider<MessageDispatcher<Void>> msgDispatcherProvider;
	private final Key zeroKey;
	private final String udpScheme;
	private final int needToBoostrapSize;
	private final boolean pingBeforeInsertNode;
	private final boolean doRandomFindNode;
	private final KBuckets kBuckets;
	private final Node localNode;
	private final Provider<KadNode> kadNodeProvider;
	private final Timer timer;
	private final boolean enableRefreshTash;
	private final long refreshFirstDelay;
	private final long refreshInterval;
	private final TimerTask refreshTask;

	// state
	private Collection<Node> bootstrap = new HashSet<Node>();

	// testing
	AtomicInteger nrCompleted = new AtomicInteger(0);

	private static Logger logger = LoggerFactory
			.getLogger(EMuleJoinOperation.class);

	@Inject
	EMuleJoinOperation(
			Provider<BootstrapOperation> bootstrapOperationProvider,
			Provider<EMuleFindNodeOperation> findNodeOperationProvider,
			Provider<PingRequest> pingRequestProvider,
			Provider<MessageDispatcher<Void>> msgDispatcherProvider,
			Provider<KadNode> kadNodeProvider,
			KBuckets kBuckets,
			@Named("openkad.keys.zerokey") Key zeroKey,
			Provider<KeyFactory> keyFactoryProvider,
			@Named("openkad.scheme.name") String udpScheme,
			@Named("openkad.scheme2.name") String tcpScheme,
			@Named("openkad.need_bootstrap.minsize") int needToBoostrapSize,
			@Named("openkad.bootstrap.ping_befor_insert") boolean pingBeforeInsertNode,
			@Named("openkad.bootstrap.do_rendom_findnode") boolean doRandomFindNode,
			@Named("openkad.local.node") Node localNode,
			@Named("openkad.timer") Timer timer,
			@Named("openkad.refresh.enable") boolean enableRefreshTash,
			@Named("openkad.refresh.first_delay") long refreshFirstDelay,
			@Named("openkad.refresh.interval") long refreshInterval,
			@Named("openkad.refresh.task") TimerTask refreshTask) {

		this.bootstrapOperationProvider = bootstrapOperationProvider;
		this.kadNodeProvider = kadNodeProvider;
		this.findNodeOperationProvider = findNodeOperationProvider;
		this.pingRequestProvider = pingRequestProvider;
		this.msgDispatcherProvider = msgDispatcherProvider;
		this.kBuckets = kBuckets;
		this.zeroKey = zeroKey;
		this.udpScheme = udpScheme;
		this.needToBoostrapSize = needToBoostrapSize;
		this.pingBeforeInsertNode = pingBeforeInsertNode;
		this.doRandomFindNode = doRandomFindNode;
		this.localNode = localNode;
		this.timer = timer;
		this.enableRefreshTash = enableRefreshTash;
		this.refreshFirstDelay = refreshFirstDelay;
		this.refreshInterval = refreshInterval;
		this.refreshTask = refreshTask;
	}

	/**
	 * Sets the bootstrap nodes for stating the join operation
	 * 
	 * @param bootstrapUri
	 *            all the bootstraps URI in the following format:
	 *            openkad.udp://[node ip]:[port]/
	 * @return this for fluent interface
	 */
	public EMuleJoinOperation addBootstrapURI(Collection<URI> bootstrapUri) {

		for (URI uri : bootstrapUri) {
			Node n = new Node(zeroKey);
			try {
				n.setInetAddress(InetAddress.getByName(uri.getHost()));
			} catch (UnknownHostException e) {
				e.printStackTrace();
				continue;
			}
			n.addEndpoint(udpScheme, uri.getPort());
			bootstrap.add(n);
			// logger.debug(n);
		}
		// logger.debug(bootstrap.size());
		return this;
	}

	public EMuleJoinOperation addBootstrapNode(Collection<Node> bootstrapNodes) {
		bootstrap.addAll(bootstrapNodes);
		return this;
	}

	/**
	 * Join a network !
	 */
	public void doJoin() {
		if (bootstrap.size() <= needToBoostrapSize) {
			bootstrapOperationProvider.get().setBootstrap(bootstrap)
					.doBootstrap();
		} else if (pingBeforeInsertNode) {

			final CountDownLatch latch = new CountDownLatch(bootstrap.size());
			CompletionHandler<KadMessage, Void> callback = new CompletionHandler<KadMessage, Void>() {

				@Override
				public void completed(KadMessage msg, Void nothing) {
					try {
						kBuckets.insert(kadNodeProvider.get()
								.setNode(msg.getSrc()).setNodeWasContacted());
					} finally {
						latch.countDown();
						nrCompleted.incrementAndGet();
					}
				}

				@Override
				public void failed(Throwable exc, Void nothing) {
					logger.debug("{}", exc);
					latch.countDown();
				}
			};

			for (Node n : bootstrap) {
				PingRequest pingRequest = pingRequestProvider.get();
				msgDispatcherProvider
						.get()
						// .addFilter(new IdMessageFilter(pingRequest.getId()))
						.addFilter(new SrcMessageFilter(n))
						.addFilter(new TypeMessageFilter(PingResponse.class))
						.setConsumable(true).setCallback(null, callback)
						.send(n, pingRequest);
			}

			// waiting for responses

			try {
				latch.await();
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1);
			}

			logger.debug("nrCompleted={}", nrCompleted);
			if (kBuckets.getClosestNodesByKey(zeroKey, 1).isEmpty())
				throw new IllegalStateException("all bootstrap nodes are down");

		} else {
			for (Node n : bootstrap) {
				kBuckets.insert(kadNodeProvider.get().setNode(n));
				nrCompleted.incrementAndGet();
			}
			logger.debug("nrCompleted={}", nrCompleted);
		}

		if (doRandomFindNode) {
			findNodeOperationProvider.get().setKey(localNode.getKey())
					.setRequestType(OpCodes.FIND_NODE).doFindNode();

			for (Key key : kBuckets.randomKeysForAllBuckets()) {
				findNodeOperationProvider.get().setKey(key).doFindNode();
			}
		}

		if (enableRefreshTash) {
			try {
				// timer.scheduleAtFixedRate(refreshTask,
				// refreshInterval,refreshInterval);
				timer.schedule(refreshTask, refreshFirstDelay, refreshInterval);
			} catch (IllegalStateException e) {
				// if I couldn't schedule the refresh task i don't care
			}
		}
	}

}
