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
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import sailfish.remoting.Address;
import sailfish.remoting.codec.RemotingDecoder;
import sailfish.remoting.codec.RemotingEncoder;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.eventgroup.ClientEventGroup;
import sailfish.remoting.handler.ChannelEventsHandler;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.handler.ShareableSimpleChannelInboundHandler;
import sailfish.remoting.protocol.Protocol;

/**
 * @author spccold
 * @version $Id: EagerExchangeChannel.java, v 0.1 2016年11月21日 下午11:17:02 spccold
 *          Exp $
 */
public class EagerExchangeChannel extends SingleConnctionExchangeChannel {

	public EagerExchangeChannel(ExchangeChannelGroup parent, Address address) {
		super(parent, address, true);
	}

	public EagerExchangeChannel(ExchangeChannelGroup parent, Address address, int connectTimeout,
			int reconnectInterval) {
		super(parent, address, connectTimeout, reconnectInterval, true);
	}

	public EagerExchangeChannel(ExchangeChannelGroup parent, int idleTimeout, int maxIdleTimeOut, Address address) {
		super(parent, address, idleTimeout, maxIdleTimeOut, true);
	}

	public EagerExchangeChannel(ExchangeChannelGroup parent, Address address, int connectTimeout, int reconnectInterval,
			int idleTimeout, int maxIdleTimeOut) {
		super(parent, address, connectTimeout, reconnectInterval, idleTimeout, maxIdleTimeOut, true);
	}

	@Override
	public boolean isAvailable() {
		if (isClosed()) {
			return false;
		}
		boolean isAvailable = false;
		if (!reconnectting && !(isAvailable = available())) {
			synchronized (this) {
				if (!reconnectting && !(isAvailable = available())) {
					// add reconnect task
					//TODO ReconnectManager.INSTANCE.addReconnectTask(this);
					this.reconnectting = true;
				}
			}
		}
		return isAvailable;
	}

	@Override
	protected io.netty.channel.ChannelInitializer<SocketChannel> newChannelInitializer(
			final MsgHandler<Protocol> handler) {
		final EventExecutorGroup executorGroup = ClientEventGroup.INSTANCE.getExecutorGroup();
		return new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				ch.attr(ChannelAttrKeys.idleTimeout).set(idleTimeout);
				ch.attr(ChannelAttrKeys.maxIdleTimeout).set(maxIdleTimeOut);
				// TODO should increase ioRatio when every ChannelHandler bind
				// to executorGroup?
				pipeline.addLast(executorGroup, new RemotingEncoder());
				pipeline.addLast(executorGroup, new RemotingDecoder());
				pipeline.addLast(executorGroup, new IdleStateHandler(idleTimeout, 0, 0));
				pipeline.addLast(executorGroup, new ChannelEventsHandler(true));
				pipeline.addLast(executorGroup, new ShareableSimpleChannelInboundHandler(handler, true));
			}
		};
	}
}
