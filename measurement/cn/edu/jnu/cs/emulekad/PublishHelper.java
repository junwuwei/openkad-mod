/**
 * 
 */
package cn.edu.jnu.cs.emulekad;

import org.jmule.core.edonkey.packet.tag.ByteTag;
import org.jmule.core.edonkey.packet.tag.IntTag;
import org.jmule.core.edonkey.packet.tag.LongTag;
import org.jmule.core.edonkey.packet.tag.ShortTag;
import org.jmule.core.edonkey.packet.tag.StringTag;
import org.jmule.core.edonkey.packet.tag.Tag;
import org.jmule.core.edonkey.packet.tag.TagList;

import il.technion.ewolf.kbr.Node;

import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.indexer.tag.TagNames;
import cn.edu.jnu.cs.emulekad.util.IOUtil;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class PublishHelper {
	private final Node localNode;
	private final String udpScheme;
	private final String tcpScheme;

	@Inject
	public PublishHelper(@Named("openkad.local.node") Node localNode,
			@Named("openkad.scheme.name") String udpScheme,
			@Named("openkad.scheme2.name") String tcpScheme) {
		this.localNode = localNode;
		this.udpScheme = udpScheme;
		this.tcpScheme = tcpScheme;
	}

	public Entry makeSourceEntry(String vanishString) {
		TagList tagList = new TagList();
		Tag fileSize = new LongTag(TagNames.TAG_FILESIZE, 734197760L);
		Tag ip = new IntTag(TagNames.TAG_SOURCEIP,
				IOUtil.ipBytesToInt(localNode.getInetAddress().getAddress()));
		Tag sourceType = new ByteTag(TagNames.TAG_SOURCETYPE, (byte) 1);
		Tag tcpPort = new ShortTag(TagNames.TAG_SOURCEPORT,
				(short) localNode.getPort(tcpScheme));
		Tag udpPort = new ShortTag(TagNames.TAG_SOURCEUPORT,
				(short) localNode.getPort(udpScheme));
		Tag encryption = new ByteTag(TagNames.TAG_ENCRYPTION, (byte) 1);
		Tag vanishValue = new StringTag(TagNames.TAG_VANISH, vanishString);
		tagList.addTag(fileSize);
		tagList.addTag(ip);
		tagList.addTag(sourceType);
		tagList.addTag(tcpPort);
		tagList.addTag(udpPort);
		tagList.addTag(encryption);
		tagList.addTag(encryption);
		tagList.addTag(vanishValue);
		Entry entry = new Entry(localNode.getKey(), tagList);
		return entry;
	}

	public Entry makeKeywordEntry(String vanishString) {
		TagList tagList = new TagList();
		Tag fileSize = new LongTag(TagNames.TAG_FILESIZE, 734197760L);
		Tag filename = new StringTag(TagNames.TAG_FILENAME, "ÎÄ¼þÃû");
		tagList.addTag(filename);
		tagList.addTag(fileSize);
		Tag vanishValue = new StringTag(TagNames.TAG_VANISH, vanishString);
		tagList.addTag(vanishValue);
		Entry entry = new Entry(localNode.getKey(), tagList);
		return entry;
	}

	public Entry makeNoteEntry(String vanishString) {
		TagList tagList = new TagList();
		Tag vanishValue = new StringTag(TagNames.TAG_VANISH, vanishString);
		tagList.addTag(vanishValue);
		Entry entry = new Entry(localNode.getKey(), tagList);
		return entry;
	}
	
	
	public  String getVanishString(Entry entry){
		if(entry.getTagList().hasTag(TagNames.TAG_VANISH)){
			return (String) entry.getTagList().getTag(TagNames.TAG_VANISH).getValue();
		}
		return "";
	}
}
