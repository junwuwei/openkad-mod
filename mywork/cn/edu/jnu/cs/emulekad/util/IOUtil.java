/**
 * 
 */
package cn.edu.jnu.cs.emulekad.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class IOUtil {
	public static byte[] intToOneByte(int i) {
		ByteBuffer data = ByteBuffer.allocate(4);
		data.order(ByteOrder.LITTLE_ENDIAN);
		data.putInt(i);
		byte[] oneByteArray = new byte[1];
		data.position(0);
		data.get(oneByteArray, 0, 1);
		return oneByteArray;
	}
	
	public static byte[] intToTwoBytes(int i) {
		ByteBuffer data = ByteBuffer.allocate(4);
		data.order(ByteOrder.LITTLE_ENDIAN);
		data.putInt(i);
		byte[] shortBytes = new byte[2];
		data.position(0);
		data.get(shortBytes, 0, 2);
		return shortBytes;
	}
	
	public static byte[] longToBytes(long l) {
		ByteBuffer data = ByteBuffer.allocate(8);
		data.order(ByteOrder.LITTLE_ENDIAN);
		data.putLong(l);
		data.position(0);
		return data.array();
	}

	public static int readTwoBytesAsInt(InputStream in) throws IOException {
		byte[] bytes = new byte[4];
		in.read(bytes, 0, 2);
		bytes[2] = 0x00;
		bytes[3] = 0x00;
		ByteBuffer data = ByteBuffer.wrap(bytes);
		data.order(ByteOrder.LITTLE_ENDIAN);
		return data.getInt();
	}
	
	public static long readLong(InputStream in) throws IOException {
		byte[] bytes = new byte[8];
		in.read(bytes, 0, 8);
		ByteBuffer data = ByteBuffer.wrap(bytes);
		data.order(ByteOrder.LITTLE_ENDIAN);
		return data.getLong();
	}

	public static byte readOneByte(InputStream in) throws IOException {
		byte[] bytes = new byte[1];
		in.read(bytes, 0, 1);
		return bytes[0];
	}


	public static void writeOneByte(OutputStream out, byte b)
			throws IOException {
		byte[] bytes = new byte[] { b };
		out.write(bytes);
	}

	public static byte[] reverse(byte[] bytes, int offset, int length,boolean clone) {
		byte[] $;
		if(clone){
			$=bytes.clone();
		}else{
			$=bytes;
		}
		byte temp;
		for (int i = offset, j = offset + length - 1; i < j; i++, j--) {
			temp = $[i];
			$[i] = $[j];
			$[j] = temp;
		}
		return $;
	}

	public static byte[] reverse(byte[] bytes) {
		return reverse(bytes, 0, bytes.length,false);
	}
	
	public static byte[] cloneThenReverse(byte[] bytes) {
		return reverse(bytes, 0, bytes.length,true);
	}

	public static byte[] cloneThenReverseKeyBytes(byte[] keyBytes) {
		byte[] $=keyBytes.clone();
		for (int i = 0; i < $.length - 1; i += 4) {
			reverse($, i, 4,false);
		}
		return $;
	}
	
	public static byte[] reverseKeyBytes(byte[] keyBytes) {
		for (int i = 0; i < keyBytes.length - 1; i += 4) {
			reverse(keyBytes, i, 4,false);
		}
		return keyBytes;
	}
	
	

	public static int unsignShortToInt(short s){
		ByteBuffer buffer=ByteBuffer.allocate(4);
		buffer.putShort((short) 0);
		buffer.putShort(s);
		buffer.position(0);
		return buffer.getInt();
	}
	
	public static int unsignByteToInt(byte b){
		ByteBuffer buffer=ByteBuffer.allocate(4);
		buffer.putShort((short) 0);
		buffer.put((byte) 0);
		buffer.put(b);
		buffer.position(0);
		return buffer.getInt();
	}
	
//	public static short intToUnsignShort(int i){
//		ByteBuffer buffer=ByteBuffer.allocate(4);
//		buffer.putInt(i);
//		buffer.position(2);
//		return buffer.getShort();
//	}
	
	public static String toHexString(byte[] bytes){
		StringBuilder strBuilder=new StringBuilder();
		for(int i= 0; i< bytes.length; i++){
			strBuilder.append(String.format("%02X",bytes[i]));
			if((i+1)%4==0) strBuilder.append(" ");
		}
		return strBuilder.toString();	
	}
	
	public static byte[] hexStringToByteArray(String hexString) throws DecoderException{
		Hex hex=new Hex();
		return hex.decode(hexString.getBytes());
	}
	
	public static int ipBytesToInt(byte[] ipBytes){
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.put(ipBytes);
		buffer.position(0);
		return buffer.getInt();
	}
	
	public static void main(String[] args) throws DecoderException {
		ByteBuffer buffer = ByteBuffer.allocate(10);
		System.out.println(toHexString(buffer.array()));
		buffer.flip();
		System.out.println(buffer.array().length);
		
		byte[] a=new byte[]{10,20,30,40,50,60,70,80,10,20,30,40,50,60,70,80};
		byte[] b=new byte[]{40,30,20,10,80,70,60,50,40,30,20,10,80,70,60,50};
		byte[] c=reverseKeyBytes(b);
		byte[] d=b.clone();
//		byte[] d=reverse(b);
		System.out.println(a+Arrays.toString(a));
		System.out.println(b+Arrays.toString(b));
		System.out.println(c+Arrays.toString(c));
		System.out.println(d+Arrays.toString(d));
		
		System.out.println("\n0C2891295454BCBD1240F00E40D0EA1D");
		byte[] bytes=hexStringToByteArray("0C2891295454BCBD1240F00E40D0EA1D");
		System.out.println(toHexString(bytes));
		
		
	}
}
