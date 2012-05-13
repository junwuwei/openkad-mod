package cn.edu.jnu.cs.emulekad.handlers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cn.edu.jnu.cs.emulekad.indexer.Indexer;
import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.msg.SearchRequest;
import cn.edu.jnu.cs.emulekad.msg.SearchResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.handlers.AbstractHandler;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.net.KadServer;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import il.technion.ewolf.kbr.openkad.net.filter.MessageFilter;
import il.technion.ewolf.kbr.openkad.net.filter.TypeMessageFilter;

/**
 * ¥¶¿ÌSearch«Î«Û
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class SearchHandler extends AbstractHandler {

	// dependencies
	private final Indexer indexer;
	private final Node localNode;
	private final KadServer kadServer;

	@Inject
	protected SearchHandler(Indexer indexer,
			@Named("openkad.local.node") Node localNode, KadServer kadServer,
			Provider<MessageDispatcher<Void>> msgDispatcherProvider) {
		super(msgDispatcherProvider);
		this.indexer = indexer;
		this.localNode = localNode;
		this.kadServer = kadServer;
	}

	@Override
	public void completed(KadMessage msg, Void attachment) {
		SearchRequest request = (SearchRequest) msg;
		SearchResponse response = request.generateResponse(localNode);
		List<Entry> entryList = Collections.emptyList();
		switch (request.getSearchType()) {
		case SOURCE:
			entryList = indexer.getSources(request.getTargetKey());
			break;
		case KEYWORD:
			entryList = indexer.getKeywords(request.getTargetKey());
			break;
		case NOTE:
			entryList = indexer.getNotes(request.getTargetKey());
			break;
		}
		if (entryList != null && !entryList.isEmpty()) {
			response.setEntries(entryList);
			try {
				kadServer.send(request.getSrc(), response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void failed(Throwable exc, Void attachment) {
		// should never b here
	}

	@Override
	protected Collection<MessageFilter> getFilters() {
		return Arrays.asList(new MessageFilter[] { new TypeMessageFilter(
				SearchRequest.class) });
	}
}
