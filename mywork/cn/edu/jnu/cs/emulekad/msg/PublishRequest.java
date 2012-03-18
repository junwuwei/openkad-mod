package cn.edu.jnu.cs.emulekad.msg;

import java.util.List;

import cn.edu.jnu.cs.emulekad.indexer.Entry;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.msg.KadRequest;

public class PublishRequest extends KadRequest {

	private static final long serialVersionUID = -461367860180913454L;

	// state
	private PublishAndSearchType publishType;
	private Key targetKey;
	private List<Entry> entries;

	@Inject
	public PublishRequest(@Named("openkad.rnd.id") long id,
			@Named("openkad.local.node") Node src) {
		super(id, src);
	}

	@Override
	public PublishResponse generateResponse(Node localNode) {
		PublishResponse response = new PublishResponse(this.getId(), localNode);
		response.setTargetKey(targetKey);
		return response;
	}

	public PublishAndSearchType getPublishType() {
		return publishType;
	}

	public PublishRequest setPublishType(PublishAndSearchType publishType) {
		this.publishType = publishType;
		return this;
	}

	public Key getTargetKey() {
		return targetKey;
	}

	public PublishRequest setTargetKey(Key targetKey) {
		this.targetKey = targetKey;
		return this;
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public PublishRequest setEntries(List<Entry> entries) {
		this.entries = entries;
		return this;
	}



}
