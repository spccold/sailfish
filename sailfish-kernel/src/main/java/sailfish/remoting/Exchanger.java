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
import sailfish.remoting.channel.ExchangeChannelGroup;
import sailfish.remoting.channel.ReadWriteExchangeChannelGroup;
import sailfish.remoting.configuration.AbstractExchangeConfig;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.configuration.ExchangeServerConfig;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.DefaultMsgHandler;
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
		checkConfig(config);
		MsgHandler<Protocol> msgHandler = new DefaultMsgHandler(config.getRequestProcessors());
		switch (config.mode()) {
		case simple:
		case multiconns:
			// TODO
			return new DefaultExchangeChannelGroup(null, msgHandler, config.address(), config.connections(),
					config.connectTimeout(), config.reconnectInterval(), config.idleTimeout(), config.idleTimeout(),
					config.isLazyConnection(), null);
		case readwrite:
			// TODO
			return new ReadWriteExchangeChannelGroup(msgHandler, config.address(), config.connectTimeout(),
					config.reconnectInterval(), config.idleTimeout(), config.maxIdleTimeout(),
					config.isLazyConnection(), config.connections(), config.writeConnections());
		default:
			throw new IllegalArgumentException("invalid channel mode");
		}
	}

	public static ExchangeServer bind(ExchangeServerConfig config) throws SailfishException {
		checkConfig(config);
		return new ExchangeServer(config);
	}

	private static void checkConfig(AbstractExchangeConfig config) {
		ParameterChecker.checkNotNull(config, "exchange config").check();
	}
}
