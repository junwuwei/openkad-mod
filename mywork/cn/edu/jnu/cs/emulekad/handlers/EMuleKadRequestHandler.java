package cn.edu.jnu.cs.emulekad.handlers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import cn.edu.jnu.cs.emulekad.msg.EMuleKadRequest;
import cn.edu.jnu.cs.emulekad.msg.EMuleKadResponse;
import cn.edu.jnu.cs.emulekad.net.OpCodes;

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

public class EMuleKadRequestHandler extends AbstractHandler {
	
	//dependencies
	private final int findNodeSize;
	private final int findValueSize;
	private final int storeSize;
	private final KBuckets kBuckets;
	private final Node localNode;
	private final KadServer kadServer;

	@Inject
	protected EMuleKadRequestHandler(
			@Named("openkad.findnode.response.max_nodes") int findNodeSize,
			@Named("openkad.findvalue.response.max_nodes") int findValueSize,
			@Named("openkad.store.response.max_nodes") int storeSize,
			@Named("openkad.local.node") Node localNode,
			KBuckets kBuckets,
			Provider<MessageDispatcher<Void>> msgDispatcherProvider,
			KadServer kadServer) {
		super(msgDispatcherProvider);
		this.findNodeSize=findNodeSize;
		this.findValueSize=findValueSize;
		this.storeSize=storeSize;
		this.kBuckets=kBuckets;
		this.localNode=localNode;
		this.kadServer=kadServer;
	}

	@Override
	public void completed(KadMessage msg, Void nothing) {
		EMuleKadRequest req=(EMuleKadRequest) msg;
		List<Node> nodes;
		if(req.getRequestType()==OpCodes.FIND_NODE){
			nodes=kBuckets.getClosestNodesByKey(req.getKey(), findNodeSize);
		}else if(req.getRequestType()==OpCodes.FIND_VALUE){
			nodes=kBuckets.getClosestNodesByKey(req.getKey(), findValueSize);
		}else{
			nodes=kBuckets.getClosestNodesByKey(req.getKey(), storeSize);
		}
		
		EMuleKadResponse res=req.generateResponse(localNode);
		res.setNodes(nodes);
		try {
			kadServer.send(req.getSrc(), res);
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
				EMuleKadRequest.class) });
	}

}
