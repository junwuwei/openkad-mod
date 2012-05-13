/**
 * 
 */
package cn.edu.jnu.cs.emulekad.msg;

import java.util.List;

import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.msg.KadResponse;

/**
 * Bootstrap»Ø¸´ÏûÏ¢
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class BootstrapResponse extends KadResponse {

	private static final long serialVersionUID = 8784177292654239075L;
	private List<Node> nodes;

	public BootstrapResponse(long id, @Named("openkad.local.node")Node src) {
		super(id, src);
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	
}
