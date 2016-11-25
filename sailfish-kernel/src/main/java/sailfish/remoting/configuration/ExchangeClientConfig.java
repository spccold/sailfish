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
package sailfish.remoting.configuration;

import sailfish.remoting.channel.ChannelGroupMode;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.utils.ParameterChecker;

/**
 * 
 * @author spccold
 * @version $Id: ExchangeClientConfig.java, v 0.1 2016年10月26日 下午10:55:22 jileng Exp $
 */
public class ExchangeClientConfig extends AbstractExchangeConfig {
	// in milliseconds
	private int connectTimeout = RemotingConstants.DEFAULT_CONNECT_TIMEOUT;
	private int reconnectInterval = RemotingConstants.DEFAULT_RECONNECT_INTERVAL;

	private short connections = 1;
	// enable channels Read/Write Splitting or not when connections greater than one
	private boolean enableReadWriteSplitting = false;
	/**
	 * write channels ratio when enableReadWriteSplitting is true, take writeConnections first if
	 * you don't specify writeConnections, writeRatio will be used
	 */
	private int writeRatio = 50;
	private short writeConnections;
	private boolean lazyConnection = false;

	public ChannelGroupMode mode() {
		if (connections == 1) {
			return ChannelGroupMode.simple;
		}
		if (enableReadWriteSplitting) {
			return ChannelGroupMode.readwrite;
		}
		return ChannelGroupMode.multiconns;
	}

	@Override
	public void check() {
		super.check();
		if (enableReadWriteSplitting) {
			if (connections <= 1) {
				throw new IllegalArgumentException(
						"connections must greater than one when enableReadWriteSplitting is true");
			}
			if (writeConnections == 0) {
				writeConnections = (short)(connections * (writeRatio / 100));
			}
			if (writeConnections == 0 || writeConnections == connections) {
				throw new IllegalArgumentException("writeConnections:" + writeConnections
						+ ", you should specify an appropriate writeConnections or writeRatio");
			}
		}
	}

	public short connections() {
		return connections;
	}

	public void connections(short connections) {
		this.connections = ParameterChecker.checkPositive(connections, "connections");
	}

	public short writeConnections() {
		return writeConnections;
	}

	public void writeConnections(short writeConnections) {
		this.writeConnections = ParameterChecker.checkPositive(writeConnections, "writeConnections");
	}

	public int reconnectInterval() {
		return reconnectInterval;
	}

	public void reconnectInterval(int reconnectInterval) {
		this.reconnectInterval = ParameterChecker.checkPositive(reconnectInterval, "reconnectInterval");
	}

	public int connectTimeout() {
		return connectTimeout;
	}

	public void connectTimeout(int connectTimeout) {
		this.connectTimeout = ParameterChecker.checkPositive(connectTimeout, "connectTimeout");
	}

	public boolean enableReadWriteSplitting() {
		return enableReadWriteSplitting;
	}

	public void enableReadWriteSplitting(boolean enableReadWriteSplitting) {
		this.enableReadWriteSplitting = enableReadWriteSplitting;
	}

	public int writeRatio() {
		return writeRatio;
	}

	public void writeRatio(int writeRatio) {
		if (writeRatio <= 0 || writeRatio > 100) {
			throw new IllegalArgumentException("writeRatio: " + writeRatio + " (expected: 0 < writeRatio <= 100)");
		}
		this.writeRatio = writeRatio;
	}

	public boolean isLazyConnection() {
		return lazyConnection;
	}

	public void setLazyConnection(boolean lazyConnection) {
		this.lazyConnection = lazyConnection;
	}
}
