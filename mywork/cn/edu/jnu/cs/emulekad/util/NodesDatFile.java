/**
 * 
 */
package cn.edu.jnu.cs.emulekad.util;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.Node;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.URI;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.jnu.cs.emulekad.EMuleKadModule;
import cn.edu.jnu.cs.emulekad.net.OpCodes;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import static cn.edu.jnu.cs.emulekad.util.IOUtil.*;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class NodesDatFile {

	private final Provider<KeyFactory> keyFactoryProvider;
	private final String tcpScheme;
	private final String udpScheme;
	private final String nodesFilePath;

	private final Logger logger = LoggerFactory.getLogger(NodesDatFile.class);

	@Inject
	public NodesDatFile(Provider<KeyFactory> keyFactoryProvider,
			@Named("openkad.scheme.name") String udpScheme,
			@Named("openkad.scheme2.name") String tcpScheme,
			@Named("openkad.nodes.file.path") String nodesFilePath) {

		this.keyFactoryProvider = keyFactoryProvider;
		this.tcpScheme = tcpScheme;
		this.udpScheme = udpScheme;
		this.nodesFilePath = nodesFilePath;
	}

	/*
	 * 
	 * http://wiki.amule.org/index.php/Nodes.dat_file
	 */
	public List<Node> readNodeFromFile(String fileName) {
		List<Node> $ = Collections.emptyList();
		File file=new File(fileName);
		if(!file.exists()){
			logger.info("load data from nodes.dat failed. File not exists.");
			return $;
		}
		FileChannel channel = null;
		try {
			channel = new RandomAccessFile(fileName, "r").getChannel();
			MappedByteBuffer buffer = channel.map(
					FileChannel.MapMode.READ_ONLY, 0, channel.size());
			buffer.order(ByteOrder.LITTLE_ENDIAN);

			buffer.position(4); // skip 'old' contacts count field

			buffer.getInt(); // nodes.dat version

			int totalContacts = buffer.getInt();
			$ = new ArrayList<Node>(totalContacts);
			logger.info("loading {} contacts from nodes.dat:", totalContacts);

			// Total bytes for a given contact: 16 + 4 + 2 + 2 + 1 + 8 + 1 = 34
			// bytes
			for (int i = 1; i <= totalContacts; i++) {
				byte[] keyBytes = new byte[16];
				buffer.get(keyBytes);
				Key key = keyFactoryProvider.get().get(
						reverseKeyBytes(keyBytes));
				Node n = new Node(key);

				byte[] ipBytes = new byte[4];
				buffer.get(ipBytes);
				n.setInetAddress(InetAddress.getByAddress(reverse(ipBytes)));

				int udpPort = unsignShortToInt(buffer.getShort());
				n.addEndpoint(udpScheme, udpPort);

				int tcpPort = unsignShortToInt(buffer.getShort());
				n.addEndpoint(tcpScheme, tcpPort);

				buffer.get();// byte contact_version

				byte[] kadUDPKeyBytes = new byte[8];
				buffer.get(kadUDPKeyBytes);// KadUDPKey udp_key

				buffer.get();// Verified

				$.add(n);
			}

			channel.close();
			return $;
		} catch (Throwable t) {
			t.printStackTrace();
			if ($ == null)
				$ = Collections.emptyList();
			return $;
		} finally {
			if (channel != null)
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}

	public List<Node> readNodeFromFile() {
		return readNodeFromFile(this.nodesFilePath);
	}

	public Collection<URI> readURIsFromFile() {
		List<Node> nodes = readNodeFromFile();
		Collection<URI> $ = new ArrayList<URI>();
		for (Node node : nodes) {
			$.add(node.getURI(udpScheme));
		}
		return $;
	}

	public void writeNodesToFile(String fileName, List<Node> nodeList) {
		FileChannel channel = null;
		try {
			channel = new RandomAccessFile(fileName, "rw").getChannel();
			MappedByteBuffer buffer = channel.map(
					FileChannel.MapMode.READ_WRITE, 0,
					12 + 34 * nodeList.size());
			buffer.order(ByteOrder.LITTLE_ENDIAN);

			buffer.putInt(0);
			buffer.putInt(OpCodes.NODES_DAT_VERSION);
			buffer.putInt(nodeList.size());

			for (Node node : nodeList) {
				buffer.put(cloneThenReverseKeyBytes(node.getKey().getBytes()));
				buffer.put(reverse(node.getInetAddress().getAddress()));
				buffer.putShort((short) node.getPort(udpScheme));
				buffer.putShort((short) node.getPort(tcpScheme));
				buffer.put(OpCodes.ContactType4);
				buffer.put(new byte[8]);
				buffer.put((byte) 1);
			}

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (channel != null)
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public void writeNodesToFile(List<Node> nodeList) {
		writeNodesToFile(this.nodesFilePath, nodeList);
	}

	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new EMuleKadModule());
		NodesDatFile nodesDatFile = injector.getInstance(NodesDatFile.class);
		List<Node> nodes = nodesDatFile.readNodeFromFile();
		System.out.println(nodes.size());
		for (Node node : nodes) {
			System.out.println(node);
		}
	}

}
