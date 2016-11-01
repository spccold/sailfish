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
package sailfish.remoting.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import sailfish.remoting.protocol.Protocol;

/**
 * 
 * @author spccold
 * @version $Id: ShareableSimpleChannelInboundHandler.java, v 0.1 2016年11月1日 下午2:17:59 jileng Exp $
 */
//FIXME should only single instance
@ChannelHandler.Sharable
public class ShareableSimpleChannelInboundHandler extends SimpleChannelInboundHandler<Protocol>{
    private final MsgHandler<Protocol> msgHandler;
    
    public ShareableSimpleChannelInboundHandler(MsgHandler<Protocol> msgHandler) {
        this.msgHandler = msgHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Protocol msg) throws Exception {
        msgHandler.handle(ctx, msg);
    }

}
