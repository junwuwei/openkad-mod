package cn.edu.jnu.cs.emulekad;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.KeybasedRouting;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.RandomKeyFactory;
import il.technion.ewolf.kbr.openkad.Bucket;
import il.technion.ewolf.kbr.openkad.KBuckets;
import il.technion.ewolf.kbr.openkad.KadNode;
import il.technion.ewolf.kbr.openkad.StableBucket;
import il.technion.ewolf.kbr.openkad.DummyBucket;
import il.technion.ewolf.kbr.openkad.cache.KadCache;
import il.technion.ewolf.kbr.openkad.cache.LRUKadCache;
import il.technion.ewolf.kbr.openkad.handlers.PingHandler;
import il.technion.ewolf.kbr.openkad.msg.PingRequest;
import il.technion.ewolf.kbr.openkad.net.KadSerializer;
import il.technion.ewolf.kbr.openkad.net.KadServer;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import cn.edu.jnu.cs.emulekad.handlers.BootstrapHandler;
import cn.edu.jnu.cs.emulekad.handlers.EMuleKadRequestHandler;
import cn.edu.jnu.cs.emulekad.handlers.PublishHandler;
import cn.edu.jnu.cs.emulekad.handlers.SearchHandler;
import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.indexer.Indexer;
import cn.edu.jnu.cs.emulekad.net.EMuleKadSerializer;
import cn.edu.jnu.cs.emulekad.net.OpCodes;
import cn.edu.jnu.cs.emulekad.op.EMuleFindNodeOperation;
import cn.edu.jnu.cs.emulekad.op.EMuleFindValueOperation;
import cn.edu.jnu.cs.emulekad.op.EMuleJoinOperation;
import cn.edu.jnu.cs.emulekad.op.PublishOperation;
import cn.edu.jnu.cs.emulekad.op.SearchOperation;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class EMuleKadModule extends AbstractModule {

	private final Properties properties;

	private Properties getDefaultProperties() {
		Properties defaultProps = new Properties();

		// testing params, DONT TOUCH !!!
		defaultProps.setProperty("openkad.keyfactory.keysize", "16");
		defaultProps.setProperty("openkad.keyfactory.hashalgo", "MD4");
		defaultProps.setProperty("openkad.bucket.kbuckets.maxsize", "10");
		defaultProps.setProperty("openkad.bucket.kbuckets.optimal.maxsize", "20");
		defaultProps.setProperty("openkad.kbucket.nrbucket", "30");
		defaultProps.setProperty("openkad.color.nrcolors", "14");
		defaultProps.setProperty("openkad.scheme.name", "emulekad.udp");
		defaultProps.setProperty("openkad.scheme2.name", "emulekad.tcp");

		// bootstrap
		defaultProps.setProperty("openkad.bootstrap.response.max_nodes", "20");
		defaultProps.setProperty("openkad.need_bootstrap.minsize", "20");
		defaultProps.setProperty("openkad.bootstrap.ping_befor_insert",
				false + "");
		defaultProps.setProperty("openkad.bootstrap.do_rendom_findnode",
				false + "");
		defaultProps.setProperty("openkad.nodes.file.path", "nodes.dat");

		// find node
		defaultProps.setProperty("openkad.findnode.response.max_nodes", "11");
		defaultProps.setProperty("openkad.findvalue.response.max_nodes", "2");
		defaultProps.setProperty("openkad.store.response.max_nodes", "4");

		// index
		defaultProps.setProperty("openkad.index.max_source_per_file", "1000");
		defaultProps.setProperty("openkad.index.max_note_per_file", "150");
		defaultProps.setProperty("openkad.index.max_keyword_indexes", "50000");
		defaultProps.setProperty("openkad.index.max_keyword_entries", "60000");
		defaultProps.setProperty("openkad.index.source.publish.timespan",
				TimeUnit.HOURS.toMillis(5) + "");
		defaultProps.setProperty("openkad.index.note.publish.timespan",
				TimeUnit.HOURS.toMillis(24) + "");
		defaultProps.setProperty("openkad.index.keyword.publish.timespan",
				TimeUnit.HOURS.toMillis(24) + "");
		defaultProps.setProperty("openkad.index.cleantask.first_delay",
				TimeUnit.MINUTES.toMillis(30) + "");
		defaultProps.setProperty("openkad.index.cleantask.interval",
				TimeUnit.HOURS.toMillis(1) + "");

		// search
		defaultProps.setProperty("openkad.search.recipient_minsize", "1");
		defaultProps.setProperty("openkad.search.accelerate4vanish", false+"");

		// publish
		defaultProps.setProperty("openkad.publish.recipient_minsize", "1");

		// interval between successive find node operations for refresh buckets
		defaultProps.setProperty("openkad.refresh.enable", true + "");
		// defaultProps.setProperty("openkad.refresh.enable", true+"");
		// defaultProps.setProperty("openkad.refresh.interval",
		// TimeUnit.SECONDS.toMillis(30)+"");
		defaultProps.setProperty("openkad.refresh.interval",
				TimeUnit.MINUTES.toMillis(1) + "");
		defaultProps.setProperty("openkad.refresh.first_delay",
				TimeUnit.SECONDS.toMillis(1) + "");

		// performance params

		// handling incoming messages
		defaultProps.setProperty("openkad.executors.server.nrthreads", "40");
		defaultProps.setProperty("openkad.executors.server.max_pending", "128");
		// handling registered callback
		defaultProps.setProperty("openkad.executors.client.nrthreads", "2");
		defaultProps.setProperty("openkad.executors.client.max_pending", "128");
		
		// forwarding find node requests
		//eMuleKad不需用到
		defaultProps.setProperty("openkad.executors.forward.nrthreads", "2");
		defaultProps.setProperty("openkad.executors.forward.max_pending", "128");
		// executing the long find node operations
		//eMuleKad不需用到
		defaultProps.setProperty("openkad.executors.op.nrthreads", "1");
		defaultProps.setProperty("openkad.executors.op.max_pending", "128");
		
		// sending back pings
		defaultProps.setProperty("openkad.executors.ping.nrthreads", "1");
		defaultProps.setProperty("openkad.executors.ping.max_pending", "16");
		// cache settings
		defaultProps.setProperty("openkad.cache.validtime",
				TimeUnit.HOURS.toMillis(10) + "");
		defaultProps.setProperty("openkad.cache.size", "100");
		defaultProps.setProperty("openkad.cache.share", "1");
		// minimum time between successive pings
		defaultProps.setProperty("openkad.bucket.valid_timespan",
				TimeUnit.MINUTES.toMillis(10) + "");
		
		// network timeouts and concurrency level
		defaultProps.setProperty("openkad.net.concurrency", "50");
		defaultProps.setProperty("openkad.net.timeout",
				TimeUnit.SECONDS.toMillis(1) + "");
		defaultProps.setProperty("openkad.net.forwarded.timeout",
				TimeUnit.SECONDS.toMillis(300) + "");

		//eMuleKad不需用到
		defaultProps.setProperty("openkad.color.candidates", "1");
		defaultProps.setProperty("openkad.color.slack.size", "1");
		defaultProps.setProperty("openkad.color.allcolors", "95");

		// timer thread
		 defaultProps.setProperty("openkad.timerpool.size", "10");

		// local configuration, please touch
		defaultProps.setProperty("openkad.net.udp.port", "10086");
		defaultProps.setProperty("openkad.net.tcp.port", "10086");
		defaultProps.setProperty("openkad.local.key", "");

		// misc
		defaultProps.setProperty("openkad.seed", "17");

		return defaultProps;
	}

	public EMuleKadModule() {
		this(new Properties());
	}

	public EMuleKadModule(Properties properties) {
		this.properties = getDefaultProperties();
		this.properties.putAll(properties);
	}

	public EMuleKadModule setProperty(String name, String value) {
		this.properties.setProperty(name, value);
		return this;
	}

	@Override
	protected void configure() {
		Names.bindProperties(binder(), properties);

		bindTestingParams();

		// bind requests
//		bind(PingRequest.class);
//		bind(FindNodeRequest.class).to(EMuleKadRequest.class);
//		bind(EMuleKadRequest.class);
//		bind(ForwardRequest.class);
//		bind(ContentRequest.class);

		bind(KadNode.class);
		bind(KBuckets.class).to(OptimalKBuckets.class).in(Scopes.SINGLETON);
		bind(MessageDispatcher.class);
		bind(KadSerializer.class).to(EMuleKadSerializer.class).in(
				Scopes.SINGLETON);
		bind(KadServer.class).in(Scopes.SINGLETON);

		// bind index
		bind(Indexer.class).in(Scopes.SINGLETON);
		bind(Entry.class);

		bind(KadCache.class)
		// .to(DummyKadCache.class)
		// .to(OptimalKadCache.class)
				.to(LRUKadCache.class)
				// .to(ColorLRUKadCache.class)
				.in(Scopes.SINGLETON);


		bind(EMuleJoinOperation.class);
		bind(EMuleFindNodeOperation.class);
		bind(EMuleFindNodeOperation.class);
		bind(EMuleFindValueOperation.class);
		bind(SearchOperation.class);
		bind(PublishOperation.class);


		bind(PingHandler.class);
//		bind(StoreHandler.class);
//		bind(ForwardHandler.class);
		bind(BootstrapHandler.class);
		bind(EMuleKadRequestHandler.class);
		bind(SearchHandler.class);
		bind(PublishHandler.class);


		bind(KeybasedRouting.class).to(EMuleKad.class);
		bind(EMuleKad.class).to(EMuleKadNet.class).in(Scopes.SINGLETON);
		
		//measurement
		bind(PublishHelper.class);
		bind(PublishNoteTimespan.class);
	}

	@Provides
	@Singleton
	@Named("openkad.timers")
	List<Timer> systemTimers(@Named("openkad.timerpool.size") int poolSize){
		return new LinkedList<Timer>();
	}
	
	@Provides
	@Singleton
	@Named("openkad.timerpool")
	Pool<Timer> provideTimerpool(@Named("openkad.timerpool.size") int poolSize,@Named("openkad.timers")List<Timer> systemTimers){
		Pool<Timer> pool= new Pool<Timer>(poolSize);
		for (int i = 0; i < poolSize; i++) {
			Timer timer=new Timer("TimeoutTimer"+i,true);
			pool.add(timer);
			systemTimers.add(timer);
		}
		return pool;
	}
	
	@Provides
	@Named("openkad.timer")
	Timer provideMessageDespatcherTimer(
			@Named("openkad.timerpool") Pool<Timer> timerpool,
			@Named("openkad.timers")List<Timer> systemTimers){
		return timerpool.get();
//		Timer timer=new Timer("TimeoutTimer",true);
//		systemTimers.add(timer);
//		return timer;
	}
	
	@Provides
	@Singleton
	@Named("openkad.index.timer")
	Timer provideIndexTimer(@Named("openkad.timers")List<Timer> systemTimers){
		Timer timer=new Timer("IndexerCleanTaskTimer",true);
		systemTimers.add(timer);
		return timer;
	}
	
	@Provides
	@Singleton
	@Named("openkad.refresh.timer")
	Timer provideRefreshTimer(@Named("openkad.timers")List<Timer> systemTimers){
		Timer timer = new Timer("FindNodeRefreshTaskTimer",true);
		systemTimers.add(timer);
		return timer;
	}
	
	

	@Provides
	@Named("openkad.bucket.kbuckets")
	Bucket provideKBucket(
			@Named("openkad.bucket.kbuckets.optimal.maxsize") int maxSize,
			@Named("openkad.bucket.valid_timespan") long validTimespan,
			@Named("openkad.executors.ping") ExecutorService pingExecutor,
			Provider<PingRequest> pingRequestProvider,
			Provider<MessageDispatcher<Void>> msgDispatcherProvider) {
		return new StableBucket(maxSize, validTimespan, pingExecutor,
				pingRequestProvider, msgDispatcherProvider);
	}

	@Provides
	@Named("openkad.bucket.slack")
	Bucket provideSlackBucket(@Named("openkad.color.slack.size") int maxSize) {
		// return new SlackBucket(maxSize);
		return new DummyBucket();
	}

	@Provides
	@Singleton
	@Named("openkad.rnd")
	Random provideRandom(@Named("openkad.seed") long seed) {
		return seed == 0 ? new Random() : new Random(seed);
	}

	@Provides
	@Singleton
	KeyFactory provideKeyFactory(
			@Named("openkad.keyfactory.keysize") int keyByteLength,
			@Named("openkad.rnd") Random rnd,
			@Named("openkad.keyfactory.hashalgo") String hashAlgo)
			throws NoSuchAlgorithmException {
		if ("MD4".equals(hashAlgo)) {
			java.security.Security.addProvider(new BouncyCastleProvider());
		}
		return new RandomKeyFactory(keyByteLength, rnd, hashAlgo);
	}

	@Provides
	@Named("openkad.net.expecters")
	@Singleton
	Set<MessageDispatcher<?>> provideExpectersSet() {
		return Collections.synchronizedSet(new HashSet<MessageDispatcher<?>>());
	}

	@Provides
	@Named("openkad.net.udp.sock")
	@Singleton
	DatagramSocket provideKadDatagramSocket(
			@Named("openkad.scheme.name") String kadScheme,
			@Named("openkad.local.node") Node localNode) throws SocketException {
//		System.out.println("binding: " + localNode.getPort(kadScheme));
		return new DatagramSocket(localNode.getPort(kadScheme));
	}

	//处理接收到的数据包的线程池
	@Provides
	@Named("openkad.executors.server")
	@Singleton
	ExecutorService provideServerExecutor(
			@Named("openkad.executors.server.nrthreads") int nrThreads,
			@Named("openkad.executors.server.max_pending") int maxPending) {
		int n=(int) (0.5*nrThreads);
		if(n==0){
			n=1;
		}
		return new ThreadPoolExecutor(n, nrThreads, 5, TimeUnit.MINUTES,
				new ArrayBlockingQueue<Runnable>(maxPending, true),new MyThreadFactory("ServerExecutors"));
	}

	@Provides
	@Named("openkad.executors.ping")
	@Singleton
	ExecutorService providePingExecutor(
			@Named("openkad.executors.ping.nrthreads") int nrThreads,
			@Named("openkad.executors.ping.max_pending") int maxPending) {
		return new ThreadPoolExecutor(1, nrThreads, 5, TimeUnit.MINUTES,
				new ArrayBlockingQueue<Runnable>(maxPending, true),new MyThreadFactory("PingExecutors"));
	}

	@Provides
	@Named("openkad.executors.forward")
	@Singleton
	ExecutorService provideColorExecutor(
			@Named("openkad.executors.forward.nrthreads") int nrThreads,
			@Named("openkad.executors.forward.max_pending") int maxPending) {
		return new ThreadPoolExecutor(1, nrThreads, 5, TimeUnit.MINUTES,
				new ArrayBlockingQueue<Runnable>(maxPending, true),new MyThreadFactory("ForwardExecutors"));
	}

	@Provides
	@Named("openkad.executors.op")
	@Singleton
	ExecutorService provideOperationExecutor(
			@Named("openkad.executors.op.nrthreads") int nrThreads,
			@Named("openkad.executors.op.max_pending") int maxPending) {
		return new ThreadPoolExecutor(1, nrThreads, 5, TimeUnit.MINUTES,
				new ArrayBlockingQueue<Runnable>(maxPending, true),new MyThreadFactory("OperationExecutors"));
	}

	//用于运行发送ContentRequst后的callback
	@Provides
	@Named("openkad.executors.client")
	@Singleton
	ExecutorService provideClientExecutor(
			@Named("openkad.executors.client.nrthreads") int nrThreads,
			@Named("openkad.executors.client.max_pending") int maxPending) {
		return new ThreadPoolExecutor(1, nrThreads, 5, TimeUnit.MINUTES,
				new ArrayBlockingQueue<Runnable>(maxPending, true),new MyThreadFactory("ClientExecutors"));
	}

	@Provides
	@Named("openkad.net.req_queue")
	@Singleton
	BlockingQueue<MessageDispatcher<?>> provideOutstandingRequestsQueue(
			@Named("openkad.net.concurrency") int concurrency) {
		return new ArrayBlockingQueue<MessageDispatcher<?>>(concurrency, true);
//		return new LinkedBlockingQueue<MessageDispatcher<?>>();
	}

	@Provides
	@Named("openkad.rnd.id")
	long provideRandomId(@Named("openkad.rnd") Random rnd) {
		return rnd.nextLong();
	}

	@Provides
	@Named("openkad.keys.zerokey")
	@Singleton
	Key provideZeroKey(KeyFactory keyFactory) {
		return keyFactory.getZeroKey();
	}

	@Provides
	@Named("openkad.local.node")
	@Singleton
	Node provideLocalNode(@Named("openkad.scheme.name") String udpScheme,
			@Named("openkad.scheme2.name") String tcpScheme,
			@Named("openkad.net.udp.port") int udpPort,
			@Named("openkad.net.tcp.port") int tcpPort,
			@Named("openkad.local.key") String base64Key, KeyFactory keyFactory)
			throws UnknownHostException, IOException {

		Key key = base64Key.isEmpty() ? keyFactory.generate() : keyFactory
				.get(base64Key);
		Node n = new Node(key);

		n.setInetAddress(InetAddress.getLocalHost());
		n.addEndpoint(udpScheme, udpPort);
		n.addEndpoint(tcpScheme, tcpPort);

		return n;
	}

	@Provides
	@Named("openkad.refresh.task")
	@Singleton
	TimerTask provideRefreshTask(
			final Provider<EMuleFindNodeOperation> findNodeOperationProvider,
			final KeyFactory keyFactory, final KBuckets kBuckets,
			// @Named("openkad.local.node")final Node localNode,
			final Provider<KadNode> kadNodeProvider) {

		return new TimerTask() {
			List<Node> nodes = null;

			@Override
			public void run() {
				nodes = findNodeOperationProvider.get()
						.setKey(keyFactory.generate())
						// .setKey(localNode.getKey())
						.setRequestType(OpCodes.FIND_NODE).doFindNode();
				for (Node node : nodes) {
					kBuckets.insert(kadNodeProvider.get().setNode(node));
				}
				System.out
						.println("*****************kBuckets.size()="
								+ kBuckets.getAllNodes().size()
								+ "*******************");
			}
		};
	}

	@Provides
	@Named("openkad.local.color")
	@Singleton
	int provideLocalColor(@Named("openkad.local.node") Node localNode,
			@Named("openkad.color.nrcolors") int nrColors) {
		return localNode.getKey().getColor(nrColors);
	}

	private void bindTestingParams() {

		// number of incoming messages
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrIncomingMessages")).toInstance(
				new AtomicInteger(0));

		// number of find nodes with wrong color
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrFindNodesWithWrongColor"))
				.toInstance(new AtomicInteger(0));

		// number of handled forward requests
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrForwardHandling")).toInstance(
				new AtomicInteger(0));

		// number of handled forward requests from initiator
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrForwardHandlingFromInitiator"))
				.toInstance(new AtomicInteger(0));

		// number of nacks recved
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrNacks")).toInstance(
				new AtomicInteger(0));

		// number of long timeouts
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrLongTimeouts")).toInstance(
				new AtomicInteger(0));

		// max number of hops until the result is found (or calculated)
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.maxHopsToResult")).toInstance(
				new AtomicInteger(0));

		// remote cache hits
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.remoteCacheHits")).toInstance(
				new AtomicInteger(0));

		// local cache hits
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.localCacheHits")).toInstance(
				new AtomicInteger(0));

		// number of hops histogram for all find node operations I caused
		// cache hits (find node hops = 0) will not be in here
		bind(new TypeLiteral<List<Integer>>() {
		}).annotatedWith(Names.named("openkad.testing.findNodeHopsHistogram"))
				.toInstance(
						Collections.synchronizedList(new ArrayList<Integer>()));

		// number of hops histogram for all forward operations
		bind(new TypeLiteral<List<Integer>>() {
		}).annotatedWith(Names.named("openkad.testing.hopsToResultHistogram"))
				.toInstance(
						Collections.synchronizedList(new ArrayList<Integer>()));

		// number of hits when requesting find node
		// instead of returning the correct K bucket, we simply return
		// the cached result
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrFindnodeHits")).toInstance(
				new AtomicInteger(0));

		// number of times we did not find anything in the cached results
		// for find node request and returned instead the right K bucjet
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrFindnodeMiss")).toInstance(
				new AtomicInteger(0));

		// number of local cache hits
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrLocalCacheHits")).toInstance(
				new AtomicInteger(0));

		// number of times the cache results was to short
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrShortCacheHits")).toInstance(
				new AtomicInteger(0));

		// number of times the cache of a remote machine had a hit
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrRemoteCacheHits")).toInstance(
				new AtomicInteger(0));

		// the max size of the optimal cache
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.optimalCacheMaxSize")).toInstance(
				new AtomicInteger(0));

		// counts the number of incoming pings
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrIncomingPings")).toInstance(
				new AtomicInteger(0));

		// counts the number of outgoing pings
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrOutgoingPings")).toInstance(
				new AtomicInteger(0));

		// counts the number of short timeouts in the forward algo
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrShortForwardTimeouts"))
				.toInstance(new AtomicInteger(0));

		// total amount of nacks sent
		bind(AtomicInteger.class).annotatedWith(
				Names.named("openkad.testing.nrNacksSent")).toInstance(
				new AtomicInteger(0));

		// total amount of bytes sent
		bind(AtomicLong.class).annotatedWith(
				Names.named("openkad.testing.nrBytesSent")).toInstance(
				new AtomicLong(0));

		// total amount of bytes recved
		bind(AtomicLong.class).annotatedWith(
				Names.named("openkad.testing.nrBytesRecved")).toInstance(
				new AtomicLong(0));
	}
	
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new EMuleKadModule());
//		Map<com.google.inject.Key<?>, Binding<?>> bindings = injector.getAllBindings();
//		for(com.google.inject.Key<?> key:bindings.keySet()){
//			System.out.println(key+":"+bindings.get(key));
//		}
		com.google.inject.Key<String> key=com.google.inject.Key.get(String.class, Names.named("openkad.keyfactory.keysize"));
		System.out.println("\n"+injector.getInstance(key));
	}
}

class MyThreadFactory implements ThreadFactory{
	private int i=1;
	private String name;
	
	public MyThreadFactory(String name) {
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable task) {
		Thread thread=new Thread(task);
		thread.setName(name+"-"+i);
		i++;
		return thread;
	}	
}


class Pool<T> extends ArrayList<T>{
	private static final long serialVersionUID = 5889518818808780040L;
	private Random rnd=new Random();
	public Pool(int capacity){
		super(capacity);
	}
	
	public T get(){
		return get(rnd.nextInt(this.size()));
	}
}
