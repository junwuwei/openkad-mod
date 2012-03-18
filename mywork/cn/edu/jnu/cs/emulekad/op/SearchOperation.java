package cn.edu.jnu.cs.emulekad.op;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.msg.SearchRequest;
import cn.edu.jnu.cs.emulekad.msg.SearchResponse;
import cn.edu.jnu.cs.emulekad.msg.PublishAndSearchType;
import cn.edu.jnu.cs.emulekad.net.OpCodes;
import cn.edu.jnu.cs.emulekad.net.filter.SrcMessageFilter;
import cn.edu.jnu.cs.emulekad.net.filter.TargetKeyMessageFilter;
import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import il.technion.ewolf.kbr.openkad.net.filter.TypeMessageFilter;

public class SearchOperation implements CompletionHandler<KadMessage, Void> {

	// dependencies
	private final Provider<MessageDispatcher<Void>> messageDispacherProvider;
	private final Provider<EMuleFindValueOperation> findValueOperationProvider;
	private final Node localNode;
	private final int minRecipientSize;

	// state
	private Key targetKey;
	private PublishAndSearchType searchType = PublishAndSearchType.KEYWORD;
	private int startPosition = 0;
	private long fileSize = 0L;
	private List<Node> recipients = Collections.emptyList();
	private List<Entry> entries = new ArrayList<Entry>();
	private CountDownLatch latch;
	private byte requestType = OpCodes.FIND_VALUE;

	private static Logger logger = LoggerFactory
			.getLogger(SearchOperation.class);

	@Inject
	public SearchOperation(
			Provider<MessageDispatcher<Void>> messageDispacherProvider,
			Provider<EMuleFindValueOperation> findValueOperationProvider,
			@Named("openkad.local.node") Node localNode,
			@Named("openkad.search.recipient_minsize") int minRecipientSize) {
		this.messageDispacherProvider = messageDispacherProvider;
		this.findValueOperationProvider = findValueOperationProvider;
		this.localNode = localNode;
		this.minRecipientSize = minRecipientSize;
	}

	public List<Entry> doSearch() {
		while (recipients.size() < minRecipientSize) {
			List<Node> candidates = findValueOperationProvider.get()
					.setKey(targetKey).setRequestType(requestType)
					.doFindValue();
			if (recipients.equals(Collections.emptyList())) {
				recipients = candidates;
			} else {
				candidates.removeAll(recipients);
				recipients.addAll(candidates);
			}
		}

		logger.info("send {} search request to {} nodes", searchType,
				recipients.size());
		logger.info("target key={}", targetKey);
		latch = new CountDownLatch(recipients.size());
		for (Node to : recipients) {
			sendSearch(to);
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("finished search request, find {} entries.", entries.size());
		return entries;

	}

	private void sendSearch(Node to) {
		SearchRequest searchRequest = new SearchRequest(
				System.currentTimeMillis(), localNode);
		searchRequest.setTargetKey(targetKey).setSearchType(searchType)
				.setStartPosition(startPosition).setFileSize(fileSize);
		messageDispacherProvider.get()
				.addFilter(new TargetKeyMessageFilter(targetKey))
				.addFilter(new SrcMessageFilter(to))
				.addFilter(new TypeMessageFilter(SearchResponse.class))
				.setCallback(null, this).send(to, searchRequest);

	}

	@Override
	public synchronized void completed(KadMessage msg, Void attachment) {
		SearchResponse response = (SearchResponse) msg;
		entries.addAll(response.getEntries());
		latch.countDown();
	}

	@Override
	public synchronized void failed(Throwable exc, Void attachment) {
		logger.debug("{}", exc);
		latch.countDown();
	}

	public SearchOperation setTargetKey(Key targetKey) {
		this.targetKey = targetKey;
		return this;
	}

	public SearchOperation setSearchType(PublishAndSearchType searchType) {
		this.searchType = searchType;
		return this;
	}

	public SearchOperation setRecipients(List<Node> recipients) {
		this.recipients = recipients;
		return this;
	}

	public SearchOperation setStartPosition(int startPosition) {
		this.startPosition = startPosition;
		return this;
	}

	public SearchOperation setFileSize(long fileSize) {
		this.fileSize = fileSize;
		return this;
	}

	public SearchOperation setRequestType(byte requestType) {
		this.requestType = requestType;
		return this;
	}

}
