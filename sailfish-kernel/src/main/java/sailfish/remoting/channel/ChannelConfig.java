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

import java.util.UUID;

/**
 * @author spccold
 * @version $Id: ChannelConfig.java, v 0.1 2016年11月22日 下午2:58:33
 *          spccold Exp $
 */
public class ChannelConfig {
	private final UUID uuid;
	private final byte type;
	private final short connections;

	private short index;
	public ChannelConfig(UUID uuid, byte type, short connections, short index) {
		this.uuid = uuid;
		this.type = type;
		this.connections = connections;
		
		this.index = index;
	}

	public UUID uuid(){
		return this.uuid;
	}

	public byte type(){
		return this.type;
	}

	public short connections(){
		return this.connections;
	}
	
	public ChannelConfig index(short index){
		this.index = index;
		return this;
	}
	public short index(){
		return this.index;
	}
	
	public boolean isRead(){
		return ChannelType.read.code == type;
	}
	
	public boolean isWrite(){
		return ChannelType.write.code == type;
	}
	
	public ChannelConfig deepCopy(){
		return new ChannelConfig(uuid, type, connections, index);
	}
}
