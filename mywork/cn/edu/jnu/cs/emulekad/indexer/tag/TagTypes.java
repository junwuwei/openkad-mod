/**
 * 
 */
package cn.edu.jnu.cs.emulekad.indexer.tag;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class TagTypes {
	
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
	
	// public static byte TAGTYPE_STR1 = 0x11;
	// public static byte TAGTYPE_STR2 = 0x12;
	// public static byte TAGTYPE_STR3 = 0x13;
	// public static byte TAGTYPE_STR4 = 0x14;
	// public static byte TAGTYPE_STR5 = 0x15;
	// public static byte TAGTYPE_STR6 = 0x16;
	// public static byte TAGTYPE_STR7 = 0x17;
	// public static byte TAGTYPE_STR8 = 0x18;
	// public static byte TAGTYPE_STR9 = 0x19;
	// public static byte TAGTYPE_STR10 = 0x1A;
	// public static byte TAGTYPE_STR11 = 0x1B;
	// public static byte TAGTYPE_STR12 = 0x1C;
	// public static byte TAGTYPE_STR13 = 0x1D;
	// public static byte TAGTYPE_STR14 = 0x1E;
	// public static byte TAGTYPE_STR15 = 0x1F;
	// public static byte TAGTYPE_STR16 = 0x20;
	// public static byte TAGTYPE_STR17 = 0x21;
	// public static byte TAGTYPE_STR18 = 0x22;
	// public static byte TAGTYPE_STR19 = 0x23;
	// public static byte TAGTYPE_STR20 = 0x24;
	// public static byte TAGTYPE_STR21 = 0x25;
	
	//Extended tag Types
	public final static byte TAGTYPE_EXSTRING_SHORT_BEGIN = (byte) 0x90;
	public final static byte TAGTYPE_EXSTRING_SHORT_END = (byte) (TAGTYPE_EXSTRING_SHORT_BEGIN + 15);
	public final static byte TAGTYPE_EXSTRING_LONG 		= (byte) 0x82; 
	public final static byte TAGTYPE_EXBYTE				= (byte) 0x89; 
	public final static byte TAGTYPE_EXWORD 			= (byte) 0x88;
	public final static byte TAGTYPE_EXDWORD 			= (byte) 0x83;
	
	private static Map<Byte,String> tagTypeStrings=new HashMap<Byte,String>();
	static{
		tagTypeStrings.put(TAGTYPE_HASH			,"Hash");
		tagTypeStrings.put(TAGTYPE_STRING 		,"String");
		tagTypeStrings.put(TAGTYPE_UINT32 		,"Int32");
		tagTypeStrings.put(TAGTYPE_FLOAT32 		,"Float32");
		tagTypeStrings.put(TAGTYPE_BOOL	 		,"Boolean");
		tagTypeStrings.put(TAGTYPE_BOOLARRAY	,"BooleanArray");
		tagTypeStrings.put(TAGTYPE_BLOB		 	,"Blob");
		tagTypeStrings.put(TAGTYPE_UINT16		,"Short");
		tagTypeStrings.put(TAGTYPE_UINT8		,"Byte");
		tagTypeStrings.put(TAGTYPE_BSOB			,"Bsob");
		tagTypeStrings.put(TAGTYPE_UINT64		,"Long");
	}
	
	public static String getTagTypeString(byte tagType){
		return tagTypeStrings.get(tagType);
	}

}
