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

import java.net.SocketAddress;
import java.util.UUID;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import sailfish.remoting.RequestControl;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.BytesResponseFuture;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;

/**
 * @author spccold
 * @version $Id: AbstractExchangeChannel.java, v 0.1 2016年11月21日 下午10:49:12 spccold Exp $
 */
public abstract class AbstractExchangeChannel implements ExchangeChannel {
	/** underlying channel */
	protected volatile Channel channel;
	protected volatile boolean closed = false;
	
	private final ExchangeChannelGroup parent;
	protected AbstractExchangeChannel(ExchangeChannelGroup parent) {
		this.parent = parent;
	}

	@Override
	public ExchangeChannelGroup parent() {
		return parent;
	}

	@Override
	public ExchangeChannel next() {
		return this;
	}

	@Override
	public UUID id() {
		return null;
	}

	@Override
	public SocketAddress localAddress() {
		if(null == channel){
			return null;
		}
		return channel.localAddress();
	}

	@Override
	public SocketAddress remoteAdress() {
		if(null == channel){
			return null;
		}
		return channel.remoteAddress();
	}
	
	@Override
	public boolean isAvailable() {
		return null != channel && channel.isOpen() && channel.isActive();
	}

	@Override
	public void close() {
		close(0);
	}
	
	@Override
	public boolean isClosed() {
		return closed;
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

	
	@Override
	public void response(ResponseProtocol response) throws SailfishException{
		channel.writeAndFlush(response, channel.voidPromise());
	}

	private ResponseFuture<byte[]> requestWithFuture(byte[] data, ResponseCallback<byte[]> callback,
			RequestControl requestControl) throws SailfishException {
		final RequestProtocol protocol = RequestProtocol.newRequest(requestControl);
		protocol.oneway(false);
		protocol.body(data);

		ResponseFuture<byte[]> respFuture = new BytesResponseFuture(protocol.packetId(), getTracer());
		respFuture.setCallback(callback, requestControl.timeout());
		// trace before write
		getTracer().trace(this, protocol.packetId(), respFuture);
		try {
			if (requestControl.sent()) {
				ChannelFuture future = channel.writeAndFlush(protocol)
						.addListener(new SimpleChannelFutureListener(this, protocol.packetId()));
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

	// reduce class create
	static class SimpleChannelFutureListener implements ChannelFutureListener {
		private final ExchangeChannel channel;
		private final int packetId;

		public SimpleChannelFutureListener(ExchangeChannel channel, int packetId) {
			this.channel = channel;
			this.packetId = packetId;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (!future.isSuccess()) {
				// FIXME maybe need more concrete error, like
				// WriteOverFlowException or some other special exceptions
				channel.getTracer().erase(ResponseProtocol.newErrorResponse(packetId, 
						new SailfishException(ExceptionCode.CHANNEL_WRITE_FAIL, "write fail", future.cause())));
			}
		}
	}
}
