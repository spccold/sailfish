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
package sailfish.remoting.constants;

import java.util.UUID;

import io.netty.util.AttributeKey;

/**
 * @author spccold
 * @version $Id: ChannelAttrKeys.java, v 0.1 2016年11月6日 上午11:22:41 spccold Exp $
 */
public interface ChannelAttrKeys {
    //for idle handle and heart beat
	AttributeKey<Integer> idleTimeout = AttributeKey.valueOf("idleTimeout");
	AttributeKey<Integer> maxIdleTimeout = AttributeKey.valueOf("maxIdleTimeout");
	AttributeKey<Long> lastReadTimeMillis = AttributeKey.valueOf("lastReadTimeMillis");
	
	//for read write splitting
	AttributeKey<Boolean> writeChannel = AttributeKey.valueOf("readwrite.writeChannel");
	AttributeKey<UUID> uuid = AttributeKey.valueOf("readwrite.uuid");
	AttributeKey<Integer> channelIndex = AttributeKey.valueOf("readwrite.channelIndex");
}
