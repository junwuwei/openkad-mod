package il.technion.ewolf.kbr.openkad;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;

import java.util.List;

public interface KBuckets {

	/**
	 * Uses the keyFactory to generate keys which will fit to different buckets
	 * @return a list of random keys where no 2 keys will fit into the same bucket
	 */
	List<Key> randomKeysForAllBuckets();

	/**
	 * Register this data structure to listen to incoming messages and update itself
	 * accordingly.
	 * Invoke this method after creating the entire system
	 */
	void registerIncomingMessageHandler();

	/**
	 * Inserts a node to the data structure
	 * The can be rejected, depending on the bucket policy
	 * @param node
	 */
	void insert(KadNode node);

	/**
	 * 
	 * @return a list containing all the nodes in the data structure
	 */
	List<Node> getAllNodes();

	void markAsDead(Node n);

	/**
	 * Returns a single bucket's content. The bucket number is calculated
	 * using the given key according to its prefix with the local node's key
	 * as explained above.
	 * 
	 * @param k key to calculate the bucket from
	 * @return a list of nodes from a particular bucket
	 */
	List<Node> getAllFromBucket(Key k);

	/**
	 * Gets all nodes with keys closest to the given k.
	 * The size of the list will be MIN(n, total number of nodes in the data structure)
	 * @param k the key which the result's nodes are close to
	 * @param n the maximum number of nodes expected
	 * @return a list of nodes sorted by proximity to k
	 */
	List<Node> getClosestNodesByKey(Key k, int n);

	/**
	 * Gets all nodes with keys closest to the given k.
	 * The size of the list will be MIN(n, total number of nodes in the data structure)
	 * @param k the key which the result's nodes are close to
	 * @param n the maximum number of nodes expected
	 * @return a list of nodes sorted by proximity to the given key's color
	 */
	List<Node> getClosestNodesByColor(Key k, int n);

	List<Node> getNodesFromColorBucket(Key k);

}