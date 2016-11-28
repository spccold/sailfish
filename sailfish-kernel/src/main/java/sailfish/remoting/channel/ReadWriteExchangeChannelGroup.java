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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import sailfish.remoting.Address;
import sailfish.remoting.Tracer;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;

/**
 * @author spccold
 * @version $Id: ReadWriteExchangeChannelGroup.java, v 0.1 2016年11月22日 下午6:22:19 spccold Exp $
 */
public class ReadWriteExchangeChannelGroup extends AbstractExchangeChannelGroup {
	private static final Logger logger = LoggerFactory.getLogger(ReadWriteExchangeChannelGroup.class);

	private final ExchangeChannelGroup readGroup;
	private final ExchangeChannelGroup writeGroup;

	private final MsgHandler<Protocol> msgHandler;
	private final Tracer tracer;

	public ReadWriteExchangeChannelGroup(MsgHandler<Protocol> msgHandler, Address address, int connectTimeout,
			int reconnectInterval, byte idleTimeout, byte maxIdleTimeOut, boolean lazy, short connections,
			short writeConnections, boolean reverseIndex, EventLoopGroup loopGroup, EventExecutorGroup executorGroup)
			throws SailfishException {
		super(UUID.randomUUID());
		this.msgHandler = msgHandler;
		this.tracer = new Tracer();

		ChannelConfig readConfig = new ChannelConfig(id(), ChannelType.read.code(), connections, writeConnections,
				(short) 0, reverseIndex);
		this.readGroup = new DefaultExchangeChannelGroup(tracer, msgHandler, address,
				(short) (connections - writeConnections), connectTimeout, reconnectInterval, idleTimeout,
				maxIdleTimeOut, lazy, reverseIndex, readConfig, this, loopGroup, executorGroup);

		ChannelConfig writeConfig = new ChannelConfig(id(), ChannelType.write.code(), connections, writeConnections,
				(short) 0, reverseIndex);
		this.writeGroup = new DefaultExchangeChannelGroup(tracer, msgHandler, address, writeConnections, connectTimeout,
				reconnectInterval, idleTimeout, maxIdleTimeOut, lazy, reverseIndex, writeConfig, this, loopGroup,
				executorGroup);
	}

	@Override
	public ExchangeChannel next() throws SailfishException {
		try {
			return writeGroup.next();
		} catch (SailfishException cause) {
			logger.warn("writeGroup not available, try to choose readGroup", cause);
		}
		// try readGroup if writeGroup no available
		return readGroup.next();
	}

	@Override
	public boolean isAvailable() {
		if (writeGroup.isAvailable()) {
			return true;
		}
		return readGroup.isAvailable();
	}

	@Override
	public void close(int timeout) {
		writeGroup.close(timeout);
		readGroup.close(timeout);
	}

	@Override
	public MsgHandler<Protocol> getMsgHander() {
		return msgHandler;
	}

	@Override
	public Tracer getTracer() {
		return tracer;
	}
}
