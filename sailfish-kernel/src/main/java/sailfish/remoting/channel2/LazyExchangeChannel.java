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
package sailfish.remoting.channel2;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import sailfish.remoting.Address;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;

/**
 * @author spccold
 * @version $Id: LazyExchangeChannel.java, v 0.1 2016年11月21日 下午11:18:11 spccold Exp $
 */
public class LazyExchangeChannel extends SingleConnctionExchangeChannel{
	public LazyExchangeChannel(ExchangeChannelGroup parent, Address address, boolean doConnect) {
		super(parent, address, doConnect);
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	protected ChannelInitializer<SocketChannel> newChannelInitializer(MsgHandler<Protocol> handler) {
		return null;
	}
}
