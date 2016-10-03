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

import io.netty.channel.ChannelHandler;

/**
 * <a href="https://en.wikipedia.org/wiki/Transport_layer">Transport_layer</a>
 * 
 * @author spccold
 * @version $Id: Transporter.java, v 0.1 2016年10月3日 下午1:06:16 jileng Exp $
 */
public interface Transporter {
    public void connect(URL url, ChannelHandler handler);
    public void transfer(byte[] data);
}
