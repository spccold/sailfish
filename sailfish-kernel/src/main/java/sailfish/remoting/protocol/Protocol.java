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
package sailfish.remoting.protocol;

import io.netty.buffer.ByteBuf;
import sailfish.remoting.exceptions.SailfishException;

/**
 * 
 * @author spccold
 * @version $Id: Protocol.java, v 0.1 2016年10月4日 下午3:01:29 jileng Exp $
 */
public interface Protocol {
    /**
     * request or response direction
     */
    boolean request();
    
    /**
     * heartbeat request/response
     */
    boolean heartbeat();

    /**
     * serialize bytes data to channel
     */
    void serialize(ByteBuf output) throws SailfishException;
    
    /**
     * deserialize bytes data from channel
     */
    void deserialize(ByteBuf input, int totalLength) throws SailfishException;
}
