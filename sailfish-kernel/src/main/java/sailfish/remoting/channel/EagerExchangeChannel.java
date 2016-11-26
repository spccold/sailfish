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
import sailfish.remoting.exceptions.SailfishException;

/**
 * @author spccold
 * @version $Id: EagerExchangeChannel.java, v 0.1 2016年11月21日 下午11:17:02 spccold Exp $
 */
public final class EagerExchangeChannel extends SingleConnctionExchangeChannel {

	EagerExchangeChannel(Bootstrap bootstrap, ExchangeChannelGroup parent, int reconnectInterval)
			throws SailfishException {
		super(bootstrap, parent, reconnectInterval, true);
	}

	@Override
	public boolean isAvailable() {
		if (isClosed()) {
			return false;
		}
		return super.isAvailable();
	}
}
