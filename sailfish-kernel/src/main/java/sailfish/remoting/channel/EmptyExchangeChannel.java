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
import sailfish.remoting.RequestControl;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.Tracer;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.protocol.ResponseProtocol;

/**
 * for test or something else
 * @author spccold
 * @version $Id: EmptyExchangeChannel.java, v 0.1 2016年11月25日 下午8:37:47 spccold Exp $
 */
public class EmptyExchangeChannel implements ExchangeChannel{

	@Override
	public UUID id() {
		return null;
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	public MsgHandler<Protocol> getMsgHander() {
		return null;
	}

	@Override
	public Tracer getTracer() {
		return null;
	}

	@Override
	public void close() {
	}

	@Override
	public void close(int timeout) {
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public void oneway(byte[] data, RequestControl requestControl) throws SailfishException {
	}

	@Override
	public ResponseFuture<byte[]> request(byte[] data, RequestControl requestControl) throws SailfishException {
		return null;
	}

	@Override
	public void request(byte[] data, ResponseCallback<byte[]> callback, RequestControl requestControl)
			throws SailfishException {
	}

	@Override
	public void response(ResponseProtocol response) throws SailfishException {
	}

	@Override
	public ExchangeChannel next() {
		return this;
	}

	@Override
	public ExchangeChannelGroup parent() {
		return null;
	}

	@Override
	public Channel update(Channel newChannel) {
		return null;
	}

	@Override
	public Channel doConnect() throws SailfishException {
		return null;
	}

	@Override
	public void recover() {
	}

	@Override
	public SocketAddress localAddress() {
		return null;
	}

	@Override
	public SocketAddress remoteAdress() {
		return null;
	}
}
