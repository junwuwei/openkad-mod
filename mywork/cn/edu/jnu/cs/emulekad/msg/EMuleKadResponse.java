package cn.edu.jnu.cs.emulekad.msg;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.msg.FindNodeResponse;

/**
 *
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class EMuleKadResponse extends FindNodeResponse {

	private static final long serialVersionUID = 2285834650106736220L;
	private final Key key;

	public EMuleKadResponse(long id, Node src, Key key) {
		super(id, src);
		this.key=key;
	}

	public Key getKey() {
		return key;
	}

}
