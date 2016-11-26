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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import sailfish.remoting.codec.RemotingDecoder;
import sailfish.remoting.codec.RemotingEncoder;
import sailfish.remoting.configuration.ExchangeServerConfig;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.eventgroup.ServerEventGroup;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.DefaultMsgHandler;
import sailfish.remoting.handler.HeartbeatChannelHandler;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.handler.NegotiateChannelHandler;
import sailfish.remoting.handler.ShareableSimpleChannelInboundHandler;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.utils.ParameterChecker;

/**
 * 
 * @author spccold
 * @version $Id: ExchangeServer.java, v 0.1 2016年10月26日 下午3:52:19 jileng Exp $
 */
public class ExchangeServer implements Endpoint {
	private volatile boolean isClosed = false;
	private final ExchangeServerConfig config;
	private final MsgHandler<Protocol> msgHandler;

	public ExchangeServer(ExchangeServerConfig config) {
		this.config = ParameterChecker.checkNotNull(config, "ExchangeServerConfig");
		this.msgHandler = new DefaultMsgHandler(config.getRequestProcessors());
	}

	public void start() throws SailfishException {
		ServerBootstrap boot = newServerBootstrap();
		EventLoopGroup accept = NettyPlatformIndependent.newEventLoopGroup(1,
				new DefaultThreadFactory(RemotingConstants.SERVER_ACCEPT_THREADNAME));
		if (null != config.getEventLoopGroup()) {
			boot.group(accept, config.getEventLoopGroup());
		} else {
			boot.group(accept, ServerEventGroup.INSTANCE.getLoopGroup());
		}
		final EventExecutorGroup executor = (null != config.getEventExecutorGroup() ? config.getEventExecutorGroup()
				: ServerEventGroup.INSTANCE.getExecutorGroup());
		boot.localAddress(config.address().host(), config.address().port());
		boot.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				ch.attr(ChannelAttrKeys.maxIdleTimeout).set(config.maxIdleTimeout());
				ch.attr(ChannelAttrKeys.OneTime.idleTimeout).set(config.idleTimeout());
				ch.attr(ChannelAttrKeys.exchangeServer).set(ExchangeServer.this);
				pipeline.addLast(executor, RemotingEncoder.INSTANCE);
				pipeline.addLast(executor, new RemotingDecoder());
				pipeline.addLast(executor, new IdleStateHandler(config.idleTimeout(), 0, 0));
				pipeline.addLast(executor, HeartbeatChannelHandler.INSTANCE);
				pipeline.addLast(executor, NegotiateChannelHandler.INSTANCE);
				pipeline.addLast(executor, ShareableSimpleChannelInboundHandler.INSTANCE);
			}
		});
		try {
			boot.bind().syncUninterruptibly();
		} catch (Throwable cause) {
			throw new SailfishException(cause);
		}
	}

	private ServerBootstrap newServerBootstrap() {
		ServerBootstrap serverBoot = new ServerBootstrap();
		serverBoot.channel(NettyPlatformIndependent.serverChannelClass());
		// connections wait for accept
		serverBoot.option(ChannelOption.SO_BACKLOG, 1024);
		serverBoot.option(ChannelOption.SO_REUSEADDR, true);
		// replace by heart beat
		serverBoot.childOption(ChannelOption.SO_KEEPALIVE, false);
		serverBoot.childOption(ChannelOption.TCP_NODELAY, true);
		serverBoot.childOption(ChannelOption.SO_SNDBUF, 32 * 1024);
		serverBoot.childOption(ChannelOption.SO_RCVBUF, 32 * 1024);
		// temporary settings, need more tests
		serverBoot.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024));
		serverBoot.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		serverBoot.childOption(ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP, false);
		return serverBoot;
	}

	@Override
	public void close() {
		close(0);
	}

	@Override
	public void close(int timeout) {
		synchronized (this) {
			if (isClosed)
				return;
			// TODO
		}
	}

	@Override
	public boolean isClosed() {
		return this.isClosed;
	}

	public MsgHandler<Protocol> getMsgHandler() {
		return msgHandler;
	}
}
