package cn.edu.jnu.cs.emulekad.kbr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.KeybasedRouting;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.net.KadServer;

import org.apache.commons.codec.DecoderException;
import org.jmule.core.edonkey.FileHash;
import org.jmule.core.edonkey.packet.tag.BSOBTag;
import org.jmule.core.edonkey.packet.tag.ByteTag;
import org.jmule.core.edonkey.packet.tag.HashTag;
import org.jmule.core.edonkey.packet.tag.IntTag;
import org.jmule.core.edonkey.packet.tag.LongTag;
import org.jmule.core.edonkey.packet.tag.ShortTag;
import org.jmule.core.edonkey.packet.tag.StringTag;
import org.jmule.core.edonkey.packet.tag.Tag;
import org.jmule.core.edonkey.packet.tag.TagList;
import org.junit.Test;

import cn.edu.jnu.cs.emulekad.EMuleKadModule;
import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.indexer.tag.TagNames;
import cn.edu.jnu.cs.emulekad.msg.EMuleKadRequest;
import cn.edu.jnu.cs.emulekad.msg.PublishAndSearchType;
import cn.edu.jnu.cs.emulekad.net.OpCodes;
import cn.edu.jnu.cs.emulekad.op.PublishOperation;
import cn.edu.jnu.cs.emulekad.op.SearchOperation;
import cn.edu.jnu.cs.emulekad.util.IOUtil;
import cn.edu.jnu.cs.emulekad.util.NodesDatFile;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class EMuleKadTest {

	private void joinNetwordByBootstrapFromKnownEMuleClient(KeybasedRouting kbr)
			throws URISyntaxException {
		int basePort = 4662;
		// String ip = "219.222.19.30";
		 String ip = "219.222.19.140";
//		String ip = "219.222.18.234";
//		String ip = "127.0.0.1";
		long stratTime = System.currentTimeMillis();
		kbr.joinURI(Arrays.asList(new URI("openkad.udp://" + ip + ":"
				+ basePort + "/")));
		long endTime = System.currentTimeMillis();
		System.out.println("finished joining, used "
				+ TimeUnit.MILLISECONDS.toSeconds(endTime - stratTime)
				+ " seconds.");
	}

	private void joinNetwordByLoadNodeDatFile(KeybasedRouting kbr,
			Injector injector) {
		NodesDatFile nodesDatFile = injector.getInstance(NodesDatFile.class);
		long stratTime = System.currentTimeMillis();
		kbr.joinNode(nodesDatFile.readNodeFromFile());
		long endTime = System.currentTimeMillis();
		System.out.println("finished joining, used "
				+ TimeUnit.MILLISECONDS.toSeconds(endTime - stratTime)
				+ " seconds.");
	}

	private Node provideOneEmuleNode(KeyFactory keyFactory)
			throws UnknownHostException {
		int basePort = 4662;
		Node n = new Node(keyFactory.get("hOg6zsNZO9+qNYD/bR0xtw=="));
		n.setInetAddress(InetAddress.getLocalHost());
		n.addEndpoint("emulekad.udp", basePort);
		return n;
	}

	@Test
	public void TwoNodesBootstrapTest() throws IOException, URISyntaxException {
		int basePort = 10000;
		List<KeybasedRouting> kbrs = new ArrayList<KeybasedRouting>();
		// KadServer server = null;
		for (int i = 0; i < 2; ++i) {
			Injector injector = Guice.createInjector(new EMuleKadModule()
					.setProperty("openkad.seed", "" + (i + basePort))
					.setProperty("openkad.net.udp.port", "" + (i + basePort))
					.setProperty("openkad.need_bootstrap.minsize", "20")
					.setProperty("openkad.refresh.enable", false + "")
					.setProperty("openkad.bootstrap.do_rendom_findnode",
							false + "")
					.setProperty("openkad.bootstrap.ping_befor_insert",
							false + ""));
	
			KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
			kbr.create();
			kbrs.add(kbr);
			// server = injector.getInstance(KadServer.class);
		}
	
		kbrs.get(1).joinURI(
				Arrays.asList(new URI("emulekad.udp://127.0.0.1:" + basePort
						+ "/")));
		System.out.println("finished joining");
		assertTrue(kbrs.get(0).getNeighbours().isEmpty());
		assertTrue(kbrs.get(1).getNeighbours().size() == 1);
	}

	@Test
	public void TwoNodesPingTest() throws Throwable {
		int basePort = 10000;
		List<KeybasedRouting> kbrs = new ArrayList<KeybasedRouting>();
		// KadServer server = null;
		for (int i = 0; i < 2; ++i) {
			Injector injector = Guice.createInjector(new EMuleKadModule()
					// .setProperty("openkad.keyfactory.keysize", "1")
					// .setProperty("openkad.bucket.kbuckets.maxsize", "3")
					// .setProperty("openkad.net.timeout",
					// TimeUnit.MINUTES.toMillis(10) + "")
					.setProperty("openkad.seed", "" + (i + basePort))
					.setProperty("openkad.net.udp.port", "" + (i + basePort))
					.setProperty("openkad.need_bootstrap.minsize", "0")
					.setProperty("openkad.refresh.enable", false + "")
					.setProperty("openkad.bootstrap.do_rendom_findnode",
							false + "")
					.setProperty("openkad.bootstrap.ping_befor_insert",
							true + ""));
	
			KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
			kbr.create();
			kbrs.add(kbr);
			// server = injector.getInstance(KadServer.class);
		}
	
		kbrs.get(1).joinURI(
				Arrays.asList(new URI("emulekad.udp://127.0.0.1:" + basePort
						+ "/")));
		System.out.println("finished joining");
		assertEquals(kbrs.get(0).getNeighbours().get(0), kbrs.get(1)
				.getLocalNode());
		assertEquals(kbrs.get(1).getNeighbours().get(0), kbrs.get(0)
				.getLocalNode());
	}

	@Test
	public void nodesDatFileReadWriteTest() {
		Injector injector = Guice.createInjector(new EMuleKadModule());
		NodesDatFile nodesFile = injector.getInstance(NodesDatFile.class);
		List<Node> nodes = nodesFile.readNodeFromFile();
		nodes = nodes.subList(nodes.size() / 2, nodes.size());
		nodesFile.writeNodesToFile("nodes.bat.backup", nodes);
		List<Node> nodes2 = nodesFile.readNodeFromFile("nodes.bat.backup");
		assertTrue(nodes2.size() == nodes.size());
	}

	@Test
	public void readNodesDatFileAndJoinTest() throws IOException {
		Injector injector = Guice
				.createInjector(new EMuleKadModule()
						.setProperty("openkad.need_bootstrap.minsize", "20")
						.setProperty("openkad.refresh.enable", false + "")
						.setProperty("openkad.bootstrap.do_rendom_findnode",
								false + "")
						.setProperty("openkad.bootstrap.ping_befor_insert",
								true + "")
				// .setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10)
				// +
				// "")
				);
		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
		System.out.println("local node key:"
				+ kbr.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + kbr.getLocalNode().getInetAddress());
		kbr.create();

		joinNetwordByLoadNodeDatFile(kbr, injector);

		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");
		kbr.shutdown();
	}

	@Test
	public void joinNetworkTest() throws Throwable {
		Injector injector = Guice
				.createInjector(new EMuleKadModule()
						.setProperty("openkad.need_bootstrap.minsize", "0")
						.setProperty("openkad.refresh.enable", false + "")
						.setProperty("openkad.bootstrap.do_rendom_findnode",
								false + "")
						.setProperty("openkad.bootstrap.ping_befor_insert",
								true + "")
				// .setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10)
				// +
				// "")
				);
		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
		System.out.println("local node key:"
				+ kbr.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + kbr.getLocalNode().getInetAddress());
		kbr.create();

		joinNetwordByBootstrapFromKnownEMuleClient(kbr);
		// joinNetwordByLoadNodeDatFile(kbr,injector);

		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");

		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");
		kbr.shutdown();
		// TimeUnit.MINUTES.sleep(2);
	}

	@Test
	public void the2NodesShouldFindEachOther() throws Throwable {
		int basePort = 10000;
		List<KeybasedRouting> kbrs = new ArrayList<KeybasedRouting>();
		for (int i = 0; i < 2; ++i) {
			Injector injector = Guice.createInjector(new EMuleKadModule()
					// .setProperty("openkad.keyfactory.keysize", "1")
					// .setProperty("openkad.bucket.kbuckets.maxsize", "3")
					.setProperty("openkad.net.timeout",
							TimeUnit.MINUTES.toMillis(10) + "")
					.setProperty("openkad.seed", "" + (i + basePort))
					.setProperty("openkad.net.udp.port", "" + (i + basePort))
					.setProperty("openkad.refresh.enable", false + "")
					.setProperty("openkad.bootstrap.do_rendom_findnode",
							false + "")
					.setProperty("openkad.need_bootstrap.minsize", "0")
					.setProperty("openkad.bootstrap.ping_befor_insert",
							true + ""));
	
			KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
			kbr.create();
			kbrs.add(kbr);
		}
	
		kbrs.get(1).joinURI(
				Arrays.asList(new URI("emulekad.udp://127.0.0.1:" + basePort
						+ "/")));
		System.out.println("finished joining");
	
		for (int i = 0; i < kbrs.size(); ++i) {
			System.out.println(kbrs.get(i));
			System.out.println("======");
		}
	
		List<Node> findNode = kbrs.get(1).findNode(
				kbrs.get(0).getLocalNode().getKey());
		System.out.println("findeNode=" + findNode);
		Assert.assertEquals(kbrs.get(0).getLocalNode(), findNode.get(0));
		Assert.assertEquals(kbrs.get(1).getLocalNode(), findNode.get(1));
	
		findNode = kbrs.get(0).findNode(kbrs.get(0).getLocalNode().getKey());
		Assert.assertEquals(kbrs.get(0).getLocalNode(), findNode.get(0));
		Assert.assertEquals(kbrs.get(1).getLocalNode(), findNode.get(1));
		// System.out.println(findNode);
	
		findNode = kbrs.get(0).findNode(kbrs.get(1).getLocalNode().getKey());
		System.out.println("findeNode=" + findNode);
		Assert.assertEquals(kbrs.get(1).getLocalNode(), findNode.get(0));
		Assert.assertEquals(kbrs.get(0).getLocalNode(), findNode.get(1));
	
		findNode = kbrs.get(1).findNode(kbrs.get(1).getLocalNode().getKey());
		Assert.assertEquals(kbrs.get(1).getLocalNode(), findNode.get(0));
		Assert.assertEquals(kbrs.get(0).getLocalNode(), findNode.get(1));
		kbrs.get(0).shutdown();
		kbrs.get(1).shutdown();
	
	}

	@Test
	public void findNodeTest() throws IOException, URISyntaxException {
		Injector injector = Guice.createInjector(new EMuleKadModule()
			.setProperty("openkad.refresh.enable", false + "")
			.setProperty("openkad.bootstrap.do_rendom_findnode", false + "")
		// .setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) +
		// "")
				);
		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
		System.out.println("local node key:"
				+ kbr.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + kbr.getLocalNode().getInetAddress());
		kbr.create();
		System.out.println("local node key:"
				+ kbr.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + kbr.getLocalNode().getInetAddress());

//		joinNetwordByBootstrapFromKnownEMuleClient(kbr);
		 joinNetwordByLoadNodeDatFile(kbr,injector);

		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");
		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
		Node to = provideOneEmuleNode(keyFactory);

		EMuleKadRequest res = new EMuleKadRequest(System.currentTimeMillis(),
				kbr.getLocalNode());
		res.setKey(kbr.getLocalNode().getKey()).setRecipient(to)
				.setRequestType(OpCodes.STORE);
//		.setRequestType(OpCodes.FIND_NODE);
//		.setRequestType(OpCodes.FIND_VALUE);
		KadServer server = injector.getInstance(KadServer.class);
		server.send(to, res);
		kbr.shutdown();
	}

	@Test
	public void twoNodePublishAndSearchTest() throws URISyntaxException, IOException, DecoderException{
		int basePort = 10000;
		List<KeybasedRouting> kbrs = new ArrayList<KeybasedRouting>();
		Injector injector=null;
		for (int i = 0; i < 2; ++i) {
			injector = Guice.createInjector(new EMuleKadModule()
					// .setProperty("openkad.keyfactory.keysize", "1")
					// .setProperty("openkad.bucket.kbuckets.maxsize", "3")
					.setProperty("openkad.net.timeout",
							TimeUnit.MINUTES.toMillis(10) + "")
					.setProperty("openkad.seed", "" + (i + basePort))
					.setProperty("openkad.net.udp.port", "" + (i + basePort))
					.setProperty("openkad.refresh.enable", false + "")
					.setProperty("openkad.bootstrap.do_rendom_findnode",
							false + "")
					.setProperty("openkad.need_bootstrap.minsize", "0")
					.setProperty("openkad.bootstrap.ping_befor_insert",
							true + ""));
	
			KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
			kbr.create();
			kbrs.add(kbr);
		}
	
		kbrs.get(1).joinURI(
				Arrays.asList(new URI("emulekad.udp://127.0.0.1:" + basePort
						+ "/")));
		System.out.println("finished joining");
	
	
		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
	
		Key targetKey = keyFactory.get(IOUtil
				.hexStringToByteArray("0C2891295454BCBD1240F00E40D0EA1D"));
		PublishOperation op = injector.getInstance(PublishOperation.class);
	
		TagList tagList = new TagList();
		Tag fileSize = new LongTag(TagNames.TAG_FILESIZE, 734197760L);
		Tag ip = new IntTag(TagNames.TAG_SOURCEIP,
				IOUtil.ipBytesToInt(InetAddress.getLocalHost().getAddress()));
		Tag sourceType=new ByteTag(TagNames.TAG_SOURCETYPE,(byte)1);
		Tag tcpPort=new ShortTag(TagNames.TAG_SOURCEPORT,(short)10086);
		Tag udpPort=new ShortTag(TagNames.TAG_SOURCEUPORT,(short)10086);
		Tag encryption=new ByteTag(TagNames.TAG_ENCRYPTION,(byte)1);
		tagList.addTag(fileSize);
		tagList.addTag(ip);
		tagList.addTag(sourceType);
		tagList.addTag(tcpPort);
		tagList.addTag(udpPort);
		tagList.addTag(encryption);
		System.out.println(tagList);
		
		Entry entry=new Entry(kbrs.get(1).getLocalNode().getKey(),tagList);
		op.setPublishType(PublishAndSearchType.SOURCE).setTargetKey(targetKey)
				.addEntry(entry).doPublish();
		
		SearchOperation searchOp = injector.getInstance(SearchOperation.class);
		
		searchOp.setSearchType(PublishAndSearchType.SOURCE).setFileSize(734197760L)
				.setStartPosition(0).setTargetKey(targetKey);
		searchOp.doSearch();
		kbrs.get(0).shutdown();
		kbrs.get(1).shutdown();
		
	}
	
	@Test
	public void joinNetworkAndPublishSourceTest() throws Throwable {
		Injector injector = Guice.createInjector(new EMuleKadModule()
		// .setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) +"")
				.setProperty("openkad.refresh.enable", false + "").setProperty(
						"openkad.bootstrap.do_rendom_findnode", false + ""));
		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
		System.out.println("local node key:"
				+ kbr.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + kbr.getLocalNode().getInetAddress());
	
		kbr.create();
		// joinNetwordByBootstrapFromKnownEMuleClient(kbr);
		joinNetwordByLoadNodeDatFile(kbr, injector);
	
		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");
	
		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
	
		Key targetKey = keyFactory.get(IOUtil
				.hexStringToByteArray("0C2891295454BCBD1240F00E40D0EA1D"));
		PublishOperation op = injector.getInstance(PublishOperation.class);
	
		TagList tagList = new TagList();
		Tag fileSize = new LongTag(TagNames.TAG_FILESIZE, 734197760L);
		Tag ip = new IntTag(TagNames.TAG_SOURCEIP,
				IOUtil.ipBytesToInt(InetAddress.getLocalHost().getAddress()));
		Tag sourceType=new ByteTag(TagNames.TAG_SOURCETYPE,(byte)1);
		Tag tcpPort=new ShortTag(TagNames.TAG_SOURCEPORT,(short)10086);
		Tag udpPort=new ShortTag(TagNames.TAG_SOURCEUPORT,(short)10086);
		Tag encryption=new ByteTag(TagNames.TAG_ENCRYPTION,(byte)1);
		tagList.addTag(fileSize);
		tagList.addTag(ip);
		tagList.addTag(sourceType);
		tagList.addTag(tcpPort);
		tagList.addTag(udpPort);
		tagList.addTag(encryption);
		System.out.println(tagList);
		
		Entry entry=new Entry(kbr.getLocalNode().getKey(),tagList);
		op.setPublishType(PublishAndSearchType.SOURCE).setTargetKey(targetKey)
				.addEntry(entry).doPublish();
	
//		System.out.println("kBuckets has " + kbr.getNeighbours().size()
//				+ " nodes.");
//		kbr.shutdown();
//	}
//
//	@Test
//		public void joinNetworkAndSearchSourceTest() throws Throwable {
//			Injector injector = Guice.createInjector(new EMuleKadModule()
//			// .setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) +"")
//					.setProperty("openkad.refresh.enable", false + "").setProperty(
//							"openkad.bootstrap.do_rendom_findnode", false + ""));
//			KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
//			System.out.println("local node key:"
//					+ kbr.getLocalNode().getKey().toHexString());
//			System.out.println("local ip:" + kbr.getLocalNode().getInetAddress());
//			kbr.create();
//	//		joinNetwordByBootstrapFromKnownEMuleClient(kbr);
//			 joinNetwordByLoadNodeDatFile(kbr,injector);
//	
//			System.out.println("kBuckets has " + kbr.getNeighbours().size()
//					+ " nodes.");
//	
//			KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
//			Key targetKey = keyFactory.get(IOUtil
//					.hexStringToByteArray("0C2891295454BCBD1240F00E40D0EA1D"));
			SearchOperation searchOp = injector.getInstance(SearchOperation.class);
	
			searchOp.setSearchType(PublishAndSearchType.SOURCE).setFileSize(734197760L)
					.setStartPosition(0).setTargetKey(targetKey);
			searchOp.doSearch();
	
			System.out.println("kBuckets has " + kbr.getNeighbours().size()
					+ " nodes.");
			kbr.shutdown();
		}

	@Test
	public void joinNetworkAndPublishKeywordTest() throws Throwable {
		Injector injector = Guice.createInjector(new EMuleKadModule()
		// .setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) +"")
				.setProperty("openkad.refresh.enable", false + "").setProperty(
						"openkad.bootstrap.do_rendom_findnode", false + ""));
		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
		System.out.println("local node key:"
				+ kbr.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + kbr.getLocalNode().getInetAddress());
	
		kbr.create();
//		joinNetwordByBootstrapFromKnownEMuleClient(kbr);
		 joinNetwordByLoadNodeDatFile(kbr,injector);
	
		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");
	
		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
	
		Key targetKey = keyFactory.create("Îè¹íÊ®Æß");
		PublishOperation op = injector.getInstance(PublishOperation.class);
	
		TagList tagList = new TagList();
		Tag fileName =new StringTag(TagNames.TAG_FILENAME,"ÌÒ½ã-ÁõµÂ»ª¡¢Ò¶µÂæµ.rmvb");
		Tag fileSize = new LongTag(TagNames.TAG_FILESIZE, 70122160);
		Tag sources = new ByteTag(TagNames.TAG_SOURCES, (byte)50);
		Tag fileType = new StringTag(TagNames.TAG_FILETYPE, "Video");
		Tag mediaAritist = new StringTag(TagNames.TAG_MEDIA_ARTIST, "éLÆé›öÃÀ");
		Tag mediaLength= new ShortTag(TagNames.TAG_MEDIA_LENGTH,(short)1241);
		Tag mediaBitrate= new ShortTag(TagNames.TAG_MEDIA_BITRATE,(short)389);
		Tag mediaCodec= new StringTag(TagNames.TAG_MEDIA_CODEC,"rv40");
		Tag publishInfo = new IntTag(TagNames.TAG_PUBLISHINFO,18032180 ); 
//		byte[] fileHashBytes=IOUtil.hexStringToByteArray("0DC398BFBEEC4877340E17C344E46C83");
//		FileHash file=new FileHash(fileHashBytes);
//		Tag fileHash= new HashTag(TagNames.TAG_FILEHASH,file);
		byte[] hash=IOUtil.hexStringToByteArray("16011AC5524F7F90E34DA1CFA13D27A3179831448FC1A2");
		ByteBuffer buffer=ByteBuffer.wrap(hash);
		Tag kadAichHashResult = new BSOBTag(TagNames.TAG_KADAICHHASHRESULT,buffer);
	
		tagList.addTag(fileName);
		tagList.addTag(fileSize);
		tagList.addTag(sources);
		tagList.addTag(fileType);
		tagList.addTag(mediaAritist);
		tagList.addTag(mediaLength);
		tagList.addTag(mediaBitrate);
		tagList.addTag(mediaCodec);
		tagList.addTag(publishInfo);
//		tagList.addTag(fileHash);
		tagList.addTag(kadAichHashResult);
		System.out.println(tagList);
		
		Entry entry=new Entry(kbr.getLocalNode().getKey(),tagList);
		op.setPublishType(PublishAndSearchType.KEYWORD).setTargetKey(targetKey)
				.addEntry(entry).doPublish();
	
		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");
		kbr.shutdown();
	}

	@Test
	public void joinNetworkAndSearchKeywordTest() throws Throwable {
		Injector injector = Guice.createInjector(new EMuleKadModule()
		// .setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) +"")
				.setProperty("openkad.refresh.enable", false + "").setProperty(
						"openkad.bootstrap.do_rendom_findnode", false + ""));

		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
		System.out.println("local node key:"
				+ kbr.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + kbr.getLocalNode().getInetAddress());
		kbr.create();
		// joinNetwordByBootstrapFromKnownEMuleClient(kbr);
		joinNetwordByLoadNodeDatFile(kbr, injector);

		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");

		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
		Key targetKey = keyFactory.create("Îè¹íÊ®Æß");
		SearchOperation op = injector.getInstance(SearchOperation.class);

		op.setSearchType(PublishAndSearchType.KEYWORD).setTargetKey(targetKey)
				.doSearch();

		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");
		kbr.shutdown();
	}

	@Test
		public void joinNetworkAndPublishNoteTest() throws Throwable {
		Injector injector = Guice.createInjector(new EMuleKadModule()
		// .setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) +"")
				.setProperty("openkad.refresh.enable", false + "").setProperty(
						"openkad.bootstrap.do_rendom_findnode", false + ""));
		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
		System.out.println("local node key:"
				+ kbr.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + kbr.getLocalNode().getInetAddress());
	
		kbr.create();
		// joinNetwordByBootstrapFromKnownEMuleClient(kbr);
		joinNetwordByLoadNodeDatFile(kbr, injector);
	
		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");
	
		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
	
//		Key targetKey = keyFactory.get(IOUtil
//				.hexStringToByteArray("26B9D0C7022D120640631EA7DE37670C"));
		Key targetKey = keyFactory.create("³ÂÆ¤");
		PublishOperation op = injector.getInstance(PublishOperation.class);
	
		TagList tagList = new TagList();
//		Tag fileName =new StringTag(TagNames.TAG_FILENAME,"PoD-divx-hz-cd2.avi");
//		Tag fileSize = new LongTag(TagNames.TAG_FILESIZE, 734816256L);
//		Tag fileRating=new ByteTag(TagNames.TAG_FILERATING,(byte)5);
		Tag description =new StringTag(TagNames.TAG_DESCRIPTION,"ºÃ³Ô£¬ºÃ³Ô£¬ÕæºÃ³Ô£¡");
		
//		tagList.addTag(fileName);
//		tagList.addTag(fileSize);
//		tagList.addTag(fileRating);
		tagList.addTag(description);
		System.out.println(tagList);
		
		
		Entry entry=new Entry(kbr.getLocalNode().getKey(),tagList);
		op.setPublishType(PublishAndSearchType.NOTE).setTargetKey(targetKey)
				.addEntry(entry).doPublish();
	
		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");
		kbr.shutdown();
		}

	@Test
	public void joinNetworkAndSearchNodeTest() throws Throwable {
		Injector injector = Guice.createInjector(new EMuleKadModule()
		// .setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) +"")
				.setProperty("openkad.refresh.enable", false + "").setProperty(
						"openkad.bootstrap.do_rendom_findnode", false + ""));
		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
		System.out.println("local node key:"
				+ kbr.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + kbr.getLocalNode().getInetAddress());

		kbr.create();
		joinNetwordByBootstrapFromKnownEMuleClient(kbr);
		// joinNetwordByLoadNodeDatFile(kbr,injector);

		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");

		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
		 Key targetKey = keyFactory.create("³ÂÆ¤");
//		Key targetKey = keyFactory.get(IOUtil
//				.hexStringToByteArray("0C2891295454BCBD1240F00E40D0EA1D"));
		SearchOperation op = injector.getInstance(SearchOperation.class);

		op.setSearchType(PublishAndSearchType.NOTE).setFileSize(734197760L)
				.setTargetKey(targetKey).doSearch();

		System.out.println("kBuckets has " + kbr.getNeighbours().size()
				+ " nodes.");
		kbr.shutdown();
		// TimeUnit.MINUTES.sleep(2);
	}

}
