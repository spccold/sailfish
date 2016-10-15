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
package sailfish.remoting.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import sailfish.remoting.protocol.Protocol;

/**
 * 
 * @author spccold
 * @version $Id: RemotingEncoder.java, v 0.1 2016年10月9日 下午9:44:43 jileng Exp $
 */
public class RemotingEncoder extends MessageToByteEncoder<Protocol>{
    @Override
    protected void encode(ChannelHandlerContext ctx, Protocol msg, ByteBuf out) throws Exception {
        DefaultRemotingCodec.INSTANCE.encode(msg, out);
    }
}
