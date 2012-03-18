/**
 * 
 */
package cn.edu.jnu.cs.emulekad.indexer;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;

import org.jmule.core.edonkey.packet.tag.Tag;
import org.jmule.core.edonkey.packet.tag.TagList;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class Entry {
	private Key clientKey;
	private TagList tagList;
	private long creationTime;

	@Inject
	public Entry(@Named("openkad.local.node") Node localNode) {
		this.clientKey = localNode.getKey();
		this.creationTime = System.currentTimeMillis();
	}

	public Entry(Key clientKey, TagList tagList) {
		this(clientKey, tagList, System.currentTimeMillis());
	}

	public Entry(Key clientKey, TagList tagList, long creationTime) {
		this.clientKey = clientKey;
		this.tagList = tagList;
		this.creationTime = creationTime;
	}

	public Key getClientKey() {
		return clientKey;
	}

	public void setClientKey(Key clientKey) {
		this.clientKey = clientKey;
	}

	public TagList getTagList() {
		return tagList;
	}

	public void setTagList(TagList tagList) {
		this.tagList = tagList;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public Entry addTag(Tag tag) {
		if (tagList == null) {
			tagList = new TagList();
		}
		tagList.addTag(tag);
		return this;
	}

	public Tag getTag(byte[] tagName) {
		if (tagList.hasTag(tagName))
			return tagList.getTag(tagName);
		return null;
	}

	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (!(object instanceof Entry))
			return false;
		Entry entry = (Entry) object;
		if (!entry.getClientKey().equals(getClientKey()))
			return false;
		return true;
	}

	public int hashCode() {
		return getClientKey().hashCode();
	}

	public String toString() {
		String result = "";
		result += "Client Key : " + clientKey + "\n";
		result += tagList;
		return result;
	}
}
