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

import sailfish.remoting.Address;
import sailfish.remoting.exceptions.SailfishException;

/**
 * @author spccold
 * @version $Id: EagerExchangeChannel.java, v 0.1 2016年11月21日 下午11:17:02 spccold
 *          Exp $
 */
public class EagerExchangeChannel extends SingleConnctionExchangeChannel {
	public EagerExchangeChannel(ExchangeChannelGroup parent, Address address, ReadWriteChannelConfig config) throws SailfishException {
		super(parent, address, config, true);
	}

	public EagerExchangeChannel(ExchangeChannelGroup parent, Address address, int connectTimeout, int reconnectInterval, ReadWriteChannelConfig config)
			throws SailfishException {
		super(parent, address, connectTimeout, reconnectInterval, config, true);
	}

	public EagerExchangeChannel(ExchangeChannelGroup parent, int idleTimeout, int maxIdleTimeOut, Address address, ReadWriteChannelConfig config)
			throws SailfishException {
		super(parent, address, idleTimeout, maxIdleTimeOut, config, true);
	}

	public EagerExchangeChannel(ExchangeChannelGroup parent, Address address, int connectTimeout, int reconnectInterval,
			int idleTimeout, int maxIdleTimeOut, ReadWriteChannelConfig config) throws SailfishException {
		super(parent, address, connectTimeout, reconnectInterval, idleTimeout, maxIdleTimeOut, config, true);
	}

	@Override
	public boolean isAvailable() {
		if (isClosed()) {
			return false;
		}
		return super.isAvailable();
	}
}
