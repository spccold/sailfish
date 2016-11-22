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
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import sailfish.remoting.Address;
import sailfish.remoting.NettyPlatformIndependent;
import sailfish.remoting.ReconnectManager;
import sailfish.remoting.RequestControl;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.Tracer;
import sailfish.remoting.codec.RemotingDecoder;
import sailfish.remoting.codec.RemotingEncoder;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.eventgroup.ClientEventGroup;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.BytesResponseFuture;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.handler.ChannelEventsHandler;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.handler.ShareableSimpleChannelInboundHandler;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;
import sailfish.remoting.utils.StrUtils;

/**
 * @author spccold
 * @version $Id: SingleConnctionExchangeChannel.java, v 0.1 2016年11月21日
 *          下午11:05:58 spccold Exp $
 */
public abstract class SingleConnctionExchangeChannel extends AbstractExchangeChannel {
	private final int connectTimeout;

	private final int idleTimeout;
	private final int maxIdleTimeOut;
	private final ReadWriteChannelConfig config;
	/**
	 * Create a new instance
	 * 
	 * @param parent
	 *            the {@link ExchangeChannelGroup} which is the parent of this
	 *            instance and belongs to it
	 * @param address
	 *            the {@link Address} which be connected to
	 * @param doConnect
	 *            connect to remote peer or not when initial
	 */
	public SingleConnctionExchangeChannel(ExchangeChannelGroup parent, Address address, ReadWriteChannelConfig config, boolean doConnect)
			throws SailfishException {
		this(parent, address, RemotingConstants.DEFAULT_CONNECT_TIMEOUT, RemotingConstants.DEFAULT_RECONNECT_INTERVAL,
				RemotingConstants.DEFAULT_IDLE_TIMEOUT, RemotingConstants.DEFAULT_MAX_IDLE_TIMEOUT, config, doConnect);
	}

	/**
	 * Create a new instance
	 * 
	 * @param parent
	 *            the {@link ExchangeChannelGroup} which is the parent of this
	 *            instance and belongs to it
	 * @param address
	 *            the {@link Address} which be connected to
	 * @param connectTimeout
	 *            connect timeout in milliseconds
	 * @param reconnectInterval
	 *            reconnect interval in milliseconds for
	 *            {@link ReconnectManager}
	 * @param doConnect
	 *            connect to remote peer or not when initial
	 */
	public SingleConnctionExchangeChannel(ExchangeChannelGroup parent, Address address, int connectTimeout,
			int reconnectInterval, ReadWriteChannelConfig config, boolean doConnect) throws SailfishException {
		this(parent, address, connectTimeout, reconnectInterval, RemotingConstants.DEFAULT_IDLE_TIMEOUT,
				RemotingConstants.DEFAULT_MAX_IDLE_TIMEOUT, config, doConnect);
	}

	/**
	 * Create a new instance
	 * 
	 * @param parent
	 *            the {@link ExchangeChannelGroup} which is the parent of this
	 *            instance and belongs to it
	 * @param idleTimeout
	 *            idle timeout in seconds for {@link IdleStateHandler}
	 * @param maxIdleTimeOut
	 *            max idle timeout in seconds for {@link ChannelEventsHandler}
	 * @param address
	 *            the {@link Address} which be connected to
	 * @param doConnect
	 *            connect to remote peer or not when initial
	 */
	public SingleConnctionExchangeChannel(ExchangeChannelGroup parent, int idleTimeout, int maxIdleTimeOut,
			Address address, ReadWriteChannelConfig config, boolean doConnect) throws SailfishException {
		this(parent, address, RemotingConstants.DEFAULT_CONNECT_TIMEOUT, RemotingConstants.DEFAULT_RECONNECT_INTERVAL,
				idleTimeout, maxIdleTimeOut, config, doConnect);
	}

	/**
	 * Create a new instance
	 * 
	 * @param parent
	 *            the {@link ExchangeChannelGroup} which is the parent of this
	 *            instance and belongs to it
	 * @param address
	 *            the {@link Address} which be connected to
	 * @param connectTimeout
	 *            connect timeout in milliseconds
	 * @param reconnectInterval
	 *            reconnect interval in milliseconds for
	 *            {@link ReconnectManager}
	 * @param idleTimeout
	 *            idle timeout in seconds for {@link IdleStateHandler}
	 * @param maxIdleTimeOut
	 *            max idle timeout in seconds for {@link ChannelEventsHandler}
	 * @param doConnect
	 *            connect to remote peer or not when initial
	 */
	public SingleConnctionExchangeChannel(ExchangeChannelGroup parent, Address address, int connectTimeout,
			int reconnectInterval, int idleTimeout, int maxIdleTimeOut, ReadWriteChannelConfig config, boolean doConnect) throws SailfishException {
		super(parent, address, reconnectInterval);
		this.connectTimeout = connectTimeout;
		this.idleTimeout = idleTimeout;
		this.maxIdleTimeOut = maxIdleTimeOut;
		this.config = config;
		if (doConnect) {
			this.channel = doConnect();
		}
	}

