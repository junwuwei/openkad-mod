package cn.edu.jnu.cs.emulekad.msg;

import cn.edu.jnu.cs.emulekad.net.OpCodes;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.msg.FindNodeRequest;

/**
 *
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class EMuleKadRequest extends FindNodeRequest {


	private static final long serialVersionUID = -5707029515162082173L;
	
	//state
	private byte requestType = OpCodes.FIND_NODE;
	private Node recipient;

	@Inject
	public EMuleKadRequest(@Named("openkad.rnd.id") long id,
			@Named("openkad.local.node") Node src) {
		super(id, src);
	}

	@Override
	public EMuleKadResponse generateResponse(Node localNode) {
		return new EMuleKadResponse(this.getId(),localNode,getKey());
	}


	public Node getRecipient() {
		return recipient;
	}

	public byte getRequestType() {
		return requestType;
	}

	@Override
	public EMuleKadRequest setKey(Key key) {
		super.setKey(key);
		return this;
	}

	public EMuleKadRequest setRecipient(Node recipient) {
		this.recipient = recipient;
		return this;
	}



	public EMuleKadRequest setRequestType(byte requestType) {
		this.requestType = requestType;
		return this;
	}

	@Override
	public EMuleKadRequest setSearchCache(boolean searchCache) {
		super.setSearchCache(searchCache);
		return this;
	}

}
