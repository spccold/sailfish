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
import java.util.concurrent.CountDownLatch;

import io.netty.util.AttributeKey;
import sailfish.remoting.ExchangeServer;
import sailfish.remoting.channel.ExchangeChannelGroup;

/**
 * @author spccold
 * @version $Id: ChannelAttrKeys.java, v 0.1 2016年11月6日 上午11:22:41 spccold Exp $
 */
public interface ChannelAttrKeys {
    //for idle handle and heart beat
	AttributeKey<Byte> maxIdleTimeout = AttributeKey.valueOf("sailfish.maxIdleTimeout");
	AttributeKey<Long> lastReadTimeMillis = AttributeKey.valueOf("sailfish.lastReadTimeMillis");

	//side
	AttributeKey<Boolean> clientSide = AttributeKey.valueOf("sailfish.side");
	
	AttributeKey<ExchangeChannelGroup> channelGroup = AttributeKey.valueOf("sailfish.channelGroup");
	AttributeKey<ExchangeServer> exchangeServer = AttributeKey.valueOf("sailfish.exchangeServer");
	AttributeKey<String> uuidStr = AttributeKey.valueOf("sailfish.uuidStr");
	
	interface OneTime{
		//for idle handle
		AttributeKey<Byte> idleTimeout = AttributeKey.valueOf("sailfish.idleTimeout");
		AttributeKey<CountDownLatch> awaitNegotiate = AttributeKey.valueOf("sailfish.awaitNegotiate");
		
		//for negotiate with remote peer
		AttributeKey<UUID> uuid = AttributeKey.valueOf("sailfish.uuid");
		AttributeKey<Byte> channelType = AttributeKey.valueOf("sailfish.channelType");
		AttributeKey<Short> connections = AttributeKey.valueOf("sailfish.connections");
		AttributeKey<Short> writeConnections = AttributeKey.valueOf("sailfish.writeConnections");
		AttributeKey<Short> channelIndex = AttributeKey.valueOf("sailfish.channelIndex");
		AttributeKey<Boolean> reverseIndex = AttributeKey.valueOf("sailfish.reverseIndex");
	}
}
