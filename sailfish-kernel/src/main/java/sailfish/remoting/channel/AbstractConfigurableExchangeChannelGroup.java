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
import java.util.concurrent.CountDownLatch;

import static sailfish.remoting.constants.ChannelAttrKeys.OneTime;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import sailfish.remoting.Address;
import sailfish.remoting.NettyPlatformIndependent;
import sailfish.remoting.codec.RemotingDecoder;
import sailfish.remoting.codec.RemotingEncoder;
import sailfish.remoting.configuration.NegotiateConfig;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.eventgroup.ClientEventGroup;
import sailfish.remoting.handler.HeartbeatChannelHandler;
import sailfish.remoting.handler.NegotiateChannelHandler;
import sailfish.remoting.handler.ConcreteRequestHandler;

/**
 * @author spccold
 * @version $Id: AbstractConfigurableExchangeChannelGroup.java, v 0.1 2016年11月23日 下午3:47:32 spccold
 *          Exp $
 */
public abstract class AbstractConfigurableExchangeChannelGroup extends AbstractExchangeChannelGroup {

	protected AbstractConfigurableExchangeChannelGroup() {
		super(UUID.randomUUID());
	}

	protected Bootstrap configureBoostrap(Address remoteAddress, int connectTimeout, NegotiateConfig config,
			ExchangeChannelGroup channelGroup, EventLoopGroup loopGroup, EventExecutorGroup executorGroup) {
		Bootstrap boot = newBootstrap();
		if (null == loopGroup) {
			loopGroup = ClientEventGroup.INSTANCE.getLoopGroup();
		}
		if (null == executorGroup) {
			executorGroup = ClientEventGroup.INSTANCE.getExecutorGroup();
		}
		boot.group(loopGroup);
		boot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
		boot.remoteAddress(remoteAddress.host(), remoteAddress.port());
		boot.handler(newChannelInitializer(config, channelGroup, executorGroup));
		return boot;
	}

	private Bootstrap newBootstrap() {
		Bootstrap boot = new Bootstrap();
		boot.channel(NettyPlatformIndependent.channelClass());
		boot.option(ChannelOption.TCP_NODELAY, true);
		// replace by heart beat
		boot.option(ChannelOption.SO_KEEPALIVE, false);
		// default is pooled direct
		// ByteBuf(io.netty.util.internal.PlatformDependent.DIRECT_BUFFER_PREFERRED)
		boot.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		// 32kb(for massive long connections, See
		// http://www.infoq.com/cn/articles/netty-million-level-push-service-design-points)
		// 64kb(RocketMq remoting default value)
		boot.option(ChannelOption.SO_SNDBUF, 32 * 1024);
		boot.option(ChannelOption.SO_RCVBUF, 32 * 1024);
		// temporary settings, need more tests
		boot.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024));
		//default is true, reduce thread context switching
		boot.option(ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP, true);
		return boot;
	}

	private ChannelInitializer<SocketChannel> newChannelInitializer(final NegotiateConfig config,
			final ExchangeChannelGroup channelGroup, final EventExecutorGroup executorGroup) {
		return new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				ch.attr(ChannelAttrKeys.maxIdleTimeout).set(config.maxIdleTimeout());
				ch.attr(ChannelAttrKeys.channelGroup).set(channelGroup);
				ch.attr(ChannelAttrKeys.clientSide).set(true);
				ch.attr(OneTime.awaitNegotiate).set(new CountDownLatch(1));
				ch.attr(OneTime.channelConfig).set(config);
				// TODO should increase ioRatio when every ChannelHandler bind to executorGroup?
				pipeline.addLast(executorGroup, 
						RemotingEncoder.INSTANCE, 
						new RemotingDecoder(), 
						new IdleStateHandler(config.idleTimeout(), 0, 0), 
						HeartbeatChannelHandler.INSTANCE,
						NegotiateChannelHandler.INSTANCE,
						ConcreteRequestHandler.INSTANCE);
			}
		};
	}
}
