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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.utils.ChannelUtil;
import sailfish.remoting.utils.RemotingUtils;

/**
 * @author spccold
 * @version $Id: HeartbeatHandler.java, v 0.1 2016年11月23日 下午9:21:35 spccold Exp $
 */
@ChannelHandler.Sharable
public class HeartbeatChannelHandler extends ChannelDuplexHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(HeartbeatChannelHandler.class);
	public static final HeartbeatChannelHandler INSTANCE = new HeartbeatChannelHandler();
	
	private HeartbeatChannelHandler() {}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
			ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).set(System.currentTimeMillis());
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		if (ctx.channel().isActive()) {
			ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).set(System.currentTimeMillis());
		}
		ctx.fireChannelRegistered();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).set(System.currentTimeMillis());
		ctx.fireChannelActive();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).set(System.currentTimeMillis());
		ctx.fireChannelReadComplete();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			int maxIdleTimeout = ctx.channel().attr(ChannelAttrKeys.maxIdleTimeout).get();
			long expireTime = System.currentTimeMillis() - ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).get();
			if (expireTime >= maxIdleTimeout * 1000) {
				logger.warn("readIdleTimeout exceed maxIdleTimeout, real timeout {}, this channel[{}] will be closed",
						expireTime, ctx.channel().toString());
				RemotingUtils.closeChannel(ctx.channel());
			} else if (ChannelUtil.clientSide(ctx)) {
				// send heart beat to remote peer
				ctx.writeAndFlush(RequestProtocol.newHeartbeat());
			}
		} else {
			ctx.fireUserEventTriggered(evt);
		}
	}
}
