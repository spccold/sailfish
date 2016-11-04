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

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import sailfish.remoting.protocol.RequestProtocol;

/**
 * 
 * @author spccold
 * @version $Id: ChannelEventsHandler.java, v 0.1 2016年11月4日 下午2:25:38 jileng Exp $
 */
public class ChannelEventsHandler extends ChannelDuplexHandler{
    private final boolean clientSide;

    public ChannelEventsHandler(boolean clientSide) {
        this.clientSide = clientSide;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if(this.clientSide){
            int idleTimeout = ctx.channel().attr(new AttributeKey<Integer>("idleTimeout")).get();
            int maxIdleTimeout = ctx.channel().attr(new AttributeKey<Integer>("maxIdleTimeout")).get();
            IdleStateHandler idleStateHandler = ctx.pipeline().get(IdleStateHandler.class);
            if(null != idleStateHandler){
                //negotiate idle timeout with remote peer
                ctx.writeAndFlush(RequestProtocol.newNegotiateHeartbeat((byte)idleTimeout, (byte)maxIdleTimeout));
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }
}
