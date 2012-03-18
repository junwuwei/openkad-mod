/**
 * 
 */
package cn.edu.jnu.cs.emulekad.net.filter;

import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.net.filter.MessageFilter;

/**
 *
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class SrcMessageFilter implements MessageFilter {
	
	private final Node src;
	
	public SrcMessageFilter(Node src) {
		this.src = src;
	}
	
	
	
	@Override
	public boolean shouldHandle(KadMessage m) {
		return src.getInetAddress().equals(m.getSrc().getInetAddress());
	}

}
