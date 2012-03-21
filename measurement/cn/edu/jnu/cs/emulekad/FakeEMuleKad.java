package cn.edu.jnu.cs.emulekad;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import cn.edu.jnu.cs.emulekad.indexer.Entry;
import cn.edu.jnu.cs.emulekad.indexer.EntryList;

public class FakeEMuleKad implements EMuleKad {
	public enum OperationResult {
		SUCCESS, FAIL
	}

	private OperationResult operationResult;

	public FakeEMuleKad(OperationResult operationResult) {
		this.operationResult = operationResult;
	}

	public OperationResult getOperationResult() {
		return operationResult;
	}

	public void setOperationResult(OperationResult operationResult) {
		this.operationResult = operationResult;
	}

	private Map<Key, EntryList> indexed = new HashMap<Key, EntryList>();

	@Override
	public void create() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void joinURI(Collection<URI> bootstraps) {
		// TODO Auto-generated method stub

	}

	@Override
	public void joinNode(Collection<Node> bootstraps) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Node> findNode(Key k) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(String tag, MessageHandler handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendMessage(Node to, String tag, Serializable msg)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Future<Serializable> sendRequest(Node to, String tag,
			Serializable msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> void sendRequest(Node to, String tag, Serializable msg,
			A attachment, CompletionHandler<Serializable, A> handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public KeyFactory getKeyFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Node> getNeighbours() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getLocalNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public int publishSource(Key targetKey, Entry entry) {
		return doPublishOperation(targetKey, entry);
	}

	private int doPublishOperation(Key targetKey, Entry entry) {
		if (operationResult == OperationResult.SUCCESS) {
			EntryList entryList = indexed.get(targetKey);
			if (entryList == null) {
				entryList = new EntryList();
				indexed.put(targetKey, entryList);
			}
			entryList.add(entry);
			return 10;
		} else {
			return 0;
		}
	}

	@Override
	public int publishNote(Key targetKey, Entry entry) {
		return doPublishOperation(targetKey, entry);
	}

	@Override
	public int publishKeyword(Key targetKey, List<Entry> entries) {
		return doPublishOperation(targetKey, entries.get(0));
	}

	@Override
	public List<Entry> searchSource(Key targetKey) {
		return doSearchOperation(targetKey);
	}

	private List<Entry> doSearchOperation(Key targetKey) {
		if (operationResult == OperationResult.SUCCESS) {
			EntryList entryList = indexed.get(targetKey);
			if (entryList == null) {
				entryList = new EntryList();
			}
			return entryList;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<Entry> searchNote(Key targetKey) {
		return doSearchOperation(targetKey);
	}

	@Override
	public List<Entry> searchKeyword(Key targetKey) {
		return doSearchOperation(targetKey);
	}

}
