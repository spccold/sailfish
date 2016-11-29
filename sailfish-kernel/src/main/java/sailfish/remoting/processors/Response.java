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
 * @version $Id: Response.java, v 0.1 2016年11月29日 下午5:18:01 spccold Exp $
 */
public class Response {
	private boolean success;
	private byte serializeType = SerializeType.NON_SERIALIZE;
	private byte compressType = CompressType.NON_COMPRESS;
	private byte[] responseData;
	
	public Response(boolean success, byte[] responseData) {
		this.success = success;
		this.responseData = responseData;
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * @return the serializeType
	 */
	public byte getSerializeType() {
		return serializeType;
	}

	/**
	 * @param serializeType the serializeType to set
	 */
	public void setSerializeType(byte serializeType) {
		this.serializeType = serializeType;
	}

	/**
	 * @return the compressType
	 */
	public byte getCompressType() {
		return compressType;
	}

	/**
	 * @param compressType the compressType to set
	 */
	public void setCompressType(byte compressType) {
		this.compressType = compressType;
	}

	/**
	 * @return the responseData
	 */
	public byte[] getResponseData() {
		return responseData;
	}

	/**
	 * @param responseData the responseData to set
	 */
	public void setResponseData(byte[] responseData) {
		this.responseData = responseData;
	}
}
