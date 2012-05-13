/**
 * 
 */
package cn.edu.jnu.cs.emulekad;

import java.util.List;

import cn.edu.jnu.cs.emulekad.indexer.Entry;
import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeybasedRouting;

/**
 * eMule Kad接口，相比KeybasedRouting添加了eMule kad特有的方法。
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public interface EMuleKad extends KeybasedRouting {

	int publishSource(Key targetKey, Entry entry);

	int publishNote(Key targetKey, Entry entry);

	int publishKeyword(Key targetKey, List<Entry> entries);

	List<Entry> searchSource(Key targetKey);

	List<Entry> searchNote(Key targetKey);

	List<Entry> searchKeyword(Key targetKey);

}
