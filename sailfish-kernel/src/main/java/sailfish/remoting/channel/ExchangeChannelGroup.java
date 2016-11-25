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
     * Returns one of the {@link ExchangeChannel}s managed by this {@link ExchangeChannelGroup}.
     */
    ExchangeChannel next() throws SailfishException;

    /**
     * like {@link Channel#id()}
     */
    UUID id();
    
    boolean isAvailable();
    
    MsgHandler<Protocol> getMsgHander();
    
    Tracer getTracer();
}
