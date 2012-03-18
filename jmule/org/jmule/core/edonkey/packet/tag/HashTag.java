/*
 *  JMule - Java file sharing client
 *  Copyright (C) 2007-2008 JMule team ( jmule@jmule.org / http://jmule.org )
 *
 *  Any parts of this program derived from other projects, or contributed
 *  by third-party developers are copyrighted by their respective authors.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package org.jmule.core.edonkey.packet.tag;

import java.nio.ByteBuffer;

import org.jmule.core.edonkey.FileHash;
import org.jmule.core.utils.Misc;
import static org.jmule.core.edonkey.E2DKConstants.*;
/**
 * Created on Jul 15, 2009
 * @author binary256
 * @version $Revision: 1.2 $
 * Last changed by $Author: binary255 $ on $Date: 2009/09/17 17:52:04 $
 */
public class HashTag extends StandartTag {

	private FileHash hash;
	
	public HashTag(byte[] tagName, FileHash hash) {
		super(TAGTYPE_HASH, tagName);
		this.hash = hash;
	}

	ByteBuffer getValueAsByteBuffer() {
		ByteBuffer result = Misc.getByteBuffer(hash.length());
		result.put(hash.getHash());
		result.position(0);
		return result;
	}


	int getValueLength() {
		return hash.length();
	}


	public Object getValue() {

		return hash;
	}

	public void setValue(Object object) {
		hash = (FileHash) object;
		
	}

}
