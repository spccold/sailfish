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
package sailfish.remoting.channel2;

import io.netty.channel.Channel;
import sailfish.remoting.exceptions.SailfishException;

/**
 * <pre>
 * <a href="https://en.wikipedia.org/wiki/Messaging_pattern">Messaging_pattern</a>
 * <a href="https://en.wikipedia.org/wiki/Request%E2%80%93response">Request–response</a>
 * </pre>
 * 
 * @author spccold
 * @version $Id: ExchangeChannel.java, v 0.1 2016年11月21日 下午7:26:12 spccold Exp $
 */
public interface ExchangeChannel extends ExchangeChannelGroup{
	/**
     * Returns a reference to itself.
     */
    @Override
    ExchangeChannel next();

    /**
     * Return the {@link ExchangeChannelGroup} which is the parent of this {@link ExchangeChannel},
     */
    ExchangeChannelGroup parent();
    
    /**
     * update this {@link ExchangeChannel} underlying {@link Channel}
     * @param newChannel
     * @return old {@link Channel}
     */
    Channel update(Channel newChannel);
    
    /**
     * connect to remote peer 
     * @return
     * @throws SailfishException
     */
    Channel doConnect() throws SailfishException;
}
