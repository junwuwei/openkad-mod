package cn.edu.jnu.cs.emulekad.kbr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.KBuckets;
import il.technion.ewolf.kbr.openkad.net.KadServer;

import org.apache.commons.codec.DecoderException;
import org.jmule.core.edonkey.packet.tag.ByteTag;
import org.jmule.core.edonkey.packet.tag.IntTag;
import org.jmule.core.edonkey.packet.tag.LongTag;
import org.jmule.core.edonkey.packet.tag.ShortTag;
import org.jmule.core.edonkey.packet.tag.Tag;
import org.jmule.core.edonkey.packet.tag.TagList;
import org.junit.Test;

import cn.edu.jnu.cs.emulekad.EMuleKad;
import cn.edu.jnu.cs.emulekad.EMuleKadModule;
import cn.edu.jnu.cs.emulekad.PublishHelper;
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
import com.google.inject.name.Names;

public class EMuleKadTest {

	private void joinNetwordByBootstrapFromKnownEMuleClient(EMuleKad eMuleKad)
			throws URISyntaxException {
		int basePort = 4662;
		 String ip = "219.222.18.218";
//		 String ip = "219.222.19.140";
//		String ip = "219.222.18.234";
//		String ip = "127.0.0.1";
		long stratTime = System.currentTimeMillis();
		eMuleKad.joinURI(Arrays.asList(new URI("openkad.udp://" + ip + ":"
				+ basePort + "/")));
		long endTime = System.currentTimeMillis();
		System.out.println("finished joining, used "
				+ TimeUnit.MILLISECONDS.toSeconds(endTime - stratTime)
				+ " seconds.");
	}

	private void joinNetwordByLoadNodeDatFile(EMuleKad eMuleKad,
			Injector injector) {
		NodesDatFile nodesDatFile = injector.getInstance(NodesDatFile.class);
		long stratTime = System.currentTimeMillis();
		eMuleKad.joinNode(nodesDatFile.readNodeFromFile());
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
		List<EMuleKad> eMuleKads = new ArrayList<EMuleKad>();
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
	
			EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
			eMuleKad.create();
			eMuleKads.add(eMuleKad);
			// server = injector.getInstance(KadServer.class);
		}
	
