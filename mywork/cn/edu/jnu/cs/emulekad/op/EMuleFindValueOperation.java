package cn.edu.jnu.cs.emulekad.op;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sort;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.jnu.cs.emulekad.msg.EMuleKadRequest;
import cn.edu.jnu.cs.emulekad.msg.EMuleKadResponse;
import cn.edu.jnu.cs.emulekad.net.OpCodes;
import cn.edu.jnu.cs.emulekad.net.filter.SrcMessageFilter;
import cn.edu.jnu.cs.emulekad.net.filter.TargetKeyMessageFilter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyComparator;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;
import il.technion.ewolf.kbr.openkad.KBuckets;
import il.technion.ewolf.kbr.openkad.KadNode;
import il.technion.ewolf.kbr.openkad.cache.KadCache;
import il.technion.ewolf.kbr.openkad.msg.FindNodeResponse;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import il.technion.ewolf.kbr.openkad.net.filter.TypeMessageFilter;
import il.technion.ewolf.kbr.openkad.op.FindValueOperation;

/**
 * EMuleFindValueOperation与EMuleFindNode的不同之处在于可以提供buotstrap nodes以及使用cache
 * @author Zhike Chan (zk.chan007@gmail.com)
 * $create on: 2012-4-23
 */
public class EMuleFindValueOperation extends FindValueOperation implements
		CompletionHandler<KadMessage, Node> {

	// state
	private List<Node> knownClosestNodes;
	private Key key;
	private final Set<Node> alreadyQueried;
	private final Set<Node> querying;
	private int nrQueried;
	private AtomicInteger nrComplete = new AtomicInteger(0);
	private byte requestType = OpCodes.FIND_NODE;
	private Collection<Node> bootstrap = Collections.emptyList();
	private boolean gotCachedResult = false;
	private long costTime;
	private int nrRetry = 0;

	// dependencies
	private final Provider<MessageDispatcher<Node>> msgDispatcherProvider;
	private final int kBucketSize;
	private final KBuckets kBuckets;
	private final Node localNode;
	private final KadCache cache;
	private final int keySize;
	private final int findNodeTolerance;
	private final int maxTryTimes;
	private final boolean useCache;
	private final Provider<KadNode> kadNodeProvider;

	private static Logger logger = LoggerFactory
			.getLogger(EMuleFindValueOperation.class);

	@Inject
	protected EMuleFindValueOperation(
			@Named("openkad.local.node") Node localNode,
			@Named("openkad.bucket.kbuckets.maxsize") int kBucketSize,
			Provider<EMuleKadRequest> findNodeRequestProvider,
			Provider<MessageDispatcher<Node>> msgDispatcherProvider,
			KBuckets kBuckets,
			Provider<KadNode> kadNodeProvider,
			KadCache cache,
			@Named("openkad.keyfactory.keysize") int keySize,
			@Named("openkad.findnode.try_times") int maxTryTimes,
			@Named("openkad.findnode.prefix_length.tolerance") int findNodeTolerance,
			@Named("openkad.findnode.usecache")boolean useCache) {
		this.localNode = localNode;
		this.kBucketSize = kBucketSize;
		this.kBuckets = kBuckets;
		this.msgDispatcherProvider = msgDispatcherProvider;
		this.kadNodeProvider=kadNodeProvider;
		this.cache = cache;
		this.keySize = keySize;
		this.maxTryTimes = maxTryTimes;
		this.findNodeTolerance = findNodeTolerance;
		this.useCache=useCache;

		alreadyQueried = new HashSet<Node>();
		querying = new HashSet<Node>();
	}

	public EMuleFindValueOperation setKey(Key key) {
		this.key = key;
		return this;
	}

	public int getNrQueried() {
		return nrQueried;
	}

	public EMuleFindValueOperation setBootstrap(Collection<Node> bootstrap) {
		this.bootstrap = bootstrap;
		return this;
	}

	private synchronized Node takeUnqueried() {
		for (int i = 0; i < knownClosestNodes.size(); ++i) {
			Node n = knownClosestNodes.get(i);
			if (!querying.contains(n) && !alreadyQueried.contains(n)) {
				querying.add(n);
				return n;
			}
		}
		return null;
	}

	private boolean hasMoreToQuery() {
		if (querying.isEmpty() && alreadyQueried.containsAll(knownClosestNodes)) {
			if (getLongestCommonPrefixLength() >= findNodeTolerance) {
				return false;
			} else if (nrRetry < maxTryTimes) {
				nrRetry++;
//				if (nrCompleted.get() == 0) {
					knownClosestNodes = kBuckets.getClosestNodesByKey(key,
							(nrRetry + 1) * kBucketSize);
					knownClosestNodes.removeAll(alreadyQueried);
//				} else {
//					alreadyQueried.removeAll(knownClosestNodes);
//				}
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	private void sendFindNode(Node to) {
		EMuleKadRequest findNodeRequest = new EMuleKadRequest(
				System.currentTimeMillis(), localNode)
				.setRequestType(requestType).setSearchCache(true).setKey(key)
				.setRecipient(to);

		msgDispatcherProvider.get()
				.addFilter(new TypeMessageFilter(EMuleKadResponse.class))
				.addFilter(new TargetKeyMessageFilter(key))
				.addFilter(new SrcMessageFilter(to)).setConsumable(true)
				.setCallback(to, this).send(to, findNodeRequest);
	}

	@Override
	public List<Node> doFindValue() {
		logger.info("find value,key={}", key);
		long startTime = System.currentTimeMillis();

		if (useCache) {
			List<Node> cacheResults = cache.search(key);
			if (cacheResults != null) {
				logger.info("cache hited.");
				return cacheResults;
			}
		}

		knownClosestNodes = kBuckets.getClosestNodesByKey(key, kBucketSize);
		knownClosestNodes.add(localNode);
		bootstrap.removeAll(knownClosestNodes);
		knownClosestNodes.addAll(bootstrap);
		alreadyQueried.add(localNode);
		KeyComparator keyComparator = new KeyComparator(key);

		do {
			synchronized (this) {
				knownClosestNodes = sort(knownClosestNodes, on(Node.class)
						.getKey(), keyComparator);
				if (knownClosestNodes.size() >= kBucketSize)
					knownClosestNodes.subList(kBucketSize,
							knownClosestNodes.size()).clear();

				if (gotCachedResult)
					break;

				if (!hasMoreToQuery())
					break;
			}

			Node n = takeUnqueried();

			if (n != null) {
				sendFindNode(n);
			} else {
				synchronized (this) {
					if (!querying.isEmpty()) {
						try {
							wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}

		} while (true);

		knownClosestNodes = Collections.unmodifiableList(knownClosestNodes);

		cache.insert(key, knownClosestNodes);

		synchronized (this) {
			nrQueried = alreadyQueried.size() - 1 + querying.size();
		}

		long endTime = System.currentTimeMillis();
		costTime = endTime - startTime;
		logger.info("finished find node,key={}, used {} seconds.", key,
				TimeUnit.MILLISECONDS.toSeconds(costTime));
		logger.info("nrQueried={},knownClosestNodes.size()={}", nrQueried,
				knownClosestNodes.size());
		logger.info("nrComplete={},LongestCommonPrefixLength={}", nrComplete,
				getLongestCommonPrefixLength());
		logger.info("nrRetry={}", nrRetry);

		return knownClosestNodes;
	}

	@Override
	public synchronized void completed(KadMessage msg, Node n) {
//		本应在KBuckets类的registerIncomingMessageHandler（）方法注册的
//		messageDispatcher中插入结点信息，但是由于EMuleKadResponse中没有ID信息，在此处插入实属无奈
		kBuckets.insert(kadNodeProvider.get()
				.setNode(n).setNodeWasContacted());
		nrComplete.incrementAndGet();
		notifyAll();
		querying.remove(n);
		alreadyQueried.add(n);

		if (gotCachedResult)
			return;

		List<Node> nodes = ((FindNodeResponse) msg).getNodes();
		nodes.removeAll(querying);
		nodes.removeAll(alreadyQueried);
		nodes.removeAll(knownClosestNodes);

		knownClosestNodes.addAll(nodes);

		if (((FindNodeResponse) msg).isCachedResults())
			gotCachedResult = true;

	}

	@Override
	public synchronized void failed(Throwable exc, Node n) {
		notifyAll();
		querying.remove(n);
		alreadyQueried.add(n);
		if (exc instanceof TimeoutException) {
			logger.debug(exc.getMessage());
		} else {
			logger.error("{}", exc);
		}
	}

	public EMuleFindValueOperation setRequestType(byte requestType) {
		this.requestType = requestType;
		return this;
	}

	public long getCostTime() {
		return costTime;
	}

	public int getLongestCommonPrefixLength() {
		return keySize
				* 8
				- key.xor(knownClosestNodes.get(0).getKey())
						.getFirstSetBitIndex() - 1;
	}
}
