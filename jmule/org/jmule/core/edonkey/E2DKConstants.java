/*
 *  JMule - Java file sharing client
 *  Copyright (C) 2007-2008 JMule team ( jmule@jmule.org / http://jmule.org )
 *
 *  Any parts of this program derived from other projects, or contributed
 *  by third-party developers are copyrighted by their respective authors.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package org.jmule.core.edonkey;

import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.*;
import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.AcceptCommentVer;
import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.DataCompressionVer;
import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.ExtendedRequestsVer;
import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.MultiPacket;
import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.NoViewSharedFiles;
import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.PeerCache;
import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.SourceExchange1Ver;
import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.SupportPreview;
import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.SupportSecIdent;
import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.UDPVer;
import static org.jmule.core.edonkey.E2DKConstants.PeerFeatures.UnicodeSupport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.jmule.core.JMConstants;
//import org.jmule.core.jkad.JKadConstants;

/**
 * Created on 2007-Nov-07
 * @author binary256
 * @version $$Revision: 1.26 $$
 * Last changed by $$Author: binary255 $$ on $$Date: 2009/12/25 20:12:13 $$
 */
public class E2DKConstants {

	public final static int ClientSoftware 				    = E2DKConstants.SO_JMULE;// JMule client identification!	
	public final static int ClientVersion[]					= new int[3];
	
	public static int getSoftwareVersion() {
		return ((ClientSoftware << 24) | (ClientVersion[0] << 17) | (ClientVersion[1] << 10) | (ClientVersion[2] << 7)); 
	}
	
//	static {
//		String[] versions = JMConstants.JMULE_VERSION.split("\\.");
//		int count = 3;
//		if (versions.length < count)
//			count = versions.length;
//		for (int i = 0; i < count; i++)
//			ClientVersion[i] = Integer.parseInt(versions[i]);
//	}
	
	public final static int ServerClientSoftware 		= E2DKConstants.SO_JMULE;
	public final static int ServerSoftwareVersionMajor 	= 0x00;
	public final static int ServerSoftwareVersionMinor 	= 40;
	public final static int ServerSoftwareVersionUpdate = 0x00;
	public final static int ServerSoftwareVersion 		= ((ServerClientSoftware<<24) | (ServerSoftwareVersionMajor<<17) | (ServerSoftwareVersionMinor<<10) | (ServerSoftwareVersionUpdate<<7));
	
	public final static int SO_EMULE					= 0x00;
	public final static int SO_LMULE					= 0x02;
	public final static int SO_AMULE					= 0x03;
	public final static int SO_SHAREAZA					= 0x04;
	public final static int SO_EMULE_PLUS				= 0x05;
	public final static int SO_HYDRANODE				= 0x06;
	public final static int SO_NEW2_MLDONKEY			= 0x0A;
	public final static int SO_LPHANT					= 0x14;
	public final static int SO_NEW2_SHAREAZA			= 0x28;
	public final static int SO_EDONKEYHYBRID			= 0x32;
	public final static int SO_EDONKEY					= 0x33;
	public final static int SO_MLDONKEY					= 0x34;
	public final static int SO_OLDEMULE					= 0x35;
	public final static int SO_JMULE					= 0xAA;
	public final static int SO_NEW_MLDONKEY				= 0x98;
	public final static int SO_COMPAT_UNK				= 0xFF;
	
	public final static int ProtocolVersion 			= 60;
	
	public final static int KEY_LENGTH					= 608;
	
	public final static int SUPPORTED_FLAGS 			= E2DKConstants.CAP_ZLIB | E2DKConstants.CAP_UNICODE | E2DKConstants.CAP_LARGEFILES ;
	
	public final static List<String> SERVER_ERROR_MESSAGES 	= new LinkedList<String>();
	
	static {
		SERVER_ERROR_MESSAGES.add("WARNING : This server is full.");
		SERVER_ERROR_MESSAGES.add("ERROR : Connection refused. Your IP is currently blacklisted.");
	}
	
