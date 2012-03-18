package cn.edu.jnu.cs.emulekad.msg;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.msg.KadResponse;

public class PublishResponse extends KadResponse {

	private static final long serialVersionUID = -2616522938406039766L;
	
	private Key targetKey;
	private int load;
	
	@Inject
	public PublishResponse(@Named("openkad.rnd.id") long id,
			@Named("openkad.local.node") Node src) {
		super(id, src);
	}

	public Key getTargetKey() {
		return targetKey;
	}

	public PublishResponse setTargetKey(Key targetKey) {
		this.targetKey = targetKey;
		return this;
	}

	public int getLoad() {
		return load;
	}

	public PublishResponse setLoad(int load) {
		this.load = load;
		return this;
	}

}
