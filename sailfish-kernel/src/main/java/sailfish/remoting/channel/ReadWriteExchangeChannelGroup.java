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

import sailfish.remoting.Address;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.exceptions.SailfishException;

/**
 * @author spccold
 * @version $Id: ReadWriteExchangeChannelGroup.java, v 0.1 2016年11月22日 下午6:22:19
 *          spccold Exp $
 */
public class ReadWriteExchangeChannelGroup extends AbstractExchangeChannelGroup {
	private static final Logger logger = LoggerFactory.getLogger(ReadWriteExchangeChannelGroup.class);

	private final ExchangeChannelGroup readGroup;
	private final ExchangeChannelGroup writeGroup;

	public ReadWriteExchangeChannelGroup(Address address, boolean lazy, int readConnections, int writeConnections)
			throws SailfishException {
		this(address, RemotingConstants.DEFAULT_CONNECT_TIMEOUT, RemotingConstants.DEFAULT_RECONNECT_INTERVAL,
				RemotingConstants.DEFAULT_IDLE_TIMEOUT, RemotingConstants.DEFAULT_MAX_IDLE_TIMEOUT, lazy,
				readConnections, writeConnections);
	}

	public ReadWriteExchangeChannelGroup(Address address, int connectTimeout, int reconnectInterval, boolean lazy,
			int readConnections, int writeConnections) throws SailfishException {
		this(address, connectTimeout, reconnectInterval, RemotingConstants.DEFAULT_IDLE_TIMEOUT,
				RemotingConstants.DEFAULT_MAX_IDLE_TIMEOUT, lazy, readConnections, writeConnections);
	}

	public ReadWriteExchangeChannelGroup(Address address, boolean lazy, byte idleTimeout, byte maxIdleTimeOut,
			int readConnections, int writeConnections) throws SailfishException {
		this(address, RemotingConstants.DEFAULT_CONNECT_TIMEOUT, RemotingConstants.DEFAULT_RECONNECT_INTERVAL,
				idleTimeout, maxIdleTimeOut, lazy, readConnections, writeConnections);
	}

	public ReadWriteExchangeChannelGroup(Address address, int connectTimeout, int reconnectInterval, byte idleTimeout,
			byte maxIdleTimeOut, boolean lazy, int readConnections, int writeConnections) throws SailfishException {
		super(UUID.randomUUID());
		
		ChannelConfig readConfig = new ChannelConfig(id(), ChannelType.read.code(), (short)readConnections, (short)0);
		this.readGroup = new DefaultExchangeChannelGroup(address, readConnections, connectTimeout, reconnectInterval,
				idleTimeout, maxIdleTimeOut, lazy, readConfig);

		ChannelConfig writeConfig = new ChannelConfig(id(), ChannelType.write.code(), (short)readConnections, (short)0);
		this.writeGroup = new DefaultExchangeChannelGroup(address, writeConnections, connectTimeout, reconnectInterval,
				idleTimeout, maxIdleTimeOut, lazy, writeConfig);
	}

	@Override
	public ExchangeChannel next() throws SailfishException {
		try {
			return writeGroup.next();
		} catch (SailfishException cause) {
			logger.warn("writeGroup not available, try to choose readGroup", cause);
		}
		//try readGroup if writeGroup no available
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
}
