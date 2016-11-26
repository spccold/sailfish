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

import sailfish.remoting.Address;
import sailfish.remoting.Tracer;
import sailfish.remoting.channel.ExchangeChannelGroup;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;

/**
 * @author spccold
 * @version $Id: ExchangeChannelGroupConfig.java, v 0.1 2016年11月26日 下午10:54:27 spccold Exp $
 */
public class ExchangeChannelGroupConfig {
	private MsgHandler<Protocol> msgHandler;
	private Tracer tracer;
	private Address remoteAddress;
	private int connectTimeout;
	private int reconnectInterval;
	private byte idleTimeout;
	private byte maxIdleTimeout;
	private boolean lazy;
	private short connections;
	private short writeConnections;
	private boolean reverseIndex;
	private ExchangeChannelGroup parent;
}
