package cn.edu.jnu.cs.emulekad.net;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.msg.PingRequest;
import il.technion.ewolf.kbr.openkad.msg.PingResponse;
import il.technion.ewolf.kbr.openkad.net.KadSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.jmule.core.edonkey.packet.tag.Tag;
import org.jmule.core.edonkey.packet.tag.TagList;
import org.jmule.core.edonkey.packet.tag.TagScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.msg.BootstrapRequest;
import cn.edu.jnu.cs.emulekad.msg.BootstrapResponse;
import cn.edu.jnu.cs.emulekad.msg.EMuleKadRequest;
import cn.edu.jnu.cs.emulekad.msg.EMuleKadResponse;
import cn.edu.jnu.cs.emulekad.msg.PublishRequest;
import cn.edu.jnu.cs.emulekad.msg.PublishResponse;
import cn.edu.jnu.cs.emulekad.msg.SearchRequest;
import cn.edu.jnu.cs.emulekad.msg.SearchResponse;
import cn.edu.jnu.cs.emulekad.msg.PublishAndSearchType;
import cn.edu.jnu.cs.emulekad.msg.UnknownMessage;
import static cn.edu.jnu.cs.emulekad.util.IOUtil.*;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class EMuleKadSerializer extends KadSerializer {

	// dependencies
	private final String udpScheme;
	private final String tcpScheme;
	private final int keySize;
	private final Provider<KeyFactory> keyFactoryProvider;
	private final Key zeroKey;
	private final Node localNode;

	private static Logger logger = LoggerFactory
			.getLogger(EMuleKadSerializer.class);

	@Inject
	public EMuleKadSerializer(@Named("openkad.scheme.name") String udpScheme,
			@Named("openkad.scheme2.name") String tcpScheme,
			@Named("openkad.keyfactory.keysize") int keySize,
			@Named("openkad.keys.zerokey") Key zeroKey,
			@Named("openkad.local.node") Node localNode,
			Provider<KeyFactory> keyFactoryProvider) {
		super();
		this.udpScheme = udpScheme;
		this.tcpScheme = tcpScheme;
		this.keySize = keySize;
		this.zeroKey = zeroKey;
		this.localNode = localNode;
		this.keyFactoryProvider = keyFactoryProvider;
	}

	@Override
	public KadMessage read(InputStream in) throws IOException,
			ClassCastException, ClassNotFoundException {
		// ReadableByteChannel channel=Channels.newChannel(in);
		// channel.read(dst);
		logger.debug("receive packet size={}", in.available());
		KadMessage msg = null;
		byte prototol = readOneByte(in);
		byte packetOpCode = readOneByte(in);
		if (prototol == OpCodes.PROTO_KAD_COMPRESSED_UDP) {
			logger.debug("receive compressed kad packet.");
			logger.debug("compress content size={}", in.available());
			byte[] compressContent = new byte[in.available()];
			in.read(compressContent);

			Inflater decompressor = new Inflater();
			decompressor.setInput(compressContent);
			byte[] deCompressContent = new byte[compressContent.length * 2];
			try {
				int nrDeCompress = decompressor.inflate(deCompressContent);
				decompressor.end();
				logger.debug("nrDeCompress={}", nrDeCompress);
				ByteArrayInputStream bin = new ByteArrayInputStream(
						deCompressContent, 0, nrDeCompress);
				in = bin;
			} catch (DataFormatException e) {
				e.printStackTrace();
			}
			prototol = OpCodes.PROTO_KAD_UDP;
		}

		if (prototol == OpCodes.PROTO_KAD_UDP) {
			if (packetOpCode == OpCodes.KADEMLIA2_HELLO_RES) {
				logger.debug("deserializing PingResponse");
				Node src = readNode(in);
				msg = new PingResponse(System.currentTimeMillis(), src);
				readTagList(in);
				return msg;

			} else if (packetOpCode == OpCodes.KADEMLIA2_HELLO_REQ) {
				logger.debug("deserializing PingRequest");
				Node src = readNode(in);
				msg = new PingRequest(System.currentTimeMillis(), src);
				return msg;

			} else if (packetOpCode == OpCodes.KADEMLIA2_BOOTSTRAP_RES) {
				logger.debug("deserializing BootStrapResponse");
				Node src = readNode(in);
				logger.debug("{}", src);
				msg = new BootstrapResponse(System.currentTimeMillis(), src);
				List<Node> nodes = new ArrayList<Node>();
				int nrNode = readTwoBytesAsInt(in);
				for (int i = 0; i < nrNode; i++) {
					nodes.add(readNode2(in));
				}
				((BootstrapResponse) msg).setNodes(nodes);
				return msg;

			} else if (packetOpCode == OpCodes.KADEMLIA2_BOOTSTRAP_REQ) {
				logger.debug("deserializing BootStrapRequest");
				msg = new BootstrapRequest(System.currentTimeMillis(),
						new Node(zeroKey));
				return msg;
			} else if (packetOpCode == OpCodes.KADEMLIA2_RES) {
				logger.debug("deserializing EMuleKadResponse");
				Key targetKey = readKey(in);
				logger.debug("target key={}", targetKey);
				EMuleKadResponse res = new EMuleKadResponse(
						System.currentTimeMillis(), new Node(zeroKey),
						targetKey);
				int nrNode = readOneByte(in);
				logger.debug("nrNode={}", nrNode);
				List<Node> nodes = new ArrayList<Node>();
				for (int i = 0; i < nrNode; i++) {
					Node n = readNode2(in);
					logger.debug("{}", n);
					nodes.add(n);
					// nodes.add(readNode2(in));
				}
				res.setNodes(nodes);
				return res;

			} else if (packetOpCode == OpCodes.KADEMLIA2_REQ) {
				logger.debug("deserializing EMuleKadRequest");
				byte requestType = readOneByte(in);
				Key targetID = readKey(in);
				Key recipientKey = readKey(in);
				Node recipient = new Node(recipientKey);
				EMuleKadRequest req = new EMuleKadRequest(
						System.currentTimeMillis(), new Node(zeroKey));
				req.setRequestType(requestType).setKey(targetID)
						.setRecipient(recipient);
				return req;

			} else if (packetOpCode == OpCodes.KADEMLIA2_SEARCH_RES) {
				logger.debug("deserializing SearchResponse");
				Key clientKey = readKey(in);
				Key targetKey = readKey(in);
				logger.debug("clientKey={}", clientKey);
				logger.debug("targetKey={}", targetKey);

				int nrEntry = readTwoBytesAsInt(in);
				logger.debug("nrEntry={}", nrEntry);
				List<Entry> entries = readEntries(in, nrEntry);
				SearchResponse res = new SearchResponse(
						System.currentTimeMillis(), new Node(clientKey));
				res.setTargetKey(targetKey);
				res.setEntries(entries);
				return res;

			} else if (packetOpCode == OpCodes.KADEMLIA2_SEARCH_KEY_REQ) {
				System.out
						.println("deserializing SearchRequest,search keywork");
				Key targetKey = readKey(in);
				SearchRequest req = new SearchRequest(
						System.currentTimeMillis(), new Node(zeroKey));
				req.setTargetKey(targetKey);
				req.setSearchType(PublishAndSearchType.KEYWORD);
				return req;

			} else if (packetOpCode == OpCodes.KADEMLIA2_SEARCH_NOTES_REQ) {
				logger.debug("deserializing SearchRequest,search note");
				Key targetKey = readKey(in);
				long fileSize = readLong(in);
				SearchRequest req = new SearchRequest(
						System.currentTimeMillis(), new Node(zeroKey));
				req.setTargetKey(targetKey);
				req.setSearchType(PublishAndSearchType.NOTE);
				req.setFileSize(fileSize);
				return req;

			} else if (packetOpCode == OpCodes.KADEMLIA2_SEARCH_SOURCE_REQ) {
				logger.debug("deserializing SearchRequest,search source");
				Key targetKey = readKey(in);
				int startPosition = readTwoBytesAsInt(in);
				long fileSize = readLong(in);
				SearchRequest req = new SearchRequest(
						System.currentTimeMillis(), new Node(zeroKey));
				req.setTargetKey(targetKey)
						.setSearchType(PublishAndSearchType.SOURCE)
						.setFileSize(fileSize).setStartPosition(startPosition);
				return req;

			} else if (packetOpCode == OpCodes.KADEMLIA2_PUBLISH_RES) {
				logger.debug("deserializing PublishResponse");
				Key targetKey = readKey(in);
				int load = readOneByte(in);
				PublishResponse res = new PublishResponse(
						System.currentTimeMillis(), new Node(zeroKey));
				logger.debug("targetKey={}", targetKey);
				res.setTargetKey(targetKey).setLoad(load);
				logger.debug("load={}", load);
				return res;

			} else if (packetOpCode == OpCodes.KADEMLIA2_PUBLISH_SOURCE_REQ) {
				logger.debug("deserializing PublishRequest,public source");
				Key targetKey = readKey(in);
				List<Entry> entries = readEntries(in, 1);
				PublishRequest req = new PublishRequest(
						System.currentTimeMillis(), new Node(entries.get(0)
								.getClientKey()));
				req.setTargetKey(targetKey)
						.setPublishType(PublishAndSearchType.SOURCE)
						.setEntries(entries);
				return req;

			} else if (packetOpCode == OpCodes.KADEMLIA2_PUBLISH_KEY_REQ) {
				System.out
						.println("deserializing PublishRequest,public keyword");
				Key targetKey = readKey(in);
				logger.debug("targetKey={}", targetKey);
				int nrEntry = readTwoBytesAsInt(in);
				logger.debug("nrEntry={}", nrEntry);
				List<Entry> entries = readEntries(in, nrEntry);
				PublishRequest req = new PublishRequest(
						System.currentTimeMillis(), new Node(entries.get(0)
								.getClientKey()));
				req.setTargetKey(targetKey)
						.setPublishType(PublishAndSearchType.KEYWORD)
						.setEntries(entries);
				return req;

			} else if (packetOpCode == OpCodes.KADEMLIA2_PUBLISH_NOTES_REQ) {
				logger.debug("deserializing PublishRequest,public note");
				Key targetKey = readKey(in);
				List<Entry> entries = readEntries(in, 1);
				PublishRequest req = new PublishRequest(
						System.currentTimeMillis(), new Node(entries.get(0)
								.getClientKey()));
				req.setTargetKey(targetKey)
						.setPublishType(PublishAndSearchType.NOTE)
						.setEntries(entries);
				return req;

			} else {
				logger.debug("received eMule kad message, opcode is: "
						+ String.format("%#04x", packetOpCode));
				in.skip(in.available());
			}

		} else {
			logger.debug("received unknow message, prototol code is: "
					+ String.format("%#04x", prototol));
			in.skip(in.available());
		}
		msg = new UnknownMessage(System.currentTimeMillis(), new Node());
		return msg;
	}

	@Override
	public void write(KadMessage msg, OutputStream out) throws IOException {
		// Channels.newChannel(out).
		writeOneByte(out, OpCodes.PROTO_KAD_UDP);
		if (msg instanceof PingRequest) {
			logger.debug("serializing PingRequest.");
			writeOneByte(out, OpCodes.KADEMLIA2_HELLO_REQ);
			writeNode(out, msg.getSrc());
			writeOneByte(out, (byte) 0x00); // Tags size
			return;

		} else if (msg instanceof PingResponse) {
			logger.debug("serializing PingResponse.");
			writeOneByte(out, OpCodes.KADEMLIA2_HELLO_RES);
			writeNode(out, msg.getSrc());
			writeOneByte(out, (byte) 0x00); // Tags size
			return;

		} else if (msg instanceof BootstrapRequest) {
			logger.debug("serializing BootStrapRequest.");
			writeOneByte(out, OpCodes.KADEMLIA2_BOOTSTRAP_REQ);
			return;

		} else if (msg instanceof BootstrapResponse) {
			logger.debug("serializing BootStrapResponse.");
			writeOneByte(out, OpCodes.KADEMLIA2_BOOTSTRAP_RES);
			writeNode(out, msg.getSrc());
			List<Node> nodes = ((BootstrapResponse) msg).getNodes();
			out.write(intToTwoBytes(nodes.size()));
			for (Node node : nodes) {
				writeNode2(out, node);
			}
			return;

		} else if (msg instanceof EMuleKadRequest) {
			logger.debug("serializing EMuleKadRequest.");
			EMuleKadRequest req = (EMuleKadRequest) msg;
			writeOneByte(out, OpCodes.KADEMLIA2_REQ);
			writeOneByte(out, req.getRequestType());
			out.write(cloneThenReverseKeyBytes(req.getKey().getBytes())); // Target
																			// Key
			logger.debug("target key={}", req.getKey());
			out.write(cloneThenReverseKeyBytes(req.getRecipient().getKey()
					.getBytes())); // Recipient key
			return;

		} else if (msg instanceof EMuleKadResponse) {
			logger.debug("serializing EMuleKadResponse.");
			EMuleKadResponse res = (EMuleKadResponse) msg;
			writeOneByte(out, OpCodes.KADEMLIA2_RES);
			out.write(cloneThenReverseKeyBytes(res.getKey().getBytes())); // Target
																			// ID
			out.write(intToOneByte(res.getNodes().size()));
			for (Node n : res.getNodes()) {
				writeNode2(out, n);
			}
			return;

		} else if (msg instanceof SearchRequest) {
			logger.debug("serializing SearchRequest.");
			SearchRequest req = (SearchRequest) msg;
			switch (req.getSearchType()) {
			case KEYWORD:
				writeOneByte(out, OpCodes.KADEMLIA2_SEARCH_KEY_REQ);
				writeKey(out, req.getTargetKey());
				writeOneByte(out, (byte) 0x00);
				writeOneByte(out, (byte) 0x00);
				break;
			case SOURCE:
				writeOneByte(out, OpCodes.KADEMLIA2_SEARCH_SOURCE_REQ);
				writeKey(out, req.getTargetKey());
				out.write(intToTwoBytes(req.getStartPosition()));
				out.write(longToBytes(req.getFileSize()));
				break;
			case NOTE:
				writeOneByte(out, OpCodes.KADEMLIA2_SEARCH_NOTES_REQ);
				writeKey(out, req.getTargetKey());
				out.write(longToBytes(req.getFileSize()));
				break;
			}
			return;

		} else if (msg instanceof SearchResponse) {
			logger.debug("serializing SearchResponse.");
			SearchResponse res = (SearchResponse) msg;
			writeOneByte(out, OpCodes.KADEMLIA2_SEARCH_RES);
			writeKey(out, localNode.getKey());
			writeKey(out, res.getTargetKey());
			out.write(intToTwoBytes(res.getEntries().size()));
			writeEntries(out, res.getEntries());
			return;

		} else if (msg instanceof PublishRequest) {
			logger.debug("serializing PublishRequest.");
			PublishRequest request = (PublishRequest) msg;
			switch (request.getPublishType()) {
			case SOURCE:
				writeOneByte(out, OpCodes.KADEMLIA2_PUBLISH_SOURCE_REQ);
				writeKey(out, request.getTargetKey());
				writeEntries(out, request.getEntries(), 1);
				break;
			case KEYWORD:
				writeOneByte(out, OpCodes.KADEMLIA2_PUBLISH_KEY_REQ);
				writeKey(out, request.getTargetKey());
				int nrEntry = request.getEntries().size();
				out.write(intToTwoBytes(nrEntry));
				writeEntries(out, request.getEntries());
				break;
			case NOTE:
				writeOneByte(out, OpCodes.KADEMLIA2_PUBLISH_NOTES_REQ);
				writeKey(out, request.getTargetKey());
				writeEntries(out, request.getEntries(), 1);
				break;
			}
			return;

		} else if (msg instanceof PublishResponse) {
			logger.debug("serializing PublishResponse.");
			PublishResponse response = (PublishResponse) msg;
			writeOneByte(out, OpCodes.KADEMLIA2_PUBLISH_RES);
			writeKey(out, response.getTargetKey());
			out.write(intToOneByte(response.getLoad()));
			return;

		} else {
			logger.debug("serializing unknown message.");
		}

	}

	private Key readKey(InputStream in) throws IOException {
		byte[] keyBytes = new byte[keySize];
		in.read(keyBytes, 0, keySize);
		Key key = keyFactoryProvider.get().get(reverseKeyBytes(keyBytes));
		return key;
	}

	private void writeKey(OutputStream out, Key key) throws IOException {
		out.write(cloneThenReverseKeyBytes(key.getBytes()));
	}

	private TagList readTagList(InputStream in) throws IOException {
		int nrTag = unsignByteToInt(readOneByte(in));
		logger.debug("nrTag={}", nrTag);
		byte[] tagContentByte = new byte[in.available()];
		ByteBuffer tagContentBuffer = ByteBuffer.wrap(tagContentByte).order(
				ByteOrder.LITTLE_ENDIAN);
		TagList tagList = new TagList();
		for (int i = 0; i < nrTag; i++) {
			Tag tag = TagScanner.scanTag(tagContentBuffer);
			if (tag == null)
				continue;
			tagList.addTag(tag);
			logger.debug("{}", tag);
		}
		return tagList;
	}

	private void writeTagList(OutputStream out, TagList tagList)
			throws IOException {
		out.write(intToOneByte(tagList.size()));
		for (Tag tag : tagList) {
			out.write(tag.getAsByteBuffer().array());
		}
	}

	private List<Entry> readEntries(InputStream in, int nrEntry)
			throws IOException {
		byte[] entryContentBytes = new byte[in.available()];
		in.read(entryContentBytes);
		ByteBuffer entryContentBuffer = ByteBuffer.wrap(entryContentBytes)
				.order(ByteOrder.LITTLE_ENDIAN);
		List<Entry> entries = new ArrayList<Entry>(nrEntry);
		for (int i = 0; i < nrEntry; i++) {
			byte[] keyBytes = new byte[keySize];
			entryContentBuffer.get(keyBytes);
			Key clientKey = keyFactoryProvider.get().get(
					reverseKeyBytes(keyBytes));
			logger.debug("clientKey={}", clientKey);
			int nrTag = unsignByteToInt(entryContentBuffer.get());
			logger.debug("nrTag={}", nrTag);
			TagList tagList = new TagList();
			for (int j = 0; j < nrTag; j++) {
				Tag tag = TagScanner.scanTag(entryContentBuffer);
				if (tag == null)
					continue;
				tagList.addTag(tag);
			}
			Entry entry = new Entry(clientKey, tagList);
			entries.add(entry);
			logger.debug("{}", entry);
		}

		return entries;
	}

	private void writeEntries(OutputStream out, List<Entry> entries, int nrEntry)
			throws IOException {
		assert (nrEntry <= entries.size());
		for (int i = 0; i < nrEntry; i++) {
			Entry entry = entries.get(i);
			writeKey(out, entry.getClientKey());
			writeTagList(out, entry.getTagList());
		}
	}

	private void writeEntries(OutputStream out, List<Entry> entries)
			throws IOException {
		writeEntries(out, entries, entries.size());
	}

	private Node readNode(InputStream in) throws IOException {
		Key key = readKey(in);
		Node src = new Node(key);
		int tcpPort = readTwoBytesAsInt(in);
		src.addEndpoint(tcpScheme, tcpPort);
		readOneByte(in);// skip kad contact version
		return src;
	}

	private Node readNode2(InputStream in) throws IOException {
		Key key = readKey(in);
		Node src = new Node(key);

		byte[] ipBytes = new byte[4];
		in.read(ipBytes, 0, 4);
		reverse(ipBytes);
		src.setInetAddress(InetAddress.getByAddress(ipBytes));

		int udpPort = readTwoBytesAsInt(in);
		src.addEndpoint(udpScheme, udpPort);
		int tcpPort = readTwoBytesAsInt(in);
		src.addEndpoint(tcpScheme, tcpPort);
		readOneByte(in);// skip kad contact version
		return src;
	}

	private void writeNode(OutputStream out, Node node) throws IOException {
		out.write(cloneThenReverseKeyBytes(node.getKey().getBytes()));
		out.write(intToTwoBytes(node.getPort(tcpScheme)));
		out.write(OpCodes.ContactType4);
		// Kademlia protocol version: 0' means a Kad v1 node, any value > 0
		// means a Kad v2 node and determines what kind of packets can be sent
		// to a node, what features it supports, etc.
	}

	private void writeNode2(OutputStream out, Node node) throws IOException {
		out.write(cloneThenReverseKeyBytes(node.getKey().getBytes()));
		out.write(cloneThenReverse(node.getInetAddress().getAddress()));
		out.write(intToTwoBytes(node.getPort(udpScheme)));
		out.write(intToTwoBytes(node.getPort(tcpScheme)));
		out.write(OpCodes.ContactType4);
	}

	public static void main(String[] args) {
		ByteBuffer buffer = ByteBuffer.allocate(10);
		buffer.flip();
		System.out.println(buffer.array().length);

		byte[] a = new byte[] { 10, 20, 30, 40, 50, 60, 70, 80, 10, 20, 30, 40,
				50, 60, 70, 80 };
		byte[] b = new byte[] { 40, 30, 20, 10, 80, 70, 60, 50, 40, 30, 20, 10,
				80, 70, 60, 50 };
		byte[] c = cloneThenReverseKeyBytes(b);
		byte[] d = reverse(b);
		System.out.println(a + Arrays.toString(a));
		System.out.println(b + Arrays.toString(b));
		System.out.println(c + Arrays.toString(c));
		System.out.println(d + Arrays.toString(d));
	}
}
