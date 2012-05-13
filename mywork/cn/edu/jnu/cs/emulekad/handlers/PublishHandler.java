package cn.edu.jnu.cs.emulekad.handlers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.indexer.Indexer;
import cn.edu.jnu.cs.emulekad.msg.PublishRequest;
import cn.edu.jnu.cs.emulekad.msg.PublishResponse;

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
 * ¥¶¿ÌPublish«Î«Û
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class PublishHandler extends AbstractHandler {

	// dependencies
	private final Indexer indexer;
	private final Node localNode;
	private final KadServer kadServer;

	@Inject
	protected PublishHandler(Indexer indexer,
			@Named("openkad.local.node") Node localNode, KadServer kadServer,
			Provider<MessageDispatcher<Void>> msgDispatcherProvider) {
		super(msgDispatcherProvider);
		this.indexer = indexer;
		this.localNode = localNode;
		this.kadServer = kadServer;
	}

	@Override
	public void completed(KadMessage msg, Void nothing) {
		PublishRequest request = (PublishRequest) msg;
		int load = -1;
		switch (request.getPublishType()) {
		case SOURCE:
			load = indexer.addSource(request.getTargetKey(), request
					.getEntries().get(0));
			break;
		case KEYWORD:
			for (Entry entry : request.getEntries()) {
				load = indexer.addKeyword(request.getTargetKey(), entry);
				if (load >= 100)
					break;
			}
			break;
		case NOTE:
			load = indexer.addNote(request.getTargetKey(), request.getEntries()
					.get(0));
			break;
		}
		if (0 <= load && load <= 100) {
			PublishResponse response = request.generateResponse(localNode)
					.setLoad(load);
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
				PublishRequest.class) });
	}

}
