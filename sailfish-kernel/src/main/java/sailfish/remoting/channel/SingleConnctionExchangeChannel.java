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
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.timeout.IdleStateHandler;
import sailfish.remoting.Address;
import sailfish.remoting.ReconnectManager;
import sailfish.remoting.RequestControl;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.Tracer;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.BytesResponseFuture;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;
import sailfish.remoting.utils.StrUtils;

/**
 * @author spccold
 * @version $Id: SingleConnctionExchangeChannel.java, v 0.1 2016年11月21日 下午11:05:58 spccold Exp $
 */
public abstract class SingleConnctionExchangeChannel extends AbstractExchangeChannel {
	private final Bootstrap reusedBootstrap;

	/**
	 * Create a new instance
	 * 
	 * @param parent    			the {@link ExchangeChannelGroup} which is the parent of this instance and belongs to it
	 * @param address				the {@link Address} which be connected to
	 * @param connectTimeout 		connect timeout in milliseconds
	 * @param reconnectInterval 	reconnect interval in milliseconds for {@link ReconnectManager}
	 * @param idleTimeout idle  	timeout in seconds for {@link IdleStateHandler}
	 * @param maxIdleTimeOut 		max idle timeout in seconds for {@link ChannelEventsHandler}
	 * @param doConnect				connect to remote peer or not when initial
	 */
	protected SingleConnctionExchangeChannel(Bootstrap bootstrap, ExchangeChannelGroup parent, Address address,
			int reconnectInterval, boolean doConnect) throws SailfishException {
		super(parent, address, reconnectInterval);
		this.reusedBootstrap = bootstrap;
		if (doConnect) {
			this.channel = doConnect();
		}
	}
	
	@Override
	public Channel doConnect() throws SailfishException {
		try {
			Channel channel = reusedBootstrap.connect().syncUninterruptibly().channel();
			this.localAddress = channel.localAddress();
			return channel;
		} catch (Throwable cause) {
			throw new SailfishException(cause);
		}
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

	private boolean underlyingAvailable() {
		return null != channel && channel.isOpen() && channel.isActive();
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