	public final static long PACKET_SIZE_TO_COMPRESS	= 900;
	public static final Set<Byte> PEER_PACKETS_NOT_ALLOWED_TO_COMPRESS = new HashSet<Byte>();
	static {
		PEER_PACKETS_NOT_ALLOWED_TO_COMPRESS.add(E2DKConstants.OP_HASHSETANSWER);
		PEER_PACKETS_NOT_ALLOWED_TO_COMPRESS.add(E2DKConstants.OP_PEERHELLO);
		PEER_PACKETS_NOT_ALLOWED_TO_COMPRESS.add(E2DKConstants.OP_PEERHELLOANSWER);
		PEER_PACKETS_NOT_ALLOWED_TO_COMPRESS.add(E2DKConstants.OP_SENDINGPART);
	}
	public static final Set<Byte> SERVER_PACKETS_NOT_ALLOWED_TO_COMPRESS = new HashSet<Byte>();
	
	public final static long MAXPACKETSIZE				= 2000000;
	public final static long PARTSIZE 					= 0x947000; //9728000
    public final static int  BLOCKSIZE 					= 184320;//184320 10240
    public final static int  MAX_OFFER_FILES			= 200;
	public final static byte PROTO_EDONKEY_TCP 			= (byte) 0xE3;
	public final static byte PROTO_EDONKEY_SERVER_UDP 	= (byte) 0xE3;
	public final static byte PROTO_EDONKEY_PEER_UDP 	= (byte) 0xC5;
	public final static byte PROTO_EMULE_EXTENDED_TCP 	= (byte) 0xC5;
	public final static byte PROTO_EMULE_COMPRESSED_TCP = (byte) 0xD4;
	
	public final static int SERVER_UDP_PORT 			= 4665;
	// Client->Server
	public final static byte OP_LOGINREQUEST 			= (byte) 0x01;
	public final static byte OP_GETSERVERLIST 			= (byte) 0x14;
	public final static byte OP_SEARCHREQUEST 			= (byte) 0x16;
	public final static byte OP_GETSOURCES 				= (byte) 0x19;
	public final static byte OP_OFFERFILES 				= (byte) 0x15;
	public final static byte PACKET_SRVMESSAGE 			= (byte) 0x38;
	public final static byte PACKET_SRVIDCHANGE 		= (byte) 0x40;
	public final static byte PACKET_SRVSTATUS 			= (byte) 0x34;
	public final static byte PACKET_SRVSEARCHRESULT 	= (byte) 0x33;
	public final static byte PACKET_SRVFOUNDSOURCES 	= (byte) 0x42;
	public final static byte PACKET_CALLBACKREQUEST 	= (byte) 0x1C;
	public final static byte PACKET_CALLBACKREQUESTED 	= (byte) 0x35;
	public final static byte PACKET_CALLBACKFAILED 		= (byte) 0x36;
	public final static byte OP_SERVERLISTREQUEST 		= (byte) 0x14;
	public final static byte OP_SERVERLIST 				= (byte) 0x32;
	// Client<->Client
	public final static byte OP_PEERHELLO 				= (byte) 0x01;
	public final static byte OP_PEERHELLOANSWER 		= (byte) 0x4C;
	public final static byte OP_FILEREQUEST 			= (byte) 0x58;
	public final static byte OP_FILESTATREQ 			= (byte) 0x4F;
	public final static byte OP_FILEREQANSNOFILE 		= (byte) 0x48;
	public final static byte OP_FILEREQANSWER 			= (byte) 0x59;
	public final static byte OP_SLOTREQUEST 			= (byte) 0x54;
	public final static byte OP_SLOTGIVEN 				= (byte) 0x55;
	public final static byte OP_SLOTRELEASE 			= (byte) 0x56;
	public final static byte OP_REQUESTPARTS 			= (byte) 0x47;
	public final static byte OP_SENDINGPART 			= (byte) 0x46;
	public final static byte OP_COMPRESSEDPART 			= (byte) 0x40;
	public final static byte OP_HASHSETREQUEST 			= (byte) 0x51;
	public final static byte OP_HASHSETANSWER 			= (byte) 0x52;
	public final static byte OP_FILESTATUS 				= (byte) 0x50;
	public final static byte OP_END_OF_DOWNLOAD 		= (byte) 0x49;
	public final static byte OP_SLOTTAKEN 				= (byte) 0x57;
	public final static byte OP_MESSAGE 				= (byte) 0x4E;
	// eMule extensions
	public final static byte OP_EMULE_QUEUERANKING 		= (byte) 0x60;
	public final static byte OP_EMULE_HELLO 			= (byte) 0x01;
	public final static byte OP_EMULEHELLOANSWER 		= (byte) 0x02;
	public final static byte OP_REQUESTSOURCES 			= (byte) 0x81;
	public final static byte OP_ANSWERSOURCES 			= (byte) 0x82;
	public final static byte OP_SECIDENTSTATE 			= (byte) 0x87;
	public final static byte OP_PUBLICKEY 				= (byte) 0x85;
	public final static byte OP_SIGNATURE 				= (byte) 0x86;
	
