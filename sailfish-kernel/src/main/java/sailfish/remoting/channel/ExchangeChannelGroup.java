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

import io.netty.channel.Channel;
import sailfish.remoting.Endpoint;
import sailfish.remoting.Tracer;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.protocol.ResponseProtocol;

/**
 * 
 * The {@link ExchangeChannelGroup} is responsible for providing the {@link ExchangeChannel}'s to use
 * via its {@link #next()} method. 
 * 
 * @author spccold
 * @version $Id: ExchangeChannelGroup.java, v 0.1 2016年11月21日 下午2:18:34 spccold
 *          Exp $
 */
public interface ExchangeChannelGroup extends Endpoint, MessageExchangePattern{
	/**
	 * like {@link Channel#id()}, Returns the globally unique identifier of this {@link ExchangeChannelGroup}.
	 */
	UUID id();

	/**
	 * Return {@code true} if this {@link ExchangeChannelGroup} is available, this means that the {@link ExchangeChannelGroup}
	 * can receive bytes from remote peer or write bytes to remote peer
	 */
	boolean isAvailable();

	/**
     * Returns one of the {@link ExchangeChannel}s managed by this {@link ExchangeChannelGroup}.
     */
    ExchangeChannel next() throws SailfishException;
    
    /**
     * Return the {@link MsgHandler} of this {@link ExchangeChannelGroup} which used for process {@link ResponseProtocol} 
     * sent by the {@link ExchangeChannelGroup}
     */
    MsgHandler<Protocol> getMsgHander();
    
    /**
     * Return the {@link Tracer} of this {@link ExchangeChannelGroup} which used for trace {@link ResponseProtocol}
     * sent by the {@link ExchangeChannelGroup}
     * @return
     */
    Tracer getTracer();
}
