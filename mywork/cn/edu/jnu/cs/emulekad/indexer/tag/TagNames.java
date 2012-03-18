/**
 * 
 */
package cn.edu.jnu.cs.emulekad.indexer.tag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class TagNames {

	// file tags
	public static byte[] TAG_FILENAME = new byte[] { (byte) 0x01 };
	public static byte[] TAG_FILESIZE = new byte[] { (byte) 0x02 };
	public static byte[] TAG_FILESIZE_HI = new byte[] { (byte) 0x3A };
	public static byte[] TAG_FILETYPE = new byte[] { (byte) 0x03 };
	public static byte[] TAG_FILEFORMAT = new byte[] { (byte) 0x04 };
	public static byte[] TAG_LASTSEENCOMPLETE = new byte[] { (byte) 0x05 };
	public static byte[] TAG_COLLECTION = new byte[] { (byte) 0x05 };
	public static byte[] TAG_PART_PATH = new byte[] { (byte) 0x06 };
	public static byte[] TAG_PART_HASH = new byte[] { (byte) 0x07 };
	public static byte[] TAG_TRANSFERRED = new byte[] { (byte) 0x08 };
	public static byte[] TAG_GAPSTART = new byte[] { (byte) 0x09 };
	public static byte[] TAG_GAPEND = new byte[] { (byte) 0x0A };
	public static byte[] TAG_DESCRIPTION = new byte[] { (byte) 0x0B };
	public static byte[] TAG_PING = new byte[] { (byte) 0x0C };
	public static byte[] TAG_FAIL = new byte[] { (byte) 0x0D };
	public static byte[] TAG_PREFERENCE = new byte[] { (byte) 0x0E };
	public static byte[] TAG_PORT = new byte[] { (byte) 0x0F };
	public static byte[] TAG_IP_ADDRESS = new byte[] { (byte) 0x10 };
	public static byte[] TAG_VERSION = new byte[] { (byte) 0x11 };
	public static byte[] TAG_PARTFILENAME = new byte[] { (byte) 0x12 };

	public static byte[] TAG_PRIORITY = new byte[] { (byte) 0x13 };
	public static byte[] TAG_STATUS = new byte[] { (byte) 0x14 };
	public static byte[] TAG_SOURCES = new byte[] { (byte) 0x15 };
	public static byte[] TAG_PERMISSIONS = new byte[] { (byte) 0x16 };

	public static byte[] TAG_PARTS = new byte[] { (byte) 0x17 };
	public static byte[] TAG_DLPRIORITY = new byte[] { (byte) 0x18 };
	public static byte[] TAG_ULPRIORITY = new byte[] { (byte) 0x19 };
	public static byte[] TAG_COMPRESSION = new byte[] { (byte) 0x1A };
	public static byte[] TAG_CORRUPTED = new byte[] { (byte) 0x1B };
	public static byte[] TAG_KADLASTPUBLISHKEY = new byte[] { (byte) 0x20 };
	public static byte[] TAG_KADLASTPUBLISHSRC = new byte[] { (byte) 0x21 };
	public static byte[] TAG_FLAGS = new byte[] { (byte) 0x22 };
	public static byte[] TAG_DL_ACTIVE_TIME = new byte[] { (byte) 0x23 };
	public static byte[] TAG_CORRUPTEDPARTS = new byte[] { (byte) 0x24 };
	public static byte[] TAG_DL_PREVIEW = new byte[] { (byte) 0x25 };
	public static byte[] TAG_KADLASTPUBLISHNOTES = new byte[] { (byte) 0x26 };
	public static byte[] TAG_AICH_HASH = new byte[] { (byte) 0x27 };
	public static byte[] TAG_FILEHASH = new byte[] { (byte) 0x28 };
	public static byte[] TAG_COMPLETE_SOURCES = new byte[] { (byte) 0x30 };
	public static byte[] TAG_COLLECTIONAUTHOR = new byte[] { (byte) 0x31 };
	public static byte[] TAG_COLLECTIONAUTHORKEY = new byte[] { (byte) 0x32 };
	public static byte[] TAG_PUBLISHINFO = new byte[] { (byte) 0x33 };
	public static byte[] TAG_LASTSHARED = new byte[] { (byte) 0x34 };
	public static byte[] TAG_AICHHASHSET = new byte[] { (byte) 0x35 };
	public static byte[] TAG_KADAICHHASHPUB = new byte[] { (byte) 0x36 };
	public static byte[] TAG_KADAICHHASHRESULT = new byte[] { (byte) 0x37 };

	// statistic
	public static byte[] TAG_ATTRANSFERRED = new byte[] { (byte) 0x50 };
	public static byte[] TAG_ATREQUESTED = new byte[] { (byte) 0x51 };
	public static byte[] TAG_ATACCEPTED = new byte[] { (byte) 0x52 };
	public static byte[] TAG_CATEGORY = new byte[] { (byte) 0x53 };
	public static byte[] TAG_ATTRANSFERREDHI = new byte[] { (byte) 0x54 };
	public static byte[] TAG_MAXSOURCES = new byte[] { (byte) 0x55 };
	public static byte[] TAG_MEDIA_ARTIST = new byte[] { (byte) 0xD0 };
	public static byte[] TAG_MEDIA_ALBUM = new byte[] { (byte) 0xD1 };
	public static byte[] TAG_MEDIA_TITLE = new byte[] { (byte) 0xD2 };
	public static byte[] TAG_MEDIA_LENGTH = new byte[] { (byte) 0xD3 };
	public static byte[] TAG_MEDIA_BITRATE = new byte[] { (byte) 0xD4 };
	public static byte[] TAG_MEDIA_CODEC = new byte[] { (byte) 0xD5 };
	public static byte[] TAG_KADMISCOPTIONS = new byte[] { (byte) 0xF2 };
	public static byte[] TAG_ENCRYPTION = new byte[] { (byte) 0xF3 };
	public static byte[] TAG_USER_COUNT = new byte[] { (byte) 0xF4 };
	public static byte[] TAG_FILE_COUNT = new byte[] { (byte) 0xF5 };
	public static byte[] TAG_FILECOMMENT = new byte[] { (byte) 0xF6 };
	public static byte[] TAG_FILERATING = new byte[] { (byte) 0xF7 };
	public static byte[] TAG_BUDDYHASH = new byte[] { (byte) 0xF8 };
	public static byte[] TAG_CLIENTLOWID = new byte[] { (byte) 0xF9 };
	public static byte[] TAG_SERVERPORT = new byte[] { (byte) 0xFA };
	public static byte[] TAG_SERVERIP = new byte[] { (byte) 0xFB };
	public static byte[] TAG_SOURCEUPORT = new byte[] { (byte) 0xFC };
	public static byte[] TAG_SOURCEPORT = new byte[] { (byte) 0xFD };
	public static byte[] TAG_SOURCEIP = new byte[] { (byte) 0xFE };
	public static byte[] TAG_SOURCETYPE = new byte[] { (byte) 0xFF };
	
	//custom tag name
	//#define FT_PRIORITY			 0x13	// Not used anymore
	//#define FT_ULPRIORITY			 0x17	// Not used anymore
	
	public static byte[] TAG_VANISH=new byte[]{(byte)0x13};
	

	private static Map<Integer, String> tagNameStrings = new HashMap<Integer, String>();

	static {
		tagNameStrings.put(Arrays.hashCode(TAG_FILENAME), "filename");
		tagNameStrings.put(Arrays.hashCode(TAG_FILESIZE), "filesize");
		tagNameStrings.put(Arrays.hashCode(TAG_FILESIZE_HI), "filesize_hi");
		tagNameStrings.put(Arrays.hashCode(TAG_FILETYPE), "filetype");
		tagNameStrings.put(Arrays.hashCode(TAG_FILEFORMAT), "fileformat");
		tagNameStrings.put(Arrays.hashCode(TAG_LASTSEENCOMPLETE),
				"lastseencomplete");
		tagNameStrings.put(Arrays.hashCode(TAG_COLLECTION), "collection");
		tagNameStrings.put(Arrays.hashCode(TAG_PART_PATH), "part_path");
		tagNameStrings.put(Arrays.hashCode(TAG_PART_HASH), "part_hash");
		tagNameStrings.put(Arrays.hashCode(TAG_TRANSFERRED), "transferred");
		tagNameStrings.put(Arrays.hashCode(TAG_GAPSTART), "gapstart");
		tagNameStrings.put(Arrays.hashCode(TAG_GAPEND), "gapend");
		tagNameStrings.put(Arrays.hashCode(TAG_DESCRIPTION), "description");
		tagNameStrings.put(Arrays.hashCode(TAG_PING), "ping");
		tagNameStrings.put(Arrays.hashCode(TAG_FAIL), "fail");
		tagNameStrings.put(Arrays.hashCode(TAG_PREFERENCE), "preference");
		tagNameStrings.put(Arrays.hashCode(TAG_PORT), "port");
		tagNameStrings.put(Arrays.hashCode(TAG_IP_ADDRESS), "ip_address");
		tagNameStrings.put(Arrays.hashCode(TAG_VERSION), "version");
		tagNameStrings.put(Arrays.hashCode(TAG_PARTFILENAME), "partfilename");
		tagNameStrings.put(Arrays.hashCode(TAG_PRIORITY), "priority");
		tagNameStrings.put(Arrays.hashCode(TAG_STATUS), "status");
		tagNameStrings.put(Arrays.hashCode(TAG_SOURCES), "sources");
		tagNameStrings.put(Arrays.hashCode(TAG_PERMISSIONS), "permissions");
		tagNameStrings.put(Arrays.hashCode(TAG_PARTS), "parts");
		tagNameStrings.put(Arrays.hashCode(TAG_DLPRIORITY), "dlpriority");
		tagNameStrings.put(Arrays.hashCode(TAG_ULPRIORITY), "ulpriority");
		tagNameStrings.put(Arrays.hashCode(TAG_COMPRESSION), "compression");
		tagNameStrings.put(Arrays.hashCode(TAG_CORRUPTED), "corrupted");
		tagNameStrings.put(Arrays.hashCode(TAG_KADLASTPUBLISHKEY),
				"kadlastpublishkey");
		tagNameStrings.put(Arrays.hashCode(TAG_KADLASTPUBLISHSRC),
				"kadlastpublishsrc");
		tagNameStrings.put(Arrays.hashCode(TAG_FLAGS), "flags");
		tagNameStrings.put(Arrays.hashCode(TAG_DL_ACTIVE_TIME),
				"dl_active_time");
		tagNameStrings.put(Arrays.hashCode(TAG_CORRUPTEDPARTS),
				"corruptedparts");
		tagNameStrings.put(Arrays.hashCode(TAG_DL_PREVIEW), "dl_preview");
		tagNameStrings.put(Arrays.hashCode(TAG_KADLASTPUBLISHNOTES),
				"kadlastpublishnotes");
		tagNameStrings.put(Arrays.hashCode(TAG_AICH_HASH), "aich_hash");
		tagNameStrings.put(Arrays.hashCode(TAG_FILEHASH), "filehash");
		tagNameStrings.put(Arrays.hashCode(TAG_COMPLETE_SOURCES),
				"complete_sources");
		tagNameStrings.put(Arrays.hashCode(TAG_COLLECTIONAUTHOR),
				"collectionauthor");
		tagNameStrings.put(Arrays.hashCode(TAG_COLLECTIONAUTHORKEY),
				"collectionauthorkey");
		tagNameStrings.put(Arrays.hashCode(TAG_PUBLISHINFO), "publishinfo");
		tagNameStrings.put(Arrays.hashCode(TAG_LASTSHARED), "lastshared");
		tagNameStrings.put(Arrays.hashCode(TAG_AICHHASHSET), "aichhashset");
		tagNameStrings.put(Arrays.hashCode(TAG_KADAICHHASHPUB),
				"kadaichhashpub");
		tagNameStrings.put(Arrays.hashCode(TAG_KADAICHHASHRESULT),
				"kadaichhashresult");
		tagNameStrings.put(Arrays.hashCode(TAG_ATTRANSFERRED), "attransferred");
		tagNameStrings.put(Arrays.hashCode(TAG_ATREQUESTED), "atrequested");
		tagNameStrings.put(Arrays.hashCode(TAG_ATACCEPTED), "ataccepted");
		tagNameStrings.put(Arrays.hashCode(TAG_CATEGORY), "category");
		tagNameStrings.put(Arrays.hashCode(TAG_ATTRANSFERREDHI),
				"attransferredhi");
		tagNameStrings.put(Arrays.hashCode(TAG_MAXSOURCES), "maxsources");
		tagNameStrings.put(Arrays.hashCode(TAG_MEDIA_ARTIST), "media_artist");
		tagNameStrings.put(Arrays.hashCode(TAG_MEDIA_ALBUM), "media_album");
		tagNameStrings.put(Arrays.hashCode(TAG_MEDIA_TITLE), "media_title");
		tagNameStrings.put(Arrays.hashCode(TAG_MEDIA_LENGTH), "media_length");
		tagNameStrings.put(Arrays.hashCode(TAG_MEDIA_BITRATE), "media_bitrate");
		tagNameStrings.put(Arrays.hashCode(TAG_MEDIA_CODEC), "media_codec");
		tagNameStrings.put(Arrays.hashCode(TAG_KADMISCOPTIONS),
				"kadmiscoptions");
		tagNameStrings.put(Arrays.hashCode(TAG_ENCRYPTION), "encryption");
		tagNameStrings.put(Arrays.hashCode(TAG_USER_COUNT), "user_count");
		tagNameStrings.put(Arrays.hashCode(TAG_FILE_COUNT), "file_count");
		tagNameStrings.put(Arrays.hashCode(TAG_FILECOMMENT), "filecomment");
		tagNameStrings.put(Arrays.hashCode(TAG_FILERATING), "filerating");
		tagNameStrings.put(Arrays.hashCode(TAG_BUDDYHASH), "buddyhash");
		tagNameStrings.put(Arrays.hashCode(TAG_CLIENTLOWID), "clientlowid");
		tagNameStrings.put(Arrays.hashCode(TAG_SERVERPORT), "serverport");
		tagNameStrings.put(Arrays.hashCode(TAG_SERVERIP), "serverip");
		tagNameStrings.put(Arrays.hashCode(TAG_SOURCEUPORT), "sourceuport");
		tagNameStrings.put(Arrays.hashCode(TAG_SOURCEPORT), "sourceport");
		tagNameStrings.put(Arrays.hashCode(TAG_SOURCEIP), "sourceip");
		tagNameStrings.put(Arrays.hashCode(TAG_SOURCETYPE), "sourcetype");
		tagNameStrings.put(Arrays.hashCode(TAG_VANISH), "Vanish value");

	}

	public static String getTagNameString(byte[] tagName) {
		return tagNameStrings.get(Arrays.hashCode(tagName));
	}

	public static void main(String[] args) {
		System.out.println(getTagNameString(new byte[] { (byte) 0xFD }));
		System.out.println(getTagNameString(new byte[] { (byte) 0xFD }));
		System.out.println(new byte[] { (byte) 0xFD }.hashCode());
		System.out.println(new byte[] { (byte) 0xFA }.hashCode());
		System.out.println(new byte[] { (byte) 0xFA }.hashCode());
		System.out.println(Arrays.hashCode(new byte[] { (byte) 0xFA }));
		System.out.println(Arrays.hashCode(new byte[] { (byte) 0xFA }));
	}
}
