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
		if (null == channel) {
			return null;
		}
		return channel.localAddress();
	}

	@Override
	public SocketAddress remoteAdress() {
		if (null == channel) {
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
		
		if(requestControl.preferHighPerformanceWriter()){
			HighPerformanceChannelWriter.write(channel, protocol);
			return;
		}
		
		if (requestControl.sent() && requestControl.timeout() > 0) {
			ChannelFuture future = channel.writeAndFlush(protocol);
			waitWriteDone(future, requestControl.timeout(), protocol, false);
			return;
		}
		// reduce memory consumption
		channel.writeAndFlush(protocol, channel.voidPromise());
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
	public void response(ResponseProtocol response) throws SailfishException {
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
		
		if(requestControl.preferHighPerformanceWriter()){
			HighPerformanceChannelWriter.write(channel, protocol);
			return respFuture;
		}
		
		if (requestControl.sent()) {
			ChannelFuture future = channel.writeAndFlush(protocol);
			waitWriteDone(future, requestControl.timeout(), protocol, true);
			return respFuture;
		}

		channel.writeAndFlush(protocol, channel.voidPromise());
		return respFuture;
	}
	
	private void waitWriteDone(ChannelFuture future, int timeout, RequestProtocol request, boolean needRemoveTrace)
			throws SailfishException {
		boolean done = future.awaitUninterruptibly(timeout);
		if (!done) {
			// useless at most of time when do writeAndFlush(...) invoke
			future.cancel(true);
			if (needRemoveTrace) {
				getTracer().remove(request.packetId());
			}
			throw new SailfishException(ExceptionCode.WRITE_TIMEOUT,
					String.format("write to remote[%s] timeout, protocol[%s]", channel.remoteAddress(), request));
		}
		if (!future.isSuccess()) {
			if (needRemoveTrace) {
				getTracer().remove(request.packetId());
			}
			throw new SailfishException(ExceptionCode.CHANNEL_WRITE_FAIL,
					String.format("write to remote[%s] fail, protocol[%s]", channel.remoteAddress(), request),
					future.cause());
		}
	}
}
