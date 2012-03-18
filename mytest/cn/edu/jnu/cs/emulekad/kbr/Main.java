/**
 * 
 */
package cn.edu.jnu.cs.emulekad.kbr;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.KeybasedRouting;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;

import cn.edu.jnu.cs.emulekad.EMuleKadModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 *
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class Main {

	public static void main(String[] args) throws URISyntaxException, IOException {
//		int basePort = 4662;
		Injector injector = Guice.createInjector(new EMuleKadModule()
//				.setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) + "")
						);
		
		KeyFactory keyFactory=injector.getInstance(KeyFactory.class);
		Key key=keyFactory.create(Base64.encodeBase64String("Ã“Ω„".getBytes()));
		Key key2=keyFactory.create("Ã“Ω„");

		System.out.println(key.toHexString());
		System.out.println(key2.toHexString());
		String keyHexString="DB409AFB 30FA9F9C 62948125 B763C498";
		System.out.println(keyHexString);
//		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
//		System.out.println("local node key:"
//				+ kbr.getLocalNode().getKey().toHexString());
//		System.out.println("local ip:"
//				+ kbr.getLocalNode().getInetAddress());
//		kbr.create();
//		kbr.joinURI(Arrays.asList(new URI("openkad.udp://219.222.19.30:"
//				+ basePort + "/")));
//		System.out.println("finished joining");
//		kbr.shutdown();
	}
}