	public final static byte OP_CHATCAPTCHAREQ			= (byte) 0xA5;
	public final static byte OP_CHATCAPTCHARES			= (byte) 0xA6;
	
	public final static byte OP_KAD_CALLBACK			= (byte) 0x99;
	
	public final static byte SRV_TCPFLG_COMPRESSION		= 0x00000001;
	public final static byte SRV_TCPFLG_NEWTAGS			= 0x00000008;
	public final static byte SRV_TCPFLG_UNICODE			= 0x00000010;
	public final static byte SRV_TCPFLG_RELATEDSEARCH	= 0x00000040;
	public final static byte SRV_TCPFLG_TYPETAGINTEGER	= (byte) 0x00000080;
	public final static byte SRV_TCPFLG_LARGEFILES		= (byte) 0x00000100;
	public final static byte SRV_TCPFLG_TCPOBFUSCATION	= (byte) 0x00000400;

	public final static byte SRV_UDPFLG_EXT_GETSOURCES	= 0x00000001;
	public final static byte SRV_UDPFLG_EXT_GETFILES	= 0x00000002;
	public final static byte SRV_UDPFLG_NEWTAGS			= 0x00000008;
	public final static byte SRV_UDPFLG_UNICODE			= 0x00000010;
	public final static byte SRV_UDPFLG_EXT_GETSOURCES2	= 0x00000020;
	public final static byte SRV_UDPFLG_LARGEFILES		= (byte) 0x00000100;
	public final static byte SRV_UDPFLG_UDPOBFUSCATION	= (byte) 0x00000200;
	public final static byte SRV_UDPFLG_TCPOBFUSCATION	= (byte) 0x00000400;
	
	// Queue rating modifiers
	public final static float INITIAL_RATING			= 100f;
	public final static float BANNED 					= 0f;
	public final static float FILE_PRIORITY_RELEASE		= 1.8f;
	public final static float FILE_PRIORITY_HIGH		= 0.9f;
	public final static float FILE_PRIORITY_NORMAL		= 0.7f;
	public final static float FILE_PRIORITY_LOW			= 0.6f;
	public final static float FILE_PRIORITY_VERY_LOW	= 0.2f;
	
	public static enum ServerFeatures {
		// TCP + UDP flags
		Compression,
		NewTags,
		Unicode,
		LargeFiles,
		// Only TCP flags
		RelatedSearch,
		TypeTagInteger,
		TCPObfusication,
		// Only UDP Flags
		GetSources,
		GetFiles,
		GetSources2,
		UDPObfusication
	}
	
	public static enum PeerFeatures { 
		// Misc options 1
		UDPVer,
		DataCompressionVer,
		SupportSecIdent,
		SourceExchange1Ver,
		ExtendedRequestsVer,
		AcceptCommentVer,
		NoViewSharedFiles,
		MultiPacket,
		SupportPreview,
		PeerCache,
		UnicodeSupport,
		AICHVer,
		// Misc options 2
		DirectUDPCallback,
		SupportsCaptcha,
		SupportsSourceEx2,
		RequiresCryptLayer,
		RequestsCryptLayer,
		SupportsCryptLayer,
		Reserved, // mod
		SupportLargeFiles,
		KadVersion,
		//internal options used in JMule
		ProtocolVersion
	}
	
