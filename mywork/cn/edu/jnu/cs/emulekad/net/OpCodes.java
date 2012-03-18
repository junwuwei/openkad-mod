/**
 * 
 */
package cn.edu.jnu.cs.emulekad.net;


/**
 * opcodes for eMule kademlia network
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class OpCodes {
	public static final byte NODES_DAT_1					= 0x01;
	public static final byte NODES_DAT_2					= 0x02;
	public static final byte NODES_DAT_VERSION				= NODES_DAT_2;
	
//	public static final String NODES_DAT					= SETTINGS_DIR + File.separator + "nodes.dat";
//	
//	public static final byte SRC_INDEX_VERSION				= 0x09;
//	
//	public static final String KEY_INDEX_DAT				= SETTINGS_DIR + File.separator + "key_index.dat";
//	public static final String SRC_INDEX_DAT				= SETTINGS_DIR + File.separator + "src_index.dat";
//	public static final String NOTE_INDEX_DAT				= SETTINGS_DIR + File.separator + "note_index.dat";
//	
//	public static final int MAX_UDP_PACKET					= 65534;
//	public static final int MIN_UNPACKET_SIZE				= 100;
//	public static final int FIREWALL_CHECK_INTERVAL			= 1000 * 60 * 60;
//	public static final int FIREWALLED_STATUS_CHANGE_INTERVAL = 1000 * 5;
//	public static final int FIREWALL_CHECK_CONTACTS			= 20;
//	
//	public static final byte KAD_VERSION 					= 0x05; // KADEMLIA_VERSION5_48a	0x05 // -0.48a
//
//	public static final int MIN_CONTACTS_TO_SEND_BOOTSTRAP	= 150; 
//	public static final int BOOTSTRAP_CONTACTS				= 20;
//	public static final int BOOTSTRAP_STOP_CONTACTS			= 50;
//	public static final int BOOTSTRAP_CHECK_INTERVAL		= 5000;
//	public static final int BOOTSTRAP_REMOVE_TIME			= 10000;// 60000
//	
//	public static final int SEARCH_CONTACTS					= 2;
//	public static final int INITIAL_SEARCH_CONTACT_COUNT	= 50;
//	public static final int PUBLISH_KEYWORD_CONTACT_COUNT	= 20;
//	
//	public static final long SEARCH_KEYWORD_TIMEOUT			= 1000 * 45;
//	public static final long SEARCH_NOTES_TIMEOUT			= 1000 * 45;
//	public static final long SEARCH_SOURCES_TIMEOUT			= 1000 * 45;
//	
//	public static final int  INITIAL_LOOKUP_CONTACTS 		= 50;
//	public static final long LOOKUP_TASK_CHECK_INTERVAL		= 5000;
//	public static final long LOOKUP_TASK_DEFAULT_TIMEOUT	= 15000;
//	
//	public static final long CONCURRENT_LOOKUP_COUNT		= 50;
//	public static final long LOOKUP_CONTACT_CHECK_INTERVAL	= 5000;
//	public static final long LOOKUP_CONTACT_TIMEOUT			= 11000;
//	public static final long MAX_LOOKUP_RUNNING_TIME		= 1000 * 60 * 5;
//	
//	public static final int LOOKUP_NODE_CONTACTS			= 11;
//	public static final int LOOKUP_VALUE_CONTACTS			= 2;
//	public static final int LOOKUP_STORE_CONTACTS			= 4;
//	
//	public static final int MAX_PUBLISH_SOURCES				= 150; // keyword and sources
//	public static final int MAX_PUBLISH_NOTES				= 200; 
//	
//	public static final int  MAX_CONTACTS 							= 5000;
//	public static final long ROUTING_TABLE_HELLO_SEND_INTERVAL		= 1000 * 60 * 2; //60
//	public static final long ROUTING_TABLE_FAKE_LOOKUP_INTERVAL		= 1000 * 60 * 3; //60
//	public static final int  CONTACTS_SEND_HELLO					= 5;
//	public static final long ROUTING_TABLE_CHECK_INTERVAL			= 1000 * 30; //30
//	public static final long ROUTING_TABLE_CONTACTS_CHECK_INTERVAL	= 1000 * 60; 
//	public static final long ROUTING_TABLE_CONTACT_TIMEOUT  		= 1000 * 60 * 2;
//	public static final long ROUTING_TABLE_CONTACT_ACCEP_TIME 		= 1000 * 60 * 2 - 1;
//	public static final long ROUTING_TABLE_CONTACT_IGNORE_TIME 		= 1000 * 60 * 2; 
//	public static final long ROUTING_TABLE_SAVE_INTERVAL			= 1000 * 60;
//	public static final long ROUTING_TABLE_DIFICIT_CONTACTS 		= 200;//200
//	public static final long ROUTING_TABLE_DIFICIT_CONTACTS_STOP = ROUTING_TABLE_DIFICIT_CONTACTS + 100;
//	
//	public static final int ROUTING_TABLE_MAINTENANCE_CONTACTS			= 10; //3
//	public static final int ROUTING_TABLE_MAX_MAINTENANCE_CONTACTS		= 50;
//	
//	public static final int INDEX_MAX_KEYWORDS				= 60000;
//	public static final int INDEX_MAX_SOURCES				= 60000;
//	public static final int INDEX_MAX_NOTES					= 60000;
//	public static final int INDEXER_SAVE_DATA_INTERVAL		= 1000 * 60 * 2;
//	public static final int INDEXER_CLEAN_DATA_INTERVAL		= 1000 * 60 ;
//	
//	public static final long TIME_24_HOURS 					=  1000 * 60 * 60 * 24;
//	
//	public static final long DEFAULT_PACKET_LISTENER_TIMEOUT = 5000;
//	
//	public static final long PUBLISHER_MAINTENANCE_INTERVAL	 = 1000 * 6;
//	
//	public static final long PUBLISHER_NOTE_PUBLISH_TIMEOUT  = 1000 * 100;
//	public static final long PUBLISHER_KEYWORD_PUBLISH_TIMEOUT  = 1000 * 140;
//	public static final long PUBLISHER_SOURCE_PUBLISH_TIMEOUT = 1000 * 140;
//	
//	public static final long INDEXTER_MAX_LOAD_TO_NOT_PUBLISH = 60;
//	
//	public static final int KAD_SOURCES_SEARCH_INTERVAL     =  1000 * 60 * 5;
//	
//	public static final long PUBLISHER_PUBLISH_CHECK_INTERVAL	= 5000;
//	
//	public static final long ITERATION_MAX_PUBLISH_FILES 	= 3;
//	public static final long MAX_CONCURRENT_PUBLISH_FILES 	= 3;
//	
//	public static final int K 								= 10;//10
//	public static final int ALPHA 							= 3;//3;
//
//	public static final long toleranceZone 					= 16777216;
	
	
	public static final byte PROTO_KAD_UDP 					= (byte) 0xE4;
	public static final byte PROTO_KAD_COMPRESSED_UDP		= (byte) 0xE5;
//	
//	public static final byte KADEMLIA_BOOTSTRAP_REQ			= (byte) 0x00;
//	public static final byte KADEMLIA_BOOTSTRAP_RES			= (byte) 0x08;
//	
//	public static final byte KADEMLIA_HELLO_REQ				= (byte) 0x10;
//	public static final byte KADEMLIA_HELLO_RES				= (byte) 0x18;
//	
//	public static final byte KADEMLIA_FIREWALLED_REQ		= (byte) 0x50;
//	public static final byte KADEMLIA_FIREWALLED_RES		= (byte) 0x58;
//	
//	public static final byte KADEMLIA_CALLBACK_REQ			= (byte) 0x52;
//	
//	public static final byte KADEMLIA_REQ					= (byte) 0x20;
//	public static final byte KADEMLIA_RES					= (byte) 0x28;
//
//	public static final byte KADEMLIA_PUBLISH_REQ			= (byte) 0x40;
//	public static final byte KADEMLIA_PUBLISH_RES			= (byte) 0x48;
//	
//	public static final byte KADEMLIA_SEARCH_REQ			= (byte) 0x30;
//	public static final byte KADEMLIA_SEARCH_RES			= (byte) 0x38;
//	
//	public static final byte KADEMLIA_SEARCH_NOTES_REQ		= (byte) 0x32;
//	public static final byte KADEMLIA_SEARCH_NOTES_RES		= (byte) 0x3A;
//	
//	public static final byte KADEMLIA_FINDBUDDY_REQ			= (byte) 0x51;
//	public static final byte KADEMLIA_FINDBUDDY_RES			= (byte) 0x5A;
//	
//	public static final byte KADEMLIA_PUBLISH_NOTES_REQ		= (byte) 0x42;
//	public static final byte KADEMLIA_PUBLISH_NOTES_RES		= (byte) 0x4A; 
	
	public static final byte KADEMLIA2_BOOTSTRAP_REQ		= (byte) 0x01;
	public static final byte KADEMLIA2_BOOTSTRAP_RES		= (byte) 0x09;

	public static final byte KADEMLIA2_REQ					= (byte) 0x21;
	public static final byte KADEMLIA2_RES					= (byte) 0x29;
	
	public static final byte KADEMLIA2_HELLO_REQ			= (byte) 0x11;
	public static final byte KADEMLIA2_HELLO_RES 			= (byte) 0x19;

//	public static final byte KADEMLIA2_HELLO_RES_ACK		= (byte) 0x22;
//	
//	public static final byte KADEMLIA_FIREWALLED2_REQ       = (byte) 0x53;
//	
//	public static final byte KADEMLIA2_FIREWALLUDP			= (byte) 0x62;
//	
	public static final byte KADEMLIA2_SEARCH_KEY_REQ		= (byte) 0x33;
	public static final byte KADEMLIA2_SEARCH_SOURCE_REQ	= (byte) 0x34;
	public static final byte KADEMLIA2_SEARCH_NOTES_REQ		= (byte) 0x35;
	
	public static final byte KADEMLIA2_SEARCH_RES			= (byte) 0x3B;
	
	public static final byte KADEMLIA2_PUBLISH_KEY_REQ		= (byte) 0x43;
	public static final byte KADEMLIA2_PUBLISH_SOURCE_REQ	= (byte) 0x44;
	public static final byte KADEMLIA2_PUBLISH_NOTES_REQ	= (byte) 0x45;
	
	public static final byte KADEMLIA2_PUBLISH_RES			= (byte) 0x4B;

//	public static final byte KADEMLIA2_PUBLISH_RES_ACK		= (byte) 0x4C;
	
//	public static final byte KADEMLIA2_PING					= (byte) 0x60;
//	public static final byte KADEMLIA2_PONG					= (byte) 0x61;
	
	public static final byte FIND_VALUE 					= (byte) 0x02;
	public static final byte STORE      					= (byte) 0x04;
	public static final byte FIND_NODE						= (byte) 0x0B;
	
//	public static final byte ContactType0					= (byte) 0x00;
//	public static final byte ContactType1					= (byte) 0x01;
//	public static final byte ContactType2					= (byte) 0x02;
//	public static final byte ContactType3					= (byte) 0x03;
//	public static final byte ContactType4					= (byte) 0x04;



	//file tags
	// #define FT_FILENAME 0x01 // <string>
	//
	// #define FT_FILESIZE 0x02 // <uint32> (or <uint64> when supported)
	//
	// #define FT_FILESIZE_HI 0x3A // <uint32>
	//
	// #define FT_FILETYPE 0x03 // <string>
	//
	// #define FT_FILEFORMAT 0x04 // <string>
	//
	// #define FT_LASTSEENCOMPLETE 0x05 // <uint32>
	//
	// #define TAG_PART_PATH "\x06" // <string>
	// #define TAG_PART_HASH "\x07"
	// #define FT_TRANSFERRED 0x08 // <uint32>
	// #define TAG_TRANSFERRED "\x08" // <uint32>
	// #define FT_GAPSTART 0x09 // <uint32>
	// #define TAG_GAPSTART "\x09" // <uint32>
	// #define FT_GAPEND 0x0A // <uint32>
	// #define TAG_GAPEND "\x0A" // <uint32>
	// #define FT_DESCRIPTION 0x0B // <string>
	// #define TAG_DESCRIPTION "\x0B" // <string>
	// #define TAG_PING "\x0C"
	// #define TAG_FAIL "\x0D"
	// #define TAG_PREFERENCE "\x0E"
	//
	//
	//
	// #define FT_PARTFILENAME 0x12 // <string>
	//
	// //#define FT_PRIORITY 0x13 // Not used anymore
	//
	// #define FT_STATUS 0x14 // <uint32>
	//
	// #define FT_SOURCES 0x15 // <uint32>
	//
	// #define FT_PERMISSIONS 0x16 // <uint32>
	//
	// //#define FT_ULPRIORITY 0x17 // Not used anymore
	//
	// #define FT_DLPRIORITY 0x18 // Was 13
	// #define FT_ULPRIORITY 0x19 // Was 17
	// #define FT_COMPRESSION 0x1A
	// #define FT_CORRUPTED 0x1B
	// #define FT_KADLASTPUBLISHKEY 0x20 // <uint32>
	// #define FT_KADLASTPUBLISHSRC 0x21 // <uint32>
	// #define FT_FLAGS 0x22 // <uint32>
	// #define FT_DL_ACTIVE_TIME 0x23 // <uint32>
	// #define FT_CORRUPTEDPARTS 0x24 // <string>
	// #define FT_DL_PREVIEW 0x25
	// #define FT_KADLASTPUBLISHNOTES 0x26 // <uint32>
	// #define FT_AICH_HASH 0x27
	// #define FT_FILEHASH 0x28
	// #define FT_COMPLETE_SOURCES 0x30 // nr. of sources which share a complete
	// version of the associated file (supported by eserver 16.46+)
	//
	// #define FT_COLLECTIONAUTHOR 0x31
	// #define FT_COLLECTIONAUTHORKEY 0x32
	// #define FT_PUBLISHINFO 0x33 // <uint32>
	//
	// #define FT_LASTSHARED 0x34 // <uint32>
	// #define FT_AICHHASHSET 0x35 // <uint32>
	// #define TAG_KADAICHHASHPUB "\x36" // <AICH Hash>
	

	
//	public static final byte[] TAG_SOURCETYPE				=  new byte[] { (byte)0xFF };
//	
//	public static final byte[] TAG_FILENAME					=  new byte[] { (byte)0x01 };
//	public static final byte[] TAG_FILESIZE					=  new byte[] { (byte)0x02 };
//	public static final byte[] TAG_SOURCECOUNT				=  new byte[] { (byte)0x15 };
//	public static final byte[] TAG_FILERATING				=  new byte[] { (byte)0xF7 };
//	public static final byte[] TAG_FILEDESCRIPTION			=  new byte[] { (byte)0x0B };
//	
//	public static final byte[] TAG_SOURCEPORT				=  new byte[] { (byte)0xFD };
//	public static final byte[] TAG_SOURCEUPORT				=  new byte[] { (byte)0xFC };
//	public static final byte[] TAG_SOURCEIP					=  new byte[] { (byte)0xFE };
//	
//	public static final byte[] TAG_SERVERIP					=  new byte[] { (byte)0xFB };
//	public static final byte[] TAG_SERVERPORT				=  new byte[] { (byte)0xFA };
	
	public static final byte[] ContactType0					=  new byte[]{(byte) 0x00};
	public static final byte[] ContactType1					=  new byte[]{(byte) 0x01};
	public static final byte[] ContactType2					=  new byte[]{(byte) 0x02};
	public static final byte[] ContactType3					=  new byte[]{(byte) 0x03};
	public static final byte[] ContactType4					=  new byte[]{(byte) 0x04};
}
