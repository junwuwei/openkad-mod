package il.technion.ewolf.kbr.openkad;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sort;
import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyColorComparator;
import il.technion.ewolf.kbr.KeyComparator;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;
import il.technion.ewolf.kbr.openkad.msg.FindNodeResponse;
import il.technion.ewolf.kbr.openkad.msg.ForwardMessage;
import il.technion.ewolf.kbr.openkad.msg.ForwardRequest;
import il.technion.ewolf.kbr.openkad.msg.ForwardResponse;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.msg.PingResponse;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import il.technion.ewolf.kbr.openkad.net.filter.SrcExcluderMessageFilter;
import il.technion.ewolf.kbr.openkad.net.filter.TypeExcluderMessageFilter;
import cn.edu.jnu.cs.emulekad.msg.BootstrapRequest;
import cn.edu.jnu.cs.emulekad.msg.EMuleKadResponse;
import cn.edu.jnu.cs.emulekad.msg.EMuleKadRequest;
import cn.edu.jnu.cs.emulekad.msg.SearchRequest;
import cn.edu.jnu.cs.emulekad.msg.PublishResponse;
import cn.edu.jnu.cs.emulekad.msg.UnknownMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * This is a data structures that holds all the known nodes
 * It sorts them into buckets according to their keys common prefix
 * with the local node's key.
 * 
 * A node with a different MSB in its key than the local node's MSB
 * will be inserted to the last bucket.
 * A node with ONLY the LSB different will be inserted into the first bucket.
 * Generally, a node with a common prefix the length of k bits with the local
 * node will be inserted to the KeyLengthInBit - k bucket
 * 
 * @author eyal.kibbar@gmail.com
 *
 */
public class SimpleKBuckets implements KBuckets {

	private final Provider<MessageDispatcher<Object>> msgDispatcherProvider;
	private final Provider<KadNode> kadNodeProvider;
	private final Bucket[] kbuckets;
	private final Bucket[] colorsBucket;
	private final Node localNode;
	private final KeyFactory keyFactory;
	private final int nrColors;
	private final int nrAllColors;
	@Inject
	SimpleKBuckets(
			KeyFactory keyFactory,
			Provider<KadNode> kadNodeProvider,
			Provider<MessageDispatcher<Object>> msgDispatcherProvider,
			@Named("openkad.bucket.kbuckets") Provider<Bucket> kBucketProvider,
			@Named("openkad.bucket.slack") Provider<Bucket> colorsBucketProvider,
			@Named("openkad.local.node") Node localNode,
			@Named("openkad.color.nrcolors") int nrColors,
			@Named("openkad.color.allcolors") int nrAllColors) {
		
		this.keyFactory = keyFactory;
		this.msgDispatcherProvider = msgDispatcherProvider;
		this.kadNodeProvider = kadNodeProvider;
		this.localNode = localNode;
		this.nrColors = nrColors;
		this.nrAllColors = nrAllColors;
		
		kbuckets = new Bucket[keyFactory.getBitLength()];
		for (int i=0; i < kbuckets.length; ++i) {
			kbuckets[i] = kBucketProvider.get();
		}
		colorsBucket = new Bucket[nrAllColors];
		for (int i=0; i < colorsBucket.length; ++i) {
			colorsBucket[i] = colorsBucketProvider.get();
		}
	}


	@Override
	public List<Key> randomKeysForAllBuckets() {
		List<Key> $ = new ArrayList<Key>();
		for (int i=0; i < kbuckets.length; ++i) {
			Key key = keyFactory.generate(i).xor(localNode.getKey());
			$.add(key);
		}
		return $;
	}
	

