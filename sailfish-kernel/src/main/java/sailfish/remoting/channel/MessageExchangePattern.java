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

import sailfish.remoting.RequestControl;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.ResponseFuture;

/**
 * 
 * <pre>
 * <a href="https://en.wikipedia.org/wiki/Messaging_pattern">Messaging_pattern</a>
 * <a href="https://en.wikipedia.org/wiki/Request%E2%80%93response">Request–response</a>
 * </pre>
 * 
 * @author spccold
 * @version $Id: MessageExchangePattern.java, v 0.1 2016年11月24日 下午5:45:03 spccold Exp $
 */
public interface MessageExchangePattern {
	
	/**
     * one-way pattern
     */
    void oneway(byte[] data, RequestControl requestControl) throws SailfishException;
    
    /**
     * request–response pattern
     */
    ResponseFuture<byte[]> request(byte[] data, RequestControl requestControl) throws SailfishException;
    
    /**
     * callback request via request–response pattern
     */
    void request(byte[] data, ResponseCallback<byte[]> callback, RequestControl requestControl) throws SailfishException;
}
