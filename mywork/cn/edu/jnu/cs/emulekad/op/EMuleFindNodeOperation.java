/**
 * 
 */
package cn.edu.jnu.cs.emulekad.op;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sort;
import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyComparator;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;
import il.technion.ewolf.kbr.openkad.KBuckets;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import il.technion.ewolf.kbr.openkad.net.filter.TypeMessageFilter;
import il.technion.ewolf.kbr.openkad.op.FindNodeOperation;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class EMuleFindNodeOperation implements
		CompletionHandler<KadMessage, Node>, FindNodeOperation {
	// state
	private List<Node> knownClosestNodes;
	private Key key;
	private final Set<Node> alreadyQueried;
	private final Set<Node> querying;
	private byte requestType = OpCodes.FIND_NODE;

	// dependencies
	private final Provider<EMuleKadRequest> EMuleKadRequestProvider;
	private final Provider<MessageDispatcher<Node>> msgDispatcherProvider;
	private final int kBucketSize;
	private final KBuckets kBuckets;
	private final Node localNode;
	
	//measurement
	private int nrQueried;
	private long costTime;

	private static Logger logger = LoggerFactory
			.getLogger(EMuleFindNodeOperation.class);

	@Inject
	public EMuleFindNodeOperation(@Named("openkad.local.node") Node localNode,
			@Named("openkad.bucket.kbuckets.maxsize") int kBucketSize,
			Provider<EMuleKadRequest> EMuleKadRequestProvider,
			Provider<MessageDispatcher<Node>> msgDispatcherProvider,
			KBuckets kBuckets) {

		this.localNode = localNode;
		this.kBucketSize = kBucketSize;
		this.kBuckets = kBuckets;
		this.EMuleKadRequestProvider = EMuleKadRequestProvider;
		this.msgDispatcherProvider = msgDispatcherProvider;

		alreadyQueried = new HashSet<Node>();
		querying = new HashSet<Node>();
	}

	/**
	 * Sets the key to be found. Do not change this value after invoking
	 * doFindNode.
	 * 
	 * @param key
	 *            the key to be found
	 * @return this for fluent interface
	 */
	public EMuleFindNodeOperation setKey(Key key) {
		this.key = key;
		return this;
	}

	public EMuleFindNodeOperation setRequestType(byte requestType) {
		this.requestType = requestType;
		return this;
	}

	public int getNrQueried() {
		return nrQueried;
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
		return !querying.isEmpty()
				|| !alreadyQueried.containsAll(knownClosestNodes);
	}

	private void sendFindNode(Node to) {
		EMuleKadRequest eMuleKadRequest = EMuleKadRequestProvider.get()
				.setSearchCache(false).setKey(key).setRequestType(requestType)
				.setRecipient(to);

		msgDispatcherProvider
				.get()
				// .addFilter(new IdMessageFilter(eMuleKadRequest.getId()))
				.addFilter(new TargetKeyMessageFilter(key))
				.addFilter(new TypeMessageFilter(EMuleKadResponse.class))
				.addFilter(new SrcMessageFilter(to)).setConsumable(true)
				.setCallback(to, this).send(to, eMuleKadRequest);
	}

	/**
	 * Do the find node recursive operation
	 * 
	 * @return a list of nodes closest to the set key
	 */
	public List<Node> doFindNode() {
		logger.info("find node,request type={},key={}", requestType,key);
		long startTime = System.currentTimeMillis();

		knownClosestNodes = kBuckets.getClosestNodesByKey(key, kBucketSize);
		knownClosestNodes.add(localNode);
		alreadyQueried.add(localNode);
		KeyComparator keyComparator = new KeyComparator(key);

		do {
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

			synchronized (this) {
				knownClosestNodes = sort(knownClosestNodes, on(Node.class)
						.getKey(), keyComparator);
				if (knownClosestNodes.size() >= kBucketSize)
					knownClosestNodes.subList(kBucketSize,
							knownClosestNodes.size()).clear();

				logger.debug("\nknownClosestNodes.size()={}",
						knownClosestNodes.size());
				logger.debug("alreadyQueried.size()={}", alreadyQueried.size());
				logger.debug("querying.size()={}", querying.size());

				if (!hasMoreToQuery())
					break;
			}

		} while (true);

		knownClosestNodes = Collections.unmodifiableList(knownClosestNodes);

		synchronized (this) {
			nrQueried = alreadyQueried.size() - 1 + querying.size();
		}

		long endTime = System.currentTimeMillis();
		costTime=endTime - startTime;
		logger.info("finished find node,key={}, used {} seconds.", key,
				TimeUnit.MILLISECONDS.toSeconds(endTime - startTime));
		logger.info("nrQueried={},knownClosestNodes.size()={}", nrQueried,
				knownClosestNodes.size());

		return knownClosestNodes;
	}

	@Override
	public synchronized void completed(KadMessage msg, Node n) {
		notifyAll();
		querying.remove(n);
		alreadyQueried.add(n);

		List<Node> nodes = ((EMuleKadResponse) msg).getNodes();
		logger.debug("received EMuleKadResponse,key={}, nodes.size()={}",
				((EMuleKadResponse) msg).getKey(), nodes.size());

		nodes.removeAll(querying);
		nodes.removeAll(alreadyQueried);
		nodes.removeAll(knownClosestNodes);

		knownClosestNodes.addAll(nodes);

		logger.debug(
				"added {} nodes to knownClosestNodes,knownClosestNodes.size()={}",
				nodes.size(), knownClosestNodes.size());
		logger.debug("alreadyQueried.size()={},querying.size()={}",
				alreadyQueried.size(), querying.size());
	}

	@Override
	public synchronized void failed(Throwable exc, Node n) {
		notifyAll();
		querying.remove(n);
		alreadyQueried.add(n);
		logger.debug("{}", exc);
	}

	public long getCostTime() {
		return costTime;
	}
}
