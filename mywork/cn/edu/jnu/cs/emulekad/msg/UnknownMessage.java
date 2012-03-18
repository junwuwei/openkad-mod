package cn.edu.jnu.cs.emulekad.msg;

import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;

/**
 *
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class UnknownMessage extends KadMessage {

	private static final long serialVersionUID = -7462534451438965739L;

	public UnknownMessage(long id, Node src) {
		super(id, src);
	}

}