		eMuleKads.get(1).joinURI(
				Arrays.asList(new URI("emulekad.udp://127.0.0.1:" + basePort
						+ "/")));
		System.out.println("finished joining");
		assertTrue(eMuleKads.get(0).getNeighbours().isEmpty());
		assertTrue(eMuleKads.get(1).getNeighbours().size() == 1);
	}

	@Test
	public void TwoNodesPingTest() throws Throwable {
		int basePort = 10000;
		List<EMuleKad> eMuleKads = new ArrayList<EMuleKad>();
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
	
			EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
			eMuleKad.create();
			eMuleKads.add(eMuleKad);
			// server = injector.getInstance(KadServer.class);
		}
	
		eMuleKads.get(1).joinURI(
				Arrays.asList(new URI("emulekad.udp://127.0.0.1:" + basePort
						+ "/")));
		System.out.println("finished joining");
		assertEquals(eMuleKads.get(0).getNeighbours().get(0), eMuleKads.get(1)
				.getLocalNode());
		assertEquals(eMuleKads.get(1).getNeighbours().get(0), eMuleKads.get(0)
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
						.setProperty("openkad.refresh.enable", true + "")
						.setProperty("openkad.bootstrap.do_rendom_findnode",
								true + "")
						.setProperty("openkad.bootstrap.ping_befor_insert",
								true + "")
				// .setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10)
				// +
				// "")
				);
		EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
		System.out.println("local node key:"
				+ eMuleKad.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + eMuleKad.getLocalNode().getInetAddress());
		eMuleKad.create();

		joinNetwordByLoadNodeDatFile(eMuleKad, injector);
		
		KBuckets kBuckets=injector.getInstance(KBuckets.class);
		
		System.out.println(kBuckets);
		
		System.out.println("kBuckets has " + eMuleKad.getNeighbours().size()
				+ " nodes.");
		eMuleKad.shutdown();
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
		EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
		System.out.println("local node key:"
				+ eMuleKad.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + eMuleKad.getLocalNode().getInetAddress());
		eMuleKad.create();

		joinNetwordByBootstrapFromKnownEMuleClient(eMuleKad);
		// joinNetwordByLoadNodeDatFile(eMuleKad,injector);

		System.out.println("kBuckets has " + eMuleKad.getNeighbours().size()
				+ " nodes.");

		System.out.println("kBuckets has " + eMuleKad.getNeighbours().size()
				+ " nodes.");
		eMuleKad.shutdown();
		// TimeUnit.MINUTES.sleep(2);
	}

	@Test
	public void the2NodesShouldFindEachOther() throws Throwable {
		int basePort = 10000;
		List<EMuleKad> eMuleKads = new ArrayList<EMuleKad>();
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
	
			EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
			eMuleKad.create();
			eMuleKads.add(eMuleKad);
		}
	
		eMuleKads.get(1).joinURI(
				Arrays.asList(new URI("emulekad.udp://127.0.0.1:" + basePort
						+ "/")));
		System.out.println("finished joining");
	
		for (int i = 0; i < eMuleKads.size(); ++i) {
			System.out.println(eMuleKads.get(i));
			System.out.println("======");
		}
	
		List<Node> findNode = eMuleKads.get(1).findNode(
				eMuleKads.get(0).getLocalNode().getKey());
		System.out.println("findeNode=" + findNode);
		Assert.assertEquals(eMuleKads.get(0).getLocalNode(), findNode.get(0));
		Assert.assertEquals(eMuleKads.get(1).getLocalNode(), findNode.get(1));
	
		findNode = eMuleKads.get(0).findNode(eMuleKads.get(0).getLocalNode().getKey());
		Assert.assertEquals(eMuleKads.get(0).getLocalNode(), findNode.get(0));
		Assert.assertEquals(eMuleKads.get(1).getLocalNode(), findNode.get(1));
		// System.out.println(findNode);
	
		findNode = eMuleKads.get(0).findNode(eMuleKads.get(1).getLocalNode().getKey());
		System.out.println("findeNode=" + findNode);
		Assert.assertEquals(eMuleKads.get(1).getLocalNode(), findNode.get(0));
		Assert.assertEquals(eMuleKads.get(0).getLocalNode(), findNode.get(1));
	
		findNode = eMuleKads.get(1).findNode(eMuleKads.get(1).getLocalNode().getKey());
		Assert.assertEquals(eMuleKads.get(1).getLocalNode(), findNode.get(0));
		Assert.assertEquals(eMuleKads.get(0).getLocalNode(), findNode.get(1));
		eMuleKads.get(0).shutdown();
		eMuleKads.get(1).shutdown();
	
	}

	@Test
	public void findNodeTest() throws IOException, URISyntaxException {
		Injector injector = Guice.createInjector(new EMuleKadModule()
			.setProperty("openkad.refresh.enable", false + "")
			.setProperty("openkad.bootstrap.do_rendom_findnode", false + "")
		// .setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) +
		// "")
				);
		EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
		System.out.println("local node key:"
				+ eMuleKad.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + eMuleKad.getLocalNode().getInetAddress());
		eMuleKad.create();
		System.out.println("local node key:"
				+ eMuleKad.getLocalNode().getKey().toHexString());
		System.out.println("local ip:" + eMuleKad.getLocalNode().getInetAddress());

//		joinNetwordByBootstrapFromKnownEMuleClient(eMuleKad);
		 joinNetwordByLoadNodeDatFile(eMuleKad,injector);

		System.out.println("kBuckets has " + eMuleKad.getNeighbours().size()
				+ " nodes.");
		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
		Node to = provideOneEmuleNode(keyFactory);

		EMuleKadRequest res = new EMuleKadRequest(System.currentTimeMillis(),
				eMuleKad.getLocalNode());
		res.setKey(eMuleKad.getLocalNode().getKey()).setRecipient(to)
				.setRequestType(OpCodes.STORE);
//		.setRequestType(OpCodes.FIND_NODE);
//		.setRequestType(OpCodes.FIND_VALUE);
		KadServer server = injector.getInstance(KadServer.class);
		server.send(to, res);
		eMuleKad.shutdown();
	}

	@Test
	public void twoNodePublishAndSearchTest() throws URISyntaxException, IOException, DecoderException{
		int basePort = 10000;
		List<EMuleKad> eMuleKads = new ArrayList<EMuleKad>();
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
	
			EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
			eMuleKad.create();
			eMuleKads.add(eMuleKad);
		}
	
		eMuleKads.get(1).joinURI(
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
		
		Entry entry=new Entry(eMuleKads.get(1).getLocalNode().getKey(),tagList);
		op.setPublishType(PublishAndSearchType.SOURCE).setTargetKey(targetKey)
				.addEntry(entry).doPublish();
		
		SearchOperation searchOp = injector.getInstance(SearchOperation.class);
		
		searchOp.setSearchType(PublishAndSearchType.SOURCE).setFileSize(734197760L)
				.setStartPosition(0).setTargetKey(targetKey);
		searchOp.doSearch();
		eMuleKads.get(0).shutdown();
		eMuleKads.get(1).shutdown();
		
	}
	

	
	@Test
	public void testPublishSourceThenSearch() throws Throwable {
		Injector injector = Guice.createInjector(new EMuleKadModule()
//		.setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) +"")
		.setProperty("openkad.refresh.enable", false + "").setProperty(
				"openkad.bootstrap.do_rendom_findnode", false + ""));
		EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
		com.google.inject.Key<Node> key=com.google.inject.Key.get(Node.class, Names.named("openkad.local.node"));
		Node localNode =injector.getInstance(key);
		System.out.println(localNode);
		
		eMuleKad.create();
//		joinNetwordByBootstrapFromKnownEMuleClient(eMuleKad);
		 joinNetwordByLoadNodeDatFile(eMuleKad,injector);
		
		PublishHelper publishHelper=injector.getInstance(PublishHelper.class);
		String str="hello world!";
		Entry entry=publishHelper.makeSourceEntry(str);
		System.out.println(entry);
		
		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
		Key targetKey = keyFactory.generate();
		eMuleKad.publishSource(targetKey, entry);
		
		List<Entry> entries=eMuleKad.searchSource(targetKey);
		boolean found=false;
		for (Entry entry2 : entries) {
			String string=publishHelper.getVanishString(entry2);
			if(str.equals(string)){
				found=true;
				break;
			}
		}
		
		eMuleKad.shutdown();
		
		assertTrue(found);
	}
	@Test
	public void testPublishNoteThenSearch() throws Throwable {
		Injector injector = Guice.createInjector(new EMuleKadModule()
//		.setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) +"")
		.setProperty("openkad.refresh.enable", false + "").setProperty(
				"openkad.bootstrap.do_rendom_findnode", false + ""));
		EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
		com.google.inject.Key<Node> key=com.google.inject.Key.get(Node.class, Names.named("openkad.local.node"));
		Node localNode =injector.getInstance(key);
		System.out.println(localNode);
		
		eMuleKad.create();
//		joinNetwordByBootstrapFromKnownEMuleClient(eMuleKad);
		 joinNetwordByLoadNodeDatFile(eMuleKad,injector);
		
		PublishHelper publishHelper=injector.getInstance(PublishHelper.class);
		String str="hello world!";
		Entry entry=publishHelper.makeNoteEntry(str);
		System.out.println(entry);
		
		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
		Key targetKey = keyFactory.generate();
		eMuleKad.publishNote(targetKey, entry);
		
		List<Entry> entries=eMuleKad.searchNote(targetKey);
		boolean found=false;
		for (Entry entry2 : entries) {
			String string=publishHelper.getVanishString(entry2);
			if(str.equals(string)){
				found=true;
				break;
			}
		}
		
		eMuleKad.shutdown();
		
		assertTrue(found);
	}
	
	@Test
	public void testPublishKeyWordThenSearch() throws Throwable {
		Injector injector = Guice.createInjector(new EMuleKadModule()
//		.setProperty("openkad.net.timeout",TimeUnit.MINUTES.toMillis(10) +"")
		.setProperty("openkad.refresh.enable", false + "").setProperty(
				"openkad.bootstrap.do_rendom_findnode", false + ""));
		EMuleKad eMuleKad = injector.getInstance(EMuleKad.class);
		com.google.inject.Key<Node> key=com.google.inject.Key.get(Node.class, Names.named("openkad.local.node"));
		Node localNode =injector.getInstance(key);
		System.out.println(localNode);
		
		eMuleKad.create();
//		joinNetwordByBootstrapFromKnownEMuleClient(eMuleKad);
		// joinNetwordByLoadNodeDatFile(eMuleKad,injector);
		
		PublishHelper publishHelper=injector.getInstance(PublishHelper.class);
		String str="hello world!";
		Entry entry=publishHelper.makeKeywordEntry(str);
		System.out.println(entry);
		
		KeyFactory keyFactory = injector.getInstance(KeyFactory.class);
		Key targetKey = keyFactory.generate();
		eMuleKad.publishKeyword(targetKey, Arrays.asList(entry));
		
		List<Entry> entries=eMuleKad.searchKeyword(targetKey);
		boolean found=false;
		for (Entry entry2 : entries) {
			String string=publishHelper.getVanishString(entry2);
			if(str.equals(string)){
				found=true;
				break;
			}
		}
		
		eMuleKad.shutdown();
		
		assertTrue(found);
	}

}