	public static final Map<PeerFeatures, Integer> DefaultJMuleFeatures = new HashMap<PeerFeatures, Integer> (); 
	static {
		DefaultJMuleFeatures.put(AICHVer, 0);
		DefaultJMuleFeatures.put(UnicodeSupport, 1);
		DefaultJMuleFeatures.put(UDPVer, 4);
		DefaultJMuleFeatures.put(DataCompressionVer, 1);
		DefaultJMuleFeatures.put(SupportSecIdent, 0);
		DefaultJMuleFeatures.put(SourceExchange1Ver, 1);
		DefaultJMuleFeatures.put(ExtendedRequestsVer,0);
		DefaultJMuleFeatures.put(AcceptCommentVer, 0);
		DefaultJMuleFeatures.put(PeerCache, 0);
		DefaultJMuleFeatures.put(NoViewSharedFiles, 0);
		DefaultJMuleFeatures.put(MultiPacket, 0);
		DefaultJMuleFeatures.put(SupportPreview, 0);
		
		DefaultJMuleFeatures.put(DirectUDPCallback, 0);
		DefaultJMuleFeatures.put(SupportsCaptcha, 1);
		DefaultJMuleFeatures.put(SupportsSourceEx2, 0);
		DefaultJMuleFeatures.put(RequiresCryptLayer, 0);
		DefaultJMuleFeatures.put(RequestsCryptLayer, 0);
		DefaultJMuleFeatures.put(SupportsCryptLayer, 0);
		DefaultJMuleFeatures.put(Reserved, 0);
		DefaultJMuleFeatures.put(SupportLargeFiles, 1);
//		DefaultJMuleFeatures.put(KadVersion, (int)JKadConstants.KAD_VERSION);
	}
	//UDP
	//Server <-> Peer 
	public final static byte OP_GLOBSERVRSTATREQ 		= (byte) 0x96;
	public final static byte OP_GLOBSERVSTATUS 			= (byte) 0x97;
	public final static byte OP_GLOBGETSOURCES 			= (byte) 0x9A;
	public final static byte OP_SERVER_DESC_REQ 		= (byte) 0xA2;
	public final static byte OP_SERVER_DESC_ANSWER 		= (byte) 0xA3;
	public final static byte OP_GLOBSEARCHREQ 			= (byte) 0x98;
	//Peer <-> Peer
	public final static byte OP_REASKFILEPING 			= (byte)(0x90);
	// Tag types
	public static final byte TAGTYPE_HASH				= (byte) 0x01;
	public final static byte TAGTYPE_STRING 			= (byte) 0x02;
	public final static byte TAGTYPE_UINT32 			= (byte) 0x03;
	public final static byte TAGTYPE_FLOAT32 			= (byte) 0x04;
	public final static byte TAGTYPE_BOOL	 			= (byte) 0x05;
	public final static byte TAGTYPE_BOOLARRAY	 		= (byte) 0x06;
	public final static byte TAGTYPE_BLOB		 		= (byte) 0x07;
	public static final byte TAGTYPE_UINT16				= (byte) 0x08;
	public static final byte TAGTYPE_UINT8				= (byte) 0x09;
	public static final byte TAGTYPE_BSOB				= (byte) 0x0A;
	public static final byte TAGTYPE_UINT64				= (byte) 0x0B;
	//Extended tag Types
	public final static byte TAGTYPE_EXSTRING_SHORT_BEGIN = (byte) 0x90;
	public final static byte TAGTYPE_EXSTRING_SHORT_END = (byte) (TAGTYPE_EXSTRING_SHORT_BEGIN + 15);
	public final static byte TAGTYPE_EXSTRING_LONG 		= (byte) 0x82; 
	public final static byte TAGTYPE_EXBYTE				= (byte) 0x89; 
	public final static byte TAGTYPE_EXWORD 			= (byte) 0x88;
	public final static byte TAGTYPE_EXDWORD 			= (byte) 0x83;
	// Meta tag Name
	public final static byte[] TAG_NAME_NAME 			= new byte[]{0x01};
	public final static byte[] TAG_NAME_NICKNAME		= new byte[]{0x01};
	public final static byte[] TAG_NAME_PROTOCOLVERSION = new byte[]{0x11};
	public final static byte[] TAG_NAME_CLIENTVER 		= new byte[]{(byte)0xFB};
	public final static byte[] TAG_NAME_FLAGS 			= new byte[]{0x20};
	public final static byte[] TAG_NAME_MISC_OPTIONS1 	= new byte[]{(byte)0xfa};
	public final static byte[] TAG_NAME_MISC_OPTIONS2 	= new byte[]{(byte)0xfe};
	public final static byte[] TAG_NAME_UDP_PORT 		= new byte[] {0x21};
	public final static byte[] TAG_NAME_UDP_PORT_PEER 	= new byte[] {(byte)0xF9};
	public final static byte[] TAG_NAME_SIZE 			= new byte[]{0x02};
	public final static byte[] TAG_NAME_FILE_TYPE		= new byte[]{0x03};
	public final static byte[] TAG_NAME_FORMAT		 	= new byte[]{0x04};
	public final static byte[] TAG_NAME_AVIABILITY 		= new byte[]{0x15};
	public final static byte[] TAG_NAME_COMPLETESRC 	= new byte[]{0x30};
	// eMule tags - used in OP_EMULE_HELLO, OP_EMULE_HELLOANSWER
	public final static byte[] ET_COMPRESSION 			= new byte[]{0x20};
	public final static byte[] ET_UDPPORT 				= new byte[]{0x21};
	public final static byte[] ET_UDPVER 				= new byte[]{0x22};
	public final static byte[] ET_SOURCEEXCHANGE 		= new byte[]{0x23};
	public final static byte[] ET_COMMENTS 				= new byte[]{0x24};
	public final static byte[] ET_EXTENDEDREQUEST 		= new byte[]{0x25};
	public final static byte[] ET_COMPATIBLECLIENT 		= new byte[]{0x26};//not used
	public final static byte[] ET_FEATURES 				= new byte[]{0x27};
	
