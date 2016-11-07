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

import sailfish.remoting.Endpoint;
import sailfish.remoting.RequestControl;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.ResponseFuture;

/**
 * communication channel for exchange client
 * <p>
 *  <a href="https://en.wikipedia.org/wiki/Messaging_pattern">Messaging_pattern</a>
 *  <a href="https://en.wikipedia.org/wiki/Request%E2%80%93response">Request–response</a>
 * </p>
 * @author spccold
 * @version $Id: ExchangeChannel.java, v 0.1 2016年10月26日 下午8:34:37 jileng Exp $
 */
public interface ExchangeChannel extends Endpoint{
    boolean isAvailable();
    
    /**
     * one-way pattern
     */
    void oneway(byte[] data, RequestControl requestControl) throws SailfishException;
    
    /**
     * request–response pattern
     */
    ResponseFuture<byte[]> request(byte[] data, RequestControl requestControl) throws SailfishException;
}
