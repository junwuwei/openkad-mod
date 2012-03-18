/**
 * 
 */
package cn.edu.jnu.cs.emulekad.msg;

import com.google.inject.name.Named;

import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.msg.KadRequest;

/**
 *
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class BootstrapRequest extends KadRequest {
	private static final long serialVersionUID = -4192460848171096615L;
	
	public BootstrapRequest(@Named("openkad.rnd.id")long id, 
			@Named("openkad.local.node") Node src) {
		super(id, src);
	}
	
	public BootstrapRequest(){
		super(System.currentTimeMillis(), null);
	}

	@Override
	public BootstrapResponse generateResponse( @Named("openkad.local.node") Node localNode) {
		return new BootstrapResponse(getId(), localNode);
	}

}
