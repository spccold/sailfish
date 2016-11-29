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
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import sailfish.remoting.Address;
import sailfish.remoting.Tracer;
import sailfish.remoting.configuration.NegotiateConfig;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;

/**
 * @author spccold
 * @version $Id: MultiConnectionsExchangeChannelGroup.java, v 0.1 2016年11月22日 下午4:01:53 spccold Exp
 *          $
 */
public abstract class MultiConnectionsExchangeChannelGroup extends AbstractConfigurableExchangeChannelGroup {

	private final ExchangeChannel[] children;
	private final ExchangeChannel[] deadChildren;
	private final ExchangeChannelChooserFactory.ExchangeChannelChooser chooser;
	private final MsgHandler<Protocol> msgHandler;
	private final Tracer tracer;

	protected MultiConnectionsExchangeChannelGroup(Tracer tracer, MsgHandler<Protocol> msgHandler, Address address,
			short connections, int connectTimeout, int reconnectInterval, byte idleTimeout, byte maxIdleTimeOut,
			boolean lazy, boolean reverseIndex, NegotiateConfig config, ExchangeChannelGroup parentGroup,
			EventLoopGroup loopGroup, EventExecutorGroup executorGroup) throws SailfishException {

		this.tracer = tracer;
		this.msgHandler = msgHandler;

		children = new ExchangeChannel[connections];
		deadChildren = new ExchangeChannel[connections];

		if (null == config) {
			config = new NegotiateConfig(idleTimeout, maxIdleTimeOut, id(), ChannelType.readwrite.code(),
					(short) connections, (short) connections, (short) 0, reverseIndex);
		}

		Bootstrap bootstrap = null;
		for (short i = 0; i < connections; i++) {
			boolean success = false;
			final NegotiateConfig deepCopy = config.deepCopy().index(i);
			parentGroup = (null == parentGroup ? this : parentGroup);
			bootstrap = configureBoostrap(address, connectTimeout, deepCopy, parentGroup, loopGroup, executorGroup);
			try {
				children[i] = newChild(parentGroup, bootstrap, reconnectInterval, lazy, deepCopy.isRead());
				success = true;
			} catch (SailfishException cause) {
				throw cause;
			} finally {
				if (!success) {
					close(Integer.MAX_VALUE);
				}
			}
		}

		chooser = DefaultExchangeChannelChooserFactory.INSTANCE.newChooser(children, deadChildren);
	}

	@Override
	public ExchangeChannel next() throws SailfishException {
		return chooser.next();
	}

	/**
	 * Return the number of {@link ExchangeChannel} this implementation uses. This number is the
	 * maps 1:1 to the connections it use.
	 */
	public final int channelOCount() {
		return children.length;
	}

	public void close(int timeout) {
		if (this.isClosed()) {
			return;
		}
		synchronized (this) {
			if (this.isClosed()) {
				return;
			}
			this.closed = true;
			for (int i = 0; i < children.length; i++) {
				deadChildren[i] = null;
				if (null != children[i]) {
					children[i].close(timeout);
				}
			}
		}
	}

	@Override
	public boolean isAvailable() {
		if (this.isClosed()) {
			return false;
		}

		if (children.length == 1) {// one connection check
			return (null != children[0] && children[0].isAvailable());
		}

		// can hit most of the time
		if (deadChildren[0] == null || deadChildren[0].isAvailable()) {
			return true;
		}

		for (int i = 1; i < children.length; i++) {
			if (deadChildren[i] == null || deadChildren[i].isAvailable()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public MsgHandler<Protocol> getMsgHander() {
		return msgHandler;
	}

	@Override
	public Tracer getTracer() {
		return tracer;
	}

	/**
	 * Create a new {@link ExchangeChannel} which will later then accessible via the {@link #next()}
	 * method.
	 */
	protected abstract ExchangeChannel newChild(ExchangeChannelGroup parent, Bootstrap bootstrap, int reconnectInterval,
			boolean lazy, boolean readChannel) throws SailfishException;
}