	@Override
	public void oneway(byte[] data, RequestControl requestControl) throws SailfishException {
		RequestProtocol protocol = RequestProtocol.newRequest(requestControl);
		protocol.oneway(true);
		protocol.body(data);
		try {
			if (requestControl.sent()) {
				// TODO write or writeAndFlush?
				ChannelFuture future = channel.writeAndFlush(protocol);
				boolean ret = future.await(requestControl.timeout());
				if (!ret) {
					future.cancel(true);
					throw new SailfishException(ExceptionCode.TIMEOUT, "oneway request timeout");
				}
				return;
			}
			// reduce memory consumption
			channel.writeAndFlush(protocol, channel.voidPromise());
		} catch (InterruptedException cause) {
			throw new SailfishException(ExceptionCode.INTERRUPTED, "interrupted exceptions");
		}
	}

	@Override
	public ResponseFuture<byte[]> request(byte[] data, RequestControl requestControl) throws SailfishException {
		return requestWithFuture(data, null, requestControl);
	}

	@Override
	public void request(byte[] data, ResponseCallback<byte[]> callback, RequestControl requestControl)
			throws SailfishException {
		requestWithFuture(data, callback, requestControl);
	}

	private ResponseFuture<byte[]> requestWithFuture(byte[] data, ResponseCallback<byte[]> callback,
			RequestControl requestControl) throws SailfishException {
		final RequestProtocol protocol = RequestProtocol.newRequest(requestControl);
		protocol.oneway(false);
		protocol.body(data);

		ResponseFuture<byte[]> respFuture = new BytesResponseFuture(protocol.packetId());
		respFuture.setCallback(callback, requestControl.timeout());
		// trace before write
		Tracer.trace(this, protocol.packetId(), respFuture);
		try {
			if (requestControl.sent()) {
				ChannelFuture future = channel.writeAndFlush(protocol)
						.addListener(new SimpleChannelFutureListener(protocol.packetId()));
				boolean ret = future.await(requestControl.timeout());
				if (!ret) {
					future.cancel(true);
					throw new SailfishException(ExceptionCode.TIMEOUT, "oneway request timeout");
				}
				return respFuture;
			}
			channel.writeAndFlush(protocol, channel.voidPromise());
		} catch (InterruptedException cause) {
			throw new SailfishException(ExceptionCode.INTERRUPTED, "interrupted exceptions");
		}
		return respFuture;
	}

	@Override
	public Channel doConnect() throws SailfishException {
		MsgHandler<Protocol> handler = new MsgHandler<Protocol>() {
			@Override
			public void handle(ChannelHandlerContext context, Protocol msg) {
				if (msg.request()) {
					// TODO
				} else {
					Tracer.erase((ResponseProtocol) msg);
				}
			}
		};
		Bootstrap boot = configureBoostrap(handler);
		try {
			Channel channel = boot.connect().syncUninterruptibly().channel();
			this.localAddress = channel.localAddress();
			return channel;
		} catch (Throwable cause) {
			throw new SailfishException(cause);
		}
	}

	private Bootstrap configureBoostrap(final MsgHandler<Protocol> handler) {
		Bootstrap boot = newBootstrap();
		boot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
		boot.remoteAddress(((Address)remoteAddress).host(), ((Address)remoteAddress).port());
		boot.group(ClientEventGroup.INSTANCE.getLoopGroup());
		boot.handler(newChannelInitializer(handler));
		return boot;
	}

	protected ChannelInitializer<SocketChannel> newChannelInitializer(final MsgHandler<Protocol> handler) {
		final EventExecutorGroup executorGroup = ClientEventGroup.INSTANCE.getExecutorGroup();
		return new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				ch.attr(ChannelAttrKeys.idleTimeout).set(idleTimeout);
				ch.attr(ChannelAttrKeys.maxIdleTimeout).set(maxIdleTimeOut);
				if (null != config) {
					ch.attr(ChannelAttrKeys.writeChannel).set(config.write());
					ch.attr(ChannelAttrKeys.channelIndex).set(config.index());
					ch.attr(ChannelAttrKeys.uuid).set(config.uuid());
				}
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

	@Override
	public boolean isAvailable() {
		boolean isAvailable = false;
		if (!reconnectting && !(isAvailable = underlyingAvailable())) {
			synchronized (this) {
				if (!reconnectting && !(isAvailable = underlyingAvailable())) {
					recover();
					this.reconnectting = true;
				}
			}
		}
		return isAvailable;
	}

	private boolean underlyingAvailable() {
		return null != channel && channel.isOpen() && channel.isActive();
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
		// 32kb(for massive long connections, suggests from
		// http://www.infoq.com/cn/articles/netty-million-level-push-service-design-points)
		// 64kb(RocketMq remoting default value)
		boot.option(ChannelOption.SO_SNDBUF, 32 * 1024);
		boot.option(ChannelOption.SO_RCVBUF, 32 * 1024);
		// temporary settings, need more tests
		// TODO what's happen if exceed high water without channel.isWritable?
		boot.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024));
		boot.option(ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP, false);
		return boot;
	}

	// reduce class create
	static class SimpleChannelFutureListener implements ChannelFutureListener {
		private int packetId;

		public SimpleChannelFutureListener(int packetId) {
			this.packetId = packetId;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (!future.isSuccess()) {
				String errorMsg = "write fail!";
				if (null != future.cause()) {
					errorMsg = StrUtils.exception2String(future.cause());
				}
				// FIXME maybe need more concrete error, like
				// WriteOverFlowException or some other special exceptions
				Tracer.erase(ResponseProtocol.newErrorResponse(packetId, errorMsg, RemotingConstants.RESULT_FAIL));
			}
		}
	}
}
