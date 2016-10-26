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
package sailfish.remoting;

/**
 * <a href="https://en.wikipedia.org/wiki/Messaging_pattern">Messaging_pattern</a>
 * <a href="https://en.wikipedia.org/wiki/Request%E2%80%93response">Request–response</a>
 * 
 * @author spccold
 * @version $Id: Exchanger.java, v 0.1 2016年10月3日 下午1:04:04 jileng Exp $
 */
public interface Exchanger extends Endpoint{
    void oneway(byte[] data);
    ResponseFuture<byte[]> request(byte[] data);
    ResponseFuture<byte[]> request(byte[] data, int timeout);
}   
