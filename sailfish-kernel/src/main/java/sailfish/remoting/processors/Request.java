/**
 *
 *	Copyright 2016-2016 spccold
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 *
 */
package sailfish.remoting.processors;

import sailfish.remoting.constants.CompressType;
import sailfish.remoting.constants.SerializeType;

/**
 * @author spccold
 * @version $Id: Request.java, v 0.1 2016年11月29日 下午5:13:03 spccold Exp $
 */
public class Request {
	private boolean oneway;
	private byte serializeType = SerializeType.NON_SERIALIZE;
	private byte compressType = CompressType.NON_COMPRESS;
	private byte[] requestData;
	private byte langType;
	
	public Request(boolean oneway, byte serializeType, byte compressType, byte[] requestData, byte langType) {
		this.oneway = oneway;
		this.serializeType = serializeType;
		this.compressType = compressType;
		this.requestData = requestData;
		this.langType = langType;
	}

	/**
	 * @return the oneway
	 */
	public boolean isOneway() {
		return oneway;
	}

	/**
	 * @return the serializeType
	 */
	public byte getSerializeType() {
		return serializeType;
	}

	/**
	 * @return the compressType
	 */
	public byte getCompressType() {
		return compressType;
	}

	/**
	 * @return the requestData
	 */
	public byte[] getRequestData() {
		return requestData;
	}

	/**
	 * @return the langType
	 */
	public byte getLangType() {
		return langType;
	}
}
