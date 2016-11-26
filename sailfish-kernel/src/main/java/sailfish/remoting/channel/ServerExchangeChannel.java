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

import io.netty.channel.Channel;
import sailfish.remoting.Tracer;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;

/**
 * @author spccold
 * @version $Id: ServerExchangeChannel.java, v 0.1 2016年11月24日 下午5:49:52 spccold Exp $
 */
public final class ServerExchangeChannel extends AbstractExchangeChannel {

	public ServerExchangeChannel(ExchangeChannelGroup parent, Channel channel) {
		super(parent);
		this.channel = channel;
	}

	@Override
	public Channel update(Channel newChannel) {
		Channel old = channel;
		this.channel = newChannel;
		return old;
	}

	@Override
	public Channel doConnect() throws SailfishException {
		throw new UnsupportedOperationException("doConnect");
	}

	@Override
	public void recover() {
		throw new UnsupportedOperationException("recover");
	}
	
	@Override
	public void close(int timeout) {
		throw new UnsupportedOperationException("close with timeout: "+timeout);
	}

	@Override
	public MsgHandler<Protocol> getMsgHander() {
		return parent().getMsgHander();
	}

	@Override
	public Tracer getTracer() {
		return parent().getTracer();
	}
}
