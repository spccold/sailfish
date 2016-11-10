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

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.utils.RemotingUtils;

/**
 * 
 * @author spccold
 * @version $Id: ChannelEventsHandler.java, v 0.1 2016年11月4日 下午2:25:38 jileng
 *          Exp $
 */
public class ChannelEventsHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChannelEventsHandler.class);
    private final boolean       clientSide;

    public ChannelEventsHandler(boolean clientSide) {
        this.clientSide = clientSide;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //record lastReadTimeMills
        ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).set(System.currentTimeMillis());
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).set(System.currentTimeMillis());
        super.channelActive(ctx);
        if (this.clientSide) {
            int idleTimeout = ctx.channel().attr(ChannelAttrKeys.idleTimeout).get();
            int maxIdleTimeout = ctx.channel().attr(ChannelAttrKeys.maxIdleTimeout).get();
            Attribute<UUID> uuidAttr = ctx.channel().attr(ChannelAttrKeys.uuid);
            if (null != uuidAttr.get()) {
                //one byte
                boolean writeChannel = ctx.channel().attr(ChannelAttrKeys.writeChannel).get();
                int channelIndex = ctx.channel().attr(ChannelAttrKeys.channelIndex).get();
                // negotiate idle timeout and read write splitting settings with remote peer
                ctx.writeAndFlush(RequestProtocol.newNegotiateHeartbeat((byte) idleTimeout, (byte) maxIdleTimeout,
                    uuidAttr.get(), writeChannel, channelIndex));
                return;
            }
            // negotiate idle timeout with remote peer
            ctx.writeAndFlush(
                RequestProtocol.newNegotiateHeartbeat((byte) idleTimeout, (byte) maxIdleTimeout, null, false, 0));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            int maxIdleTimeout = ctx.channel().attr(ChannelAttrKeys.maxIdleTimeout).get();
            long expireTime = System.currentTimeMillis() - ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).get();
            if (expireTime >= maxIdleTimeout * 1000) {
                logger.warn("readIdleTimeout exceed maxIdleTimeout, real timeout {}, this channel will be closed",
                    expireTime);
                RemotingUtils.closeChannel(ctx.channel());
            } else if (this.clientSide) {
                //send heart beat to remote peer
                ctx.writeAndFlush(RequestProtocol.newHeartbeat());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String msg = "exceptionCaught, localAddress [%s], remoteAddress [%s]";
        logger.warn(
            String.format(msg, ctx.channel().localAddress().toString(), ctx.channel().remoteAddress().toString()),
            cause);
        super.exceptionCaught(ctx, cause);
    }
}
