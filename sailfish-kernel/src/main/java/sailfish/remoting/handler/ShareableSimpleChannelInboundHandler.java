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
import io.netty.handler.timeout.IdleStateHandler;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.constants.Opcode;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;

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
        if(msg.request() && msg.heartbeat()){//deal heart beat request
            RequestProtocol requestProtocol = (RequestProtocol)msg;
            if(requestProtocol.opcode() == Opcode.HEARTBEAT_WITH_NEGOTIATE){//negotiate idle timeout
                int idleTimeout = requestProtocol.body()[0];
                int idleMaxTimeout = requestProtocol.body()[1];
                IdleStateHandler old = ctx.pipeline().get(IdleStateHandler.class);
                if(null != old){
                    ctx.pipeline().replace(IdleStateHandler.class, "idleStateHandler", new IdleStateHandler(idleTimeout, 0, 0));
                    ctx.channel().attr(ChannelAttrKeys.maxIdleTimeout).set(idleMaxTimeout);
                }
            }
            ctx.writeAndFlush(ResponseProtocol.newHeartbeat());
            return;
        }
        msgHandler.handle(ctx, msg);
    }

}
