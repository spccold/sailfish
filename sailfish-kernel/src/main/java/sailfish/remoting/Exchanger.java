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
package sailfish.remoting;

import sailfish.remoting.channel.DefaultExchangeChannelGroup;
import sailfish.remoting.channel.EagerExchangeChannel;
import sailfish.remoting.channel.ExchangeChannelGroup;
import sailfish.remoting.channel.LazyExchangeChannel;
import sailfish.remoting.channel.ReadWriteExchangeChannelGroup;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.configuration.ExchangeServerConfig;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.utils.ParameterChecker;

/**
 * 
 * @author spccold
 * @version $Id: Exchanger.java, v 0.1 2016年10月26日 下午11:40:38 jileng Exp $
 */
public class Exchanger {
	public static ExchangeChannelGroup connect(ExchangeClientConfig config) throws SailfishException {
		ParameterChecker.checkNotNull(config, "ExchangeClientConfig");
		config.check();
		ExchangeChannelGroup channelGroup = null;
		switch (config.mode()) {
		case simple:
			if (config.isLazyConnection()) {
				channelGroup = new LazyExchangeChannel(null, config.address(), config.connectTimeout(),
						config.reconnectInterval(), config.idleTimeout(), config.maxIdleTimeout(), null);
			} else {
				channelGroup = new EagerExchangeChannel(null, config.address(), config.connectTimeout(),
						config.reconnectInterval(), config.idleTimeout(), config.maxIdleTimeout(), null);
			}
			break;
		case multiconns:
			channelGroup = new DefaultExchangeChannelGroup(config.address(), config.connections(),
					config.connectTimeout(), config.reconnectInterval(), config.idleTimeout(), config.idleTimeout(),
					config.isLazyConnection(), null);
			break;
		case readwrite:
			channelGroup = new ReadWriteExchangeChannelGroup(config.address(), config.connectTimeout(),
					config.reconnectInterval(), config.idleTimeout(), config.maxIdleTimeout(),
					config.isLazyConnection(), config.connections() - config.writeConnections(),
					config.writeConnections());
			break;
		default:
			throw new IllegalArgumentException("invalid channel mode");
		}
		return channelGroup;
	}

	public static ExchangeServer bind(ExchangeServerConfig config, MsgHandler<Protocol> handler)
			throws SailfishException {
		config.check();
		return new ExchangeServer(config, handler);
	}
}
