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
 * @version $Id: ReadWriteChannelConfig.java, v 0.1 2016年11月22日 下午2:58:33
 *          spccold Exp $
 */
public class ReadWriteChannelConfig {
	private UUID uuid;
	private int index;
	private boolean write;

	public ReadWriteChannelConfig(boolean write, UUID uuid, int index) {
		this.write = write;
		this.uuid = uuid;
		this.index = index;
	}

	public ReadWriteChannelConfig write(boolean write){
		this.write = write;
		return this;
	}
	public boolean write(){
		return this.write;
	}

	public UUID uuid(){
		return this.uuid;
	}
	
	public ReadWriteChannelConfig index(int index){
		this.index = index;
		return this;
	}
	public int index(){
		return this.index;
	}
	
	public ReadWriteChannelConfig deepCopy(){
		ReadWriteChannelConfig copy = new ReadWriteChannelConfig(write, uuid, index);
		return copy;
	}
}
