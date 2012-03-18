package il.technion.ewolf.kbr.openkad.op;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;

import java.util.List;

public interface FindNodeOperation {

	/**
	 * Sets the key to be found.
	 * Do not change this value after invoking doFindNode.
	 * 
	 * @param key the key to be found
	 * @return this for fluent interface
	 */
	public abstract FindNodeOperation setKey(Key key);

	public abstract int getNrQueried();

	/**
	 * Do the find node recursive operation
	 * @return a list of nodes closest to the set key
	 */
	public abstract List<Node> doFindNode();

}