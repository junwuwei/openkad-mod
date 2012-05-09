/**
 * 
 */
package cn.edu.jnu.cs.emulekad;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.jnu.cs.emulekad.handlers.BootstrapHandler;
import cn.edu.jnu.cs.emulekad.handlers.EMuleKadRequestHandler;
import cn.edu.jnu.cs.emulekad.handlers.PublishHandler;
import cn.edu.jnu.cs.emulekad.handlers.SearchHandler;
import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.indexer.Indexer;
import cn.edu.jnu.cs.emulekad.msg.PublishAndSearchType;
import cn.edu.jnu.cs.emulekad.op.EMuleFindValueOperation;
import cn.edu.jnu.cs.emulekad.op.EMuleJoinOperation;
import cn.edu.jnu.cs.emulekad.op.PublishOperation;
import cn.edu.jnu.cs.emulekad.op.SearchOperation;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.KeybasedRouting;
import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;
import il.technion.ewolf.kbr.concurrent.FutureTransformer;
import il.technion.ewolf.kbr.openkad.IncomingContentHandler;
import il.technion.ewolf.kbr.openkad.KBuckets;
import il.technion.ewolf.kbr.openkad.KadNetModule;
import il.technion.ewolf.kbr.openkad.handlers.PingHandler;
import il.technion.ewolf.kbr.openkad.msg.ContentMessage;
import il.technion.ewolf.kbr.openkad.msg.ContentRequest;
import il.technion.ewolf.kbr.openkad.msg.ContentResponse;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.net.KadServer;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import il.technion.ewolf.kbr.openkad.net.filter.IdMessageFilter;
import il.technion.ewolf.kbr.openkad.net.filter.TagMessageFilter;
import il.technion.ewolf.kbr.openkad.net.filter.TypeMessageFilter;
import il.technion.ewolf.kbr.openkad.op.FindValueOperation;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class EMuleKadNet implements EMuleKad {

	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new KadNetModule()
				.setProperty("openkad.net.udp.port", "5555"));
		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
		kbr.create();
	}

	// dependencies
	private final Provider<MessageDispatcher<Object>> msgDispatcherProvider;
	private final Provider<EMuleJoinOperation> joinOperationProvider;
	private final Provider<ContentRequest> contentRequestProvider;
	private final Provider<ContentMessage> contentMessageProvider;
	private final Provider<IncomingContentHandler<Object>> incomingContentHandlerProvider;
	private final Provider<EMuleFindValueOperation> findValueOperationProvider;
	private final Provider<PublishOperation> publishOperationProvider;
	private final Provider<SearchOperation> searchOperationProvider;
	private final Provider<PingHandler> pingHandler;
	private final Provider<BootstrapHandler> BootStrapHandlerProvider;
	private final Provider<EMuleKadRequestHandler> eMuleKadRequestHandlerProvider;
	private final Provider<SearchHandler> searchHandlerProvider;
	private final Provider<ExecutorService> pingExecutorProvider;

	private final Provider<PublishHandler> publishHandlerProvider;
	private final Node localNode;
	private final KadServer kadServer;
	private final KBuckets kBuckets;
	private final Indexer indexer;
	private final KeyFactory keyFactory;
	private final ExecutorService clientExecutor;
	private final int bucketSize;
	private final Provider<List<Timer>> systemTimersProvider;
	private final int nrReceiveThreads;

	// testing
	private final List<Integer> findNodeHopsHistogram;
	// state
	private final Map<String, MessageDispatcher<?>> dispatcherFromTag = new HashMap<String, MessageDispatcher<?>>();
	private List<Thread> kadServerThreads = new LinkedList<Thread>();

	private boolean isCreated = false;

	private static Logger logger = LoggerFactory.getLogger(EMuleKadNet.class);

	@Inject
	EMuleKadNet(
			Provider<MessageDispatcher<Object>> msgDispatcherProvider,
			Provider<EMuleJoinOperation> joinOperationProvider,
			Provider<ContentRequest> contentRequestProvider,
			Provider<ContentMessage> contentMessageProvider,
			Provider<IncomingContentHandler<Object>> incomingContentHandlerProvider,
			Provider<EMuleFindValueOperation> findValueOperationProvider,
			Provider<PublishOperation> publishOperationProvider,
			Provider<SearchOperation> searchOperationProvider,
			Provider<EMuleKadRequestHandler> eMuleKadRequestHandlerProvider,
			Provider<PingHandler> pingHandler,
			Provider<BootstrapHandler> BootStrapHandlerProvider,
			Provider<SearchHandler> searchHandlerProvider,
			Provider<PublishHandler> publishHandlerProvider,
			@Named("openkad.executors.ping")Provider<ExecutorService> pingExecutorProvider,
			@Named("openkad.local.node") Node localNode,
			KadServer kadServer,
			KBuckets kBuckets,
			Indexer indexer,
			KeyFactory keyFactory,
			@Named("openkad.executors.client") ExecutorService clientExecutor,
			@Named("openkad.bucket.kbuckets.maxsize") int bucketSize,
			@Named("openkad.timers") Provider<List<Timer>> systemTimersProvider,
			// testing
			@Named("openkad.testing.findNodeHopsHistogram") List<Integer> findNodeHopsHistogram,
			@Named("openkad.receive.server.threads")int nrReceiveThreads) {

		this.msgDispatcherProvider = msgDispatcherProvider;
		this.joinOperationProvider = joinOperationProvider;
		this.contentRequestProvider = contentRequestProvider;
		this.contentMessageProvider = contentMessageProvider;
		this.incomingContentHandlerProvider = incomingContentHandlerProvider;
		this.findValueOperationProvider = findValueOperationProvider;
		this.publishOperationProvider = publishOperationProvider;
		this.searchOperationProvider = searchOperationProvider;
		this.eMuleKadRequestHandlerProvider = eMuleKadRequestHandlerProvider;
		this.pingHandler = pingHandler;
		this.BootStrapHandlerProvider = BootStrapHandlerProvider;
		this.searchHandlerProvider = searchHandlerProvider;
		this.publishHandlerProvider = publishHandlerProvider;
		this.pingExecutorProvider=pingExecutorProvider;
		this.localNode = localNode;
		this.kadServer = kadServer;
		this.kBuckets = kBuckets;
		this.indexer = indexer;
		this.keyFactory = keyFactory;
		this.clientExecutor = clientExecutor;
		this.bucketSize = bucketSize;
		this.systemTimersProvider=systemTimersProvider;
		this.nrReceiveThreads=nrReceiveThreads;
		// testing
		this.findNodeHopsHistogram = findNodeHopsHistogram;
	}

	@Override
	public void create() throws IOException {
		if (!isCreated) {
			// bind communicator and register all handlers
			kadServer.bind();
			pingHandler.get().register();
			eMuleKadRequestHandlerProvider.get().register();
			BootStrapHandlerProvider.get().register();
			searchHandlerProvider.get().register();
			publishHandlerProvider.get().register();

			kBuckets.registerIncomingMessageHandler();
			indexer.scheduleCleanTask();
			for (int i = 1; i <= nrReceiveThreads; i++) {
				Thread kadServerThread = new Thread(kadServer);
				kadServerThread.setName("kadServer"+i);
				kadServerThread.start();
				kadServerThreads.add(kadServerThread);
			}

			isCreated = true;
			logger.info("EMuleKadNet created.");
			logger.info("local node:{}", localNode);
		}
	}

	@Override
	public List<Node> findNode(Key k) {
		FindValueOperation op = findValueOperationProvider.get().setKey(k);

		List<Node> result = op.doFindValue();
		findNodeHopsHistogram.add(op.getNrQueried());

		List<Node> $ = new ArrayList<Node>(result);

		if ($.size() > bucketSize)
			$.subList(bucketSize, $.size()).clear();

		return result;
	}

	@Override
	public KeyFactory getKeyFactory() {
		return keyFactory;
	}

	@Override
	public Node getLocalNode() {
		return localNode;
	}

	@Override
	public List<Node> getNeighbours() {
		return kBuckets.getAllNodes();
	}

	@Override
	public void joinNode(Collection<Node> bootstraps) {
		joinOperationProvider.get().addBootstrapNode(bootstraps).doJoin();

	}

	@Override
	public void joinURI(Collection<URI> bootstraps) {
		joinOperationProvider.get().addBootstrapURI(bootstraps).doJoin();
	}

	@Override
	public int publishKeyword(Key targetKey, List<Entry> entries) {
		return publishOperationProvider.get().setTargetKey(targetKey)
				.setEntries(entries)
				.setPublishType(PublishAndSearchType.KEYWORD).doPublish();
	}

	@Override
	public int publishNote(Key targetKey, Entry entry) {
		return publishOperationProvider.get().setTargetKey(targetKey)
				.addEntry(entry).setPublishType(PublishAndSearchType.NOTE)
				.doPublish();
	}

	@Override
	public int publishSource(Key targetKey, Entry entry) {
		return publishOperationProvider.get().setTargetKey(targetKey)
				.addEntry(entry).setPublishType(PublishAndSearchType.SOURCE)
				.doPublish();
	}

	@Override
	public synchronized void register(String tag, MessageHandler handler) {
		MessageDispatcher<?> dispatcher = dispatcherFromTag.get(tag);
		if (dispatcher != null)
			dispatcher.cancel(new CancellationException());

		dispatcher = msgDispatcherProvider
				.get()
				.addFilter(new TagMessageFilter(tag))
				.setConsumable(false)
				.setCallback(
						null,
						incomingContentHandlerProvider.get()
								.setHandler(handler).setTag(tag)).register();

		dispatcherFromTag.put(tag, dispatcher);
	}

	@Override
	public List<Entry> searchKeyword(Key targetKey) {
		return searchOperationProvider.get().setTargetKey(targetKey)
				.setSearchType(PublishAndSearchType.KEYWORD).doSearch();
	}

	@Override
	public List<Entry> searchNote(Key targetKey) {
		return searchOperationProvider.get().setTargetKey(targetKey)
				.setSearchType(PublishAndSearchType.NOTE).doSearch();
	}

	@Override
	public List<Entry> searchSource(Key targetKey) {
		return searchOperationProvider.get().setTargetKey(targetKey)
				.setSearchType(PublishAndSearchType.SOURCE).doSearch();
	}

	@Override
	public void sendMessage(Node to, String tag, Serializable msg)
			throws IOException {
		kadServer.send(to,
				contentMessageProvider.get().setTag(tag).setContent(msg));
	}

	@Override
	public Future<Serializable> sendRequest(Node to, String tag,
			Serializable msg) {

		ContentRequest contentRequest = contentRequestProvider.get()
				.setTag(tag).setContent(msg);

		Future<KadMessage> futureSend = msgDispatcherProvider.get()
				.setConsumable(true)
				.addFilter(new TypeMessageFilter(ContentResponse.class))
				.addFilter(new IdMessageFilter(contentRequest.getId()))
				.futureSend(to, contentRequest);

		return new FutureTransformer<KadMessage, Serializable>(futureSend) {
			@Override
			protected Serializable transform(KadMessage msg) throws Throwable {
				return ((ContentResponse) msg).getContent();
			}
		};
	}

	@Override
	public <A> void sendRequest(Node to, String tag, Serializable msg,
			final A attachment, final CompletionHandler<Serializable, A> handler) {
		ContentRequest contentRequest = contentRequestProvider.get()
				.setTag(tag).setContent(msg);

		msgDispatcherProvider.get().setConsumable(true)
				.addFilter(new TypeMessageFilter(ContentResponse.class))
				.addFilter(new IdMessageFilter(contentRequest.getId()))
				.setCallback(null, new CompletionHandler<KadMessage, Object>() {
					@Override
					public void completed(KadMessage msg, Object nothing) {
						final ContentResponse contentResponse = (ContentResponse) msg;
						clientExecutor.execute(new Runnable() {
							@Override
							public void run() {
								handler.completed(contentResponse.getContent(),
										attachment);
							}
						});
					}

					@Override
					public void failed(Throwable exc, Object nothing) {
						handler.failed(exc, attachment);
					}
				}).send(to, contentRequest);
	}

	@Override
	public void shutdown() {
		pingExecutorProvider.get().shutdownNow();
		for(Timer timer:systemTimersProvider.get()){
			timer.cancel();
		}
		for (Thread kadServerThread:kadServerThreads) {
			kadServer.shutdown(kadServerThread);
		}		
			
		logger.info("EMuleKadNet shutdowned.");
	}

	@Override
	public String toString() {
		return localNode.toString() + "\n" + kBuckets.toString();
	}

}