	@Override
	public void registerIncomingMessageHandler() {
		msgDispatcherProvider.get()
			.setConsumable(false)
			// do not add PingResponse since it might create a loop
			.addFilter(new TypeExcluderMessageFilter(PingResponse.class))
			.addFilter(new TypeExcluderMessageFilter(BootstrapRequest.class))
			.addFilter(new TypeExcluderMessageFilter(EMuleKadRequest.class))
			.addFilter(new TypeExcluderMessageFilter(EMuleKadResponse.class))
			.addFilter(new TypeExcluderMessageFilter(SearchRequest.class))
			.addFilter(new TypeExcluderMessageFilter(PublishResponse.class))
			.addFilter(new TypeExcluderMessageFilter(UnknownMessage.class))
			.addFilter(new SrcExcluderMessageFilter(localNode))
			
			.setCallback(null, new CompletionHandler<KadMessage, Object>() {
				
				@Override
				public void failed(Throwable exc, Object attachment) {
					// should never be here
					exc.printStackTrace();
				}
				
				@Override
				public void completed(KadMessage msg, Object attachment) {
					SimpleKBuckets.this.insert(kadNodeProvider.get()
							.setNode(msg.getSrc())
							.setNodeWasContacted());
					
					// try to sniff the message for more information, such as
					// nodes in its content
					List<Node> nodes = null;
					if (msg instanceof FindNodeResponse) {
						nodes = ((FindNodeResponse)msg).getNodes();
					} else if (msg instanceof ForwardResponse) {
						nodes = ((ForwardResponse)msg).getNodes();
					} else if (msg instanceof ForwardMessage) {
						nodes = ((ForwardMessage)msg).getNodes();
					} else if (msg instanceof ForwardRequest) {
						nodes = ((ForwardRequest)msg).getBootstrap();
					}
					
					if (nodes != null) {
						for (Node n : nodes) {
							SimpleKBuckets.this.insert(kadNodeProvider.get().setNode(n));
						}
					}
				}
			})
			.register();
	}

	private int getKBucketIndex(Key key) {
		return key.xor(localNode.getKey()).getFirstSetBitIndex();
	}
	
	private List<Node> getClosestNodes(Key k, int n, int index, Bucket[] buckets) {
		Set<Node> emptySet = Collections.emptySet();
		return getClosestNodes(k, n, index, buckets, emptySet);
	}
	
	private List<Node> getClosestNodes(Key k, int n, int index, Bucket[] buckets, Collection<Node> exclude) {
	
		final List<Node> $ = new ArrayList<Node>();
		final Set<Node> t = new HashSet<Node>();
		if (index < 0)
			index = 0;
		
		buckets[index].addNodesTo($);
		
		if ($.size() < n) {
			// look in other buckets
			for (int i=1; $.size() < n; ++i) {
				if (index + i < buckets.length) {
					buckets[index + i].addNodesTo(t);
					t.removeAll(exclude);
					$.addAll(t);
					t.clear();
				}
			
				if (0 <= index - i) {
					buckets[index - i].addNodesTo(t);
					t.removeAll(exclude);
					$.addAll(t);
					t.clear();
				}
				
				if (buckets.length <= index + i && index - i < 0)
					break;
			}
		}
		
		return $;
	}
	
	@Override
	public void insert(KadNode node) {
		int i = getKBucketIndex(node.getNode().getKey());
		if (i == -1)
			return;
		
		kbuckets[i].insert(node);
		colorsBucket[node.getNode().getKey().getColor(nrAllColors)].insert(node);
	}
	

	@Override
	public List<Node> getAllNodes() {
		List<Node> $ = new ArrayList<Node>();
		for (int i=0; i < kbuckets.length; ++i) {
			kbuckets[i].addNodesTo($);
		}
		return $;
	}
	

	@Override
	public void markAsDead(Node n) {
		int i = getKBucketIndex(n.getKey());
		if (i == -1)
			return;
		
		kbuckets[i].markDead(n);
	}
	

	@Override
	public List<Node> getAllFromBucket(Key k) {
		int i = getKBucketIndex(k);
		if (i == -1)
			return Collections.emptyList();
		List<Node> $ = new ArrayList<Node>();
		kbuckets[i].addNodesTo($);
		return $;
	}
	

	@Override
	public List<Node> getClosestNodesByKey(Key k, int n) {
		List<Node> $ = getClosestNodes(k, n, getKBucketIndex(k), kbuckets);
		if ($.isEmpty())
			return $;
		$ = sort($, on(Node.class).getKey(), new KeyComparator(k));
		if ($.size() > n)
			$.subList(n, $.size()).clear();
		return $;
	}
	

	@Override
	public List<Node> getClosestNodesByColor(Key k, int n) {
		List<Node> $ = getClosestNodes(k, n, getKBucketIndex(k), kbuckets);
		if ($.isEmpty())
			return $;
		$ = sort($, on(Node.class).getKey(), new KeyColorComparator(k, nrColors));
		if ($.size() > n)
			$.subList(n, $.size()).clear();
		return $;
	}
	
	

	@Override
	public List<Node> getNodesFromColorBucket(Key k) {
		List<Node> $ = new ArrayList<Node>();
		colorsBucket[k.getColor(nrAllColors)].addNodesTo($);
		return $;
	}
	
	@Override
	public String toString() {
		String $ = "";
		for (int i=0; i < kbuckets.length; ++i)
			$ += kbuckets[i].toString()+"\n";
		return $;
	}
	
}
