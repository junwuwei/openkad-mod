package cn.edu.jnu.cs.emulekad.msg;

import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.msg.KadRequest;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class SearchRequest extends KadRequest {

	private static final long serialVersionUID = -7527949756773226075L;

	private Key targetKey;
	private PublishAndSearchType searchType = PublishAndSearchType.KEYWORD;
	private int startPosition = 0;
	private long fileSize = 0L;

	public SearchRequest(@Named("openkad.rnd.id") long id,
			@Named("openkad.local.node") Node src) {
		super(id, src);
	}

	@Override
	public SearchResponse generateResponse(Node localNode) {
		return new SearchResponse(this.getId(), localNode)
				.setTargetKey(this.targetKey);
	}

	public Key getTargetKey() {
		return targetKey;
	}

	public SearchRequest setTargetKey(Key targetKey) {
		this.targetKey = targetKey;
		return this;
	}

	public PublishAndSearchType getSearchType() {
		return searchType;
	}

	public SearchRequest setSearchType(PublishAndSearchType searchType) {
		this.searchType = searchType;
		return this;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public SearchRequest setStartPosition(int startPosition) {
		this.startPosition = startPosition;
		return this;
	}

	public long getFileSize() {
		return fileSize;
	}

	public SearchRequest setFileSize(long fileSize) {
		this.fileSize = fileSize;
		return this;
	}

}