	public final static byte[] FT_NAME_STATUS			= new byte[]{0x14};
	
	public final static List<byte[]> JMuleInternalTags		= new LinkedList<byte[]>();
	static {
		JMuleInternalTags.add(FT_NAME_STATUS);
	}
	
	// FLAGS Values 
	public final static byte CAP_NEWTAGS 				= (byte)0x0008;
	public final static byte CAP_LARGEFILES 			= (byte)0x0100;
	public final static byte CAP_UNICODE 				= (byte)0x0010;
	public final static byte CAP_ZLIB 					= (byte)0x0001;
		
	// Search constants
	public final static byte[] SEARCH_BY_NAME			= new byte[] {0x01};
	public final static byte[] SEARCH_BY_META			= new byte[] {0x02};
	public final static byte[] SEARCH_BY_LIMIT			= new byte[] {0x03};
	public final static byte[] LIMIT_MIN				= new byte[] {0x01};
	public final static byte[] LIMIT_MAX				= new byte[] {0x02};
	
	public final static byte[] FT_FILERATING			= new byte[]{(byte)0xF7};
	
	public final static byte FILE_QUALITY_NOTRATED			= 0x00;
	public final static byte FILE_QUALITY_FAKE				= 0x01;
	public final static byte FILE_QUALITY_POOR				= 0x02;
	public final static byte FILE_QUALITY_FAIR				= 0x03;
	public final static byte FILE_QUALITY_GOOD				= 0x04;
	public final static byte FILE_QUALITY_EXCELLENT			= 0x05;
	
	public final static int SERVER_SEARCH_RATIO 			= 255 / FILE_QUALITY_EXCELLENT;

	public final static byte[] SEARCH_AND 				= new byte[]{ 0x00, 0x00 };
	public final static byte[] SEARCH_OR 				= new byte[]{ 0x00, 0x01 };
	public final static byte[] SEARCH_NOT 				= new byte[]{ 0x00, 0x02 };
	
	public final static byte[] TAG_FILE_TYPE_AUDIO	 	= "Audio".getBytes();
	public final static byte[] TAG_FILE_TYPE_VIDEO	 	= "Video".getBytes();
	public final static byte[] TAG_FILE_TYPE_IMAGE	 	= "Image".getBytes();
	public final static byte[] TAG_FILE_TYPE_DOC	 	= "Doc".getBytes();
	public final static byte[] TAG_FILE_TYPE_PROGRAM 	= "Pro".getBytes();
	public final static byte[] TAG_FILE_TYPE_COLLECTION	= "EmuleCollection".getBytes();
	public final static byte[] TAG_FILE_TYPE_ARC	 	= "Arc".getBytes(); // internal usage
	public final static byte[] TAG_FILE_TYPE_ISO	 	= "Iso".getBytes(); // internal usage
	public final static byte[] TAG_FILE_TYPE_UNKNOWN 	= "Unknown".getBytes(); // internal usage
	
