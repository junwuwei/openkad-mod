package cn.edu.jnu.cs.emulekad.msg;

import java.util.List;

import cn.edu.jnu.cs.emulekad.indexer.Entry;

import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.msg.KadResponse;

public class SearchResponse extends KadResponse {

	private static final long serialVersionUID = -2760252519369927284L;
	
	private Key targetKey;
	private List<Entry> entries;
	

	public SearchResponse(@Named("openkad.rnd.id")long id, 
			@Named("openkad.local.node") Node src) {
		super(id, src);
	}


	public Key getTargetKey() {
		return targetKey;
	}


	public SearchResponse setTargetKey(Key targetKey) {
		this.targetKey = targetKey;
		return this;
	}

	public List<Entry> getEntries() {
		return entries;
	}


	public SearchResponse setEntries(List<Entry> entries) {
		this.entries = entries;
		return this;
	}

}
