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

import io.netty.bootstrap.Bootstrap;
import sailfish.remoting.Address;
import sailfish.remoting.Tracer;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;

/**
 * @author spccold
 * @version $Id: DefaultExchangeChannelGroup.java, v 0.1 2016年11月22日 下午4:10:23
 *          spccold Exp $
 */
public final class DefaultExchangeChannelGroup extends MultiConnectionsExchangeChannelGroup {

	public DefaultExchangeChannelGroup(Tracer tracer, MsgHandler<Protocol> msgHandler, Address address, 
			short connections, boolean lazy, boolean reverseIndex, ExchangeChannelGroup channelGroup)
			throws SailfishException {
		this(tracer, msgHandler, address, connections, lazy, reverseIndex, null, channelGroup);
	}

	public DefaultExchangeChannelGroup(Tracer tracer, MsgHandler<Protocol> msgHandler, Address address, short connections, boolean lazy, 
			boolean reverseIndex, ChannelConfig config, ExchangeChannelGroup channelGroup)
			throws SailfishException {
		super(null == tracer ? new Tracer() : tracer, msgHandler, address, connections, lazy, 
				reverseIndex, config, channelGroup);
	}

	public DefaultExchangeChannelGroup(Tracer tracer, MsgHandler<Protocol> msgHandler, Address address, short connections, boolean lazy, int connectTimeout,
			int reconnectInterval, boolean reverseIndex, ChannelConfig config, ExchangeChannelGroup channelGroup) throws SailfishException {
		super(null == tracer ? new Tracer() : tracer, msgHandler, address, connections, lazy, 
				connectTimeout, reconnectInterval, reverseIndex, config, channelGroup);
	}

	public DefaultExchangeChannelGroup(Tracer tracer, MsgHandler<Protocol> msgHandler, Address address, short connections, byte idleTimeout, byte maxIdleTimeOut,
			boolean lazy, boolean reverseIndex, ChannelConfig config, ExchangeChannelGroup channelGroup) throws SailfishException {
		super(null == tracer ? new Tracer() : tracer, msgHandler, address, connections, idleTimeout,
				maxIdleTimeOut, lazy, reverseIndex, config, channelGroup);
	}

	public DefaultExchangeChannelGroup(Tracer tracer, MsgHandler<Protocol> msgHandler, Address address, short connections, int connectTimeout, int reconnectInterval,
			byte idleTimeout, byte maxIdleTimeOut, boolean lazy, boolean reverseIndex, ChannelConfig config, ExchangeChannelGroup channelGroup) throws SailfishException {
		super(null == tracer ? new Tracer() : tracer, msgHandler, address, connections, connectTimeout, 
				reconnectInterval, idleTimeout, maxIdleTimeOut, lazy, reverseIndex, config, channelGroup);
	}

	/**
	 * {@link ReadWriteExchangeChannelGroup}'s read connections must be initialed eagerly whether lazy is true or false
	 */
	@Override
	protected ExchangeChannel newChild(Bootstrap bootstrap, int reconnectInterval,
			boolean lazy, boolean readChannel) throws SailfishException {
		if (lazy && (!readChannel)) {
			return new LazyExchangeChannel(bootstrap, this, reconnectInterval);
		} 
		return new EagerExchangeChannel(bootstrap, this, reconnectInterval);
	}
}