	public final static Set<String> audio_extensions	= new HashSet<String>(); 
	public final static Set<String> video_extensions	= new HashSet<String>();
	public final static Set<String> image_extensions	= new HashSet<String>();
	public final static Set<String> doc_extensions		= new HashSet<String>();
	public final static Set<String> program_extensions	= new HashSet<String>();
	public final static Set<String> archive_extensions	= new HashSet<String>();
	public final static Set<String> iso_extensions		= new HashSet<String>();
	
	static {
		audio_extensions.add("mp3");
		audio_extensions.add("mp4");
		audio_extensions.add("wav");
		audio_extensions.add("midi");
		audio_extensions.add("ogg");
		
		video_extensions.add("avi");
		video_extensions.add("mpg");
		video_extensions.add("mpeg");
		video_extensions.add("mkv");
		video_extensions.add("wmv");
		video_extensions.add("flv");
		
		image_extensions.add("png");
		image_extensions.add("gif");
		image_extensions.add("jpg");
		image_extensions.add("jpeg");
		image_extensions.add("bmp");
		image_extensions.add("xcf");
		
		doc_extensions.add("odt");
		doc_extensions.add("pdf");
		doc_extensions.add("doc");
		doc_extensions.add("docx");
		doc_extensions.add("txt");
		doc_extensions.add("wri");
		doc_extensions.add("vor");
		
		program_extensions.add("sh");
		program_extensions.add("class");
		program_extensions.add("jar");
		program_extensions.add("exe");
		program_extensions.add("com");
		program_extensions.add("bat");
		program_extensions.add("cmd");
		
		archive_extensions.add("uue");
		archive_extensions.add("bz2");
		archive_extensions.add("tar");
		archive_extensions.add("gz");
		archive_extensions.add("zip");
		archive_extensions.add("rar");
		
		iso_extensions.add("iso");
		iso_extensions.add("nrg");
		iso_extensions.add("mdf");
	}
	// Part files constants
	public final static byte PARTFILE_VERSION 			= (byte)0xe0;
	public final static byte[] FT_FILENAME				= new byte[]{0x01};
	public final static byte[] FT_TEMPFILE 				= new byte[]{0x12};
	public final static byte[] FT_FILESIZE 				= new byte[]{0x02};
	public final static byte[] FT_GAPSTART 				= new byte[]{0x09,0};
	public final static byte[] FT_GAPEND 				= new byte[] {0x0A,0};
	public final static byte GAP_OFFSET 				= 0x30; //Value hacked from eMule & aMule met.part files
	//Server list format
	public final static byte SERVERLIST_VERSION 		= (byte)0xE0;
	public final static byte[] SL_SERVERNAME 			= new byte[]{0x01};
	public final static byte[] SL_DESCRIPTION 			= new byte[]{0x0B};
	public final static byte[] SL_PING 					= new byte[] {0x0C};
	public final static byte[] SL_FAIL 					= new byte[]{0x0D};
	public final static byte[] SL_PREFERENCE 			= new byte[] {0x0E};
	public final static byte[] SL_MAXUSERS 				= new byte[] {(byte)0x87};
	public final static byte[] SL_SOFTFILES 			= new byte[] {(byte)0x88};
	public final static byte[] SL_HARDFILES 			= new byte[] {(byte)0x89};
	public final static byte[] SL_LASTPING 				= new byte[] {(byte)0x90};
	public final static byte[] SL_VERSION 				= new byte[] {(byte)0x91};
	public final static byte[] SL_UDPFLAGS 				= new byte[] {(byte)0x92};
	public final static byte[] SL_AUXILIARYPORTLIST 	= new byte[]{(byte)0x93};
	public final static byte[] SL_LOWIDCLIENTS 			= new byte[] {(byte)0x94};
	public final static byte[] SL_USERS 				= new String("users").getBytes();
	public final static byte[] SL_FILES 				= new String("files").getBytes();
	public final static byte[] SL_SRVMAXUSERS 			= new String("maxusers").getBytes();
	// Known file
	public final static byte KNOWN_VERSION 				= (byte)0x0E;
	
	// Clients.Met
	public final static byte CREDITFILE_VERSION 		= (byte) 0x12;
	public final static byte CREDITFILE_VERSION29 		= (byte) 0x11;
	
	
}
