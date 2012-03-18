package cn.edu.jnu.cs.emulekad.op;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.msg.PublishAndSearchType;
import cn.edu.jnu.cs.emulekad.msg.PublishRequest;
import cn.edu.jnu.cs.emulekad.msg.PublishResponse;
import cn.edu.jnu.cs.emulekad.net.OpCodes;
import cn.edu.jnu.cs.emulekad.net.filter.SrcMessageFilter;
import cn.edu.jnu.cs.emulekad.net.filter.TargetKeyMessageFilter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import il.technion.ewolf.kbr.openkad.net.filter.TypeMessageFilter;

public class PublishOperation implements CompletionHandler<KadMessage, Void> {

	// dependencies
	private final Provider<MessageDispatcher<Void>> messageDispacherProvider;
	private final Provider<EMuleFindValueOperation> findValueOperationProvider;
	private final Node localNode;
	private final int minRecipientSize;

	// state
	private Key targetKey;
	private PublishAndSearchType publishType = PublishAndSearchType.KEYWORD;
	private byte requestType = OpCodes.STORE;
	private List<Entry> entries;
	private List<Node> recipients = Collections.emptyList();
	private CountDownLatch latch;

	// testing
	private AtomicInteger nrCompleted = new AtomicInteger(0);
	private AtomicInteger nrFailed = new AtomicInteger(0);

	private static Logger logger = LoggerFactory
			.getLogger(PublishOperation.class);

	@Inject
	public PublishOperation(
			Provider<MessageDispatcher<Void>> messageDispacherProvider,
			Provider<EMuleFindValueOperation> findValueOperationProvider,
			@Named("openkad.scheme.name") String udpScheme,
			@Named("openkad.local.node") Node localNode,
			@Named("openkad.publish.recipient_minsize") int minRecipientSize) {
		this.messageDispacherProvider = messageDispacherProvider;
		this.findValueOperationProvider = findValueOperationProvider;
		this.localNode = localNode;
		this.minRecipientSize = minRecipientSize;
	}

	public int doPublish() {
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

		latch = new CountDownLatch(recipients.size());
		logger.info("Publish {} to {} nodes", publishType, recipients.size());
		logger.info("target key={}", targetKey);

		for (Node to : recipients) {
			sentPublish(to);
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("publish finished, nrCompleted={}, nrFailed={}",
				nrCompleted, nrFailed);
		return nrCompleted.get();
	}

	private void sentPublish(Node to) {
		PublishRequest request = new PublishRequest(System.currentTimeMillis(),
				localNode);
		request.setTargetKey(targetKey).setPublishType(publishType)
				.setEntries(entries);
		messageDispacherProvider.get()
				.addFilter(new TypeMessageFilter(PublishResponse.class))
				.addFilter(new TargetKeyMessageFilter(targetKey))
				.addFilter(new SrcMessageFilter(to)).setCallback(null, this)
				.send(to, request);

	}

	@Override
	public void completed(KadMessage msg, Void attachment) {
		PublishResponse res = (PublishResponse) msg;
		if (res.getLoad() < 100) {
			nrCompleted.incrementAndGet();
		} else {
			nrFailed.incrementAndGet();
		}
		latch.countDown();
	}

	@Override
	public void failed(Throwable exc, Void attachment) {
		logger.debug("{}", exc);
		nrFailed.incrementAndGet();
		latch.countDown();
	}

	public PublishOperation setTargetKey(Key targetKey) {
		this.targetKey = targetKey;
		return this;
	}

	public PublishOperation setPublishType(PublishAndSearchType publishType) {
		this.publishType = publishType;
		return this;
	}

	public PublishOperation setRecipients(List<Node> recipients) {
		this.recipients = recipients;
		return this;
	}

	public PublishOperation setEntries(List<Entry> entries) {
		this.entries = entries;
		return this;
	}

	public PublishOperation addEntry(Entry entry) {
		if (entries == null) {
			entries = new LinkedList<Entry>();
		}
		entries.add(entry);
		return this;
	}

	public PublishOperation setRequestType(byte requestType) {
		this.requestType = requestType;
		return this;
	}

}
