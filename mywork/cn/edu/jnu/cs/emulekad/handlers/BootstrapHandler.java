/**
 * 
 */
package cn.edu.jnu.cs.emulekad.handlers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cn.edu.jnu.cs.emulekad.msg.BootstrapRequest;
import cn.edu.jnu.cs.emulekad.msg.BootstrapResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.KBuckets;
import il.technion.ewolf.kbr.openkad.handlers.AbstractHandler;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.net.KadServer;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import il.technion.ewolf.kbr.openkad.net.filter.MessageFilter;
import il.technion.ewolf.kbr.openkad.net.filter.TypeMessageFilter;

/**
 * ¥¶¿ÌBootstrap«Î«Û
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class BootstrapHandler extends AbstractHandler {

	// dependencies
	private final KadServer kadServer;
	private final Node localNode;
	private final KBuckets kBuckets;
	private final int bootstrapSize;

	@Inject
	BootstrapHandler(Provider<MessageDispatcher<Void>> msgDispatcherProvider,
			KadServer kadServer,
			@Named("openkad.local.node") Node localNode,
			@Named("openkad.bootstrap.response.max_nodes") int bootstrapSize,
			KBuckets kBuckets) {
		super(msgDispatcherProvider);
		this.kadServer = kadServer;
		this.localNode = localNode;
		this.bootstrapSize = bootstrapSize;
		this.kBuckets = kBuckets;
	}

	@Override
	public void completed(KadMessage msg, Void attachment) {
		BootstrapRequest req = (BootstrapRequest) msg;
		BootstrapResponse rep = req.generateResponse(localNode);
		List<Node> nodes = kBuckets.getAllNodes();
		Collections.shuffle(nodes);
		if (nodes.size() > bootstrapSize) {
			nodes.subList( bootstrapSize,nodes.size()).clear();
		}
		rep.setNodes(nodes);
		try {
			kadServer.send(msg.getSrc(), rep);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void failed(Throwable exc, Void attachment) {
		// should never b here
	}

	@Override
	protected Collection<MessageFilter> getFilters() {
		return Arrays.asList(new MessageFilter[] { new TypeMessageFilter(
				BootstrapRequest.class) });
	}

}
