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
package org.netty.demos.events;

import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * 
 * @author spccold
 * @version $Id: ChannelEventsHandler.java, v 0.1 2016年11月3日 下午5:20:54 jileng Exp $
 */
public class ChannelEventsHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChannelEventsHandler.class);

    private String              side;
    private boolean             show;

    public ChannelEventsHandler(String side, boolean show) {
        this.side = side;
        this.show = show;
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise future) throws Exception {
        if (show) {
            logger.error("[side: {}]bind occur......", this.side);
        }
        super.bind(ctx, localAddress, future);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                        ChannelPromise future) throws Exception {
        if (show) {
            logger.error("[side: {}]connect occur......", this.side);
        }
        super.connect(ctx, remoteAddress, localAddress, future);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        if (show) {
            logger.error("[side: {}]disconnect occur......", this.side);
        }
        super.disconnect(ctx, future);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        if (show) {
            logger.error("[side: {}]close occur......", this.side);
        }
        super.close(ctx, future);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        if (show) {
            logger.error("[side: {}]deregister occur......", this.side);
        }
        super.deregister(ctx, future);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        if (show) {
            logger.error("[side: {}]read occur......", this.side);
        }
        super.read(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (show) {
            logger.error("[side: {}]write occur......", this.side);
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        if (show) {
            logger.error("[side: {}]flush occur......", this.side);
        }
        super.flush(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (show) {
            logger.error("[side: {}]channelRegistered occur......", this.side);
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (show) {
            logger.error("[side: {}]channelUnregistered occur......", this.side);
        }
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (show) {
            logger.error("[side: {}]channelActive occur......", this.side);
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (show) {
            logger.error("[side: {}]channelInactive occur......", this.side);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (show) {
            logger.error("[side: {}]channelRead occur......", this.side);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (show) {
            logger.error("[side: {}]channelReadComplete occur......", this.side);
        }
        super.channelReadComplete(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (show) {
            logger.error("[side: {}]userEventTriggered occur......", this.side);
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (show) {
            logger.error("[side: {}]channelWritabilityChanged occur......", this.side);
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (show) {
            logger.error("[side: {}]exceptionCaught occur......", this.side);
        }
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (show) {
            logger.error("[side: {}]handlerAdded occur......", this.side);
        }
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (show) {
            logger.error("[side: {}]handlerRemoved occur......", this.side);
        }
        super.handlerRemoved(ctx);
    }
}
