/**
 * 
 */
package cn.edu.jnu.cs.emulekad.net.filter;

import cn.edu.jnu.cs.emulekad.msg.EMuleKadRequest;
import cn.edu.jnu.cs.emulekad.msg.EMuleKadResponse;
import cn.edu.jnu.cs.emulekad.msg.PublishRequest;
import cn.edu.jnu.cs.emulekad.msg.PublishResponse;
import cn.edu.jnu.cs.emulekad.msg.SearchRequest;
import cn.edu.jnu.cs.emulekad.msg.SearchResponse;
import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.net.filter.MessageFilter;

/**
 *
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class TargetKeyMessageFilter implements MessageFilter {
	private Key targetKey;

	public TargetKeyMessageFilter(Key targetKey) {
		this.targetKey = targetKey;
	}

	@Override
	public boolean shouldHandle(KadMessage msg) {
		if(msg instanceof EMuleKadResponse){
			EMuleKadResponse res=(EMuleKadResponse) msg;
			if(targetKey.equals(res.getKey())){
				return true;
			}
			return false;
		}else if(msg instanceof EMuleKadRequest){
			EMuleKadRequest req=(EMuleKadRequest) msg;
			if(targetKey.equals(req.getKey())){
				return true;
			}
			return false;
		}else if(msg instanceof SearchRequest){
			SearchRequest req=(SearchRequest) msg;
			if(targetKey.equals(req.getTargetKey())){
				return true;
			}
			return false;
		}else if(msg instanceof SearchResponse){
			SearchResponse req=(SearchResponse) msg;
			if(targetKey.equals(req.getTargetKey())){
				return true;
			}
			return false;
		}else if(msg instanceof PublishRequest){
			PublishRequest req=(PublishRequest) msg;
			if(targetKey.equals(req.getTargetKey())){
				return true;
			}
			return false;
		}else if(msg instanceof PublishResponse){
			PublishResponse req=(PublishResponse) msg;
			if(targetKey.equals(req.getTargetKey())){
				return true;
			}
			return false;
		}
		return false;
	}

}
