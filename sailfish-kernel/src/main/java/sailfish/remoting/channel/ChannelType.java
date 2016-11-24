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
package sailfish.remoting.channel;

/**
 * @author spccold
 * @version $Id: ChannelType.java, v 0.1 2016年11月24日 下午7:04:12 spccold Exp $
 */
public enum ChannelType {
	/** read channel for {@link ReadWriteExchangeChannelGroup} */
	read((byte) 0),
	/** write channel for {@link ReadWriteExchangeChannelGroup} */
	write((byte) 1),
	/** read&write channel for {@link DefaultExchangeChannelGroup} */
	readwrite((byte) 2),

	unknow((byte) -1);
	;

	byte code;

	private ChannelType(byte code) {
		this.code = code;
	}

	public byte code() {
		return code;
	}

	public ChannelType toType(byte type) {
		switch (type) {
		case 0:
			return read;
		case 1:
			return write;
		case 2:
			return readwrite;
		default:
			return unknow;
		}
	}
}
