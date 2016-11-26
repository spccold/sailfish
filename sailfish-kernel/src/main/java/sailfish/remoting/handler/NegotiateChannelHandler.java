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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.PlatformDependent;
import sailfish.remoting.ExchangeServer;
import sailfish.remoting.channel.ChannelConfig;
import sailfish.remoting.channel.ChannelType;
import sailfish.remoting.channel.ExchangeChannelGroup;
import sailfish.remoting.channel.ReadWriteServerExchangeChannelGroup;
import sailfish.remoting.channel.ReferenceCountedServerExchangeChannelGroup;
import sailfish.remoting.channel.ServerExchangeChannel;
import sailfish.remoting.channel.ServerExchangeChannelGroup;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.constants.ChannelAttrKeys.OneTime;
import sailfish.remoting.constants.Opcode;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;
import sailfish.remoting.utils.ArrayUtils;
import sailfish.remoting.utils.ChannelUtil;

/**
 * negotiate idleTimeout, maxIdleTimeout and settings about {@link ChannelConfig} with remote peer
 * 
 * @author spccold
 * @version $Id: NegotiateChannelHandler.java, v 0.1 2016年11月23日 下午10:11:26 spccold Exp $
 */
@ChannelHandler.Sharable
public class NegotiateChannelHandler extends SimpleChannelInboundHandler<Protocol> {

	private static final Logger logger = LoggerFactory.getLogger(NegotiateChannelHandler.class);
	public static final NegotiateChannelHandler INSTANCE = new NegotiateChannelHandler();
	// This way we can reduce the memory usage compared to use Attributes.
	private final ConcurrentMap<ChannelHandlerContext, Boolean> negotiateMap = PlatformDependent.newConcurrentHashMap();

	public static final ConcurrentMap<String, ExchangeChannelGroup> uuid2ChannelGroup = new ConcurrentHashMap<>();

	private NegotiateChannelHandler() { };

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (ChannelUtil.clientSide(ctx)) {
			negotiate(ctx);
		}
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if(!ChannelUtil.clientSide(ctx)){
			String uuidStr = ctx.channel().attr(ChannelAttrKeys.uuidStr).get();
			synchronized (uuidStr.intern()) {
				ReferenceCountedServerExchangeChannelGroup channelGroup = (ReferenceCountedServerExchangeChannelGroup)uuid2ChannelGroup.get(uuidStr);
				if(null != channelGroup){
					channelGroup.release();
				}
			}
		}
		ctx.fireChannelInactive();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Protocol msg) throws Exception {
		if (!ChannelUtil.clientSide(ctx) && msg.request() && msg.heartbeat()) {
			dealNegotiate(ctx, msg);
			return;
		}
		// no sense to Protocol in fact
		ReferenceCountUtil.retain(msg);
		ctx.fireChannelRead(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.warn("Failed to negotiate. Closing: " + ctx.channel(), cause);
		ctx.close();
	}

	private boolean negotiate(ChannelHandlerContext ctx) throws Exception {
		if (negotiateMap.putIfAbsent(ctx, Boolean.TRUE) == null) { // Guard against re-entrance.
			ctx.channel().attr(OneTime.awaitNegotiate).get().countDown();
			try {
				int idleTimeout = ctx.channel().attr(OneTime.idleTimeout).get();
				int maxIdleTimeout = ctx.channel().attr(ChannelAttrKeys.maxIdleTimeout).get();
				Attribute<UUID> uuidAttr = ctx.channel().attr(OneTime.uuid);
				byte channelType = ctx.channel().attr(OneTime.channelType).get();
				short connections = ctx.channel().attr(OneTime.connections).get();
				short writeConnections = ctx.channel().attr(OneTime.writeConnections).get();
				short channelIndex = ctx.channel().attr(OneTime.channelIndex).get();
				boolean reverseIndex = ctx.channel().attr(OneTime.reverseIndex).get();
				// negotiate idle timeout and read write splitting settings with remote peer
				ctx.writeAndFlush(RequestProtocol.newNegotiateHeartbeat((byte) idleTimeout, (byte) maxIdleTimeout,
						uuidAttr.get(), channelType, connections, writeConnections, channelIndex, reverseIndex));
			} catch (Throwable cause) {
				exceptionCaught(ctx, cause);
			} finally {
				remove(ctx);
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private void dealNegotiate(ChannelHandlerContext ctx, Protocol msg) throws Exception {
		try {
			RequestProtocol requestProtocol = (RequestProtocol) msg;
			if (requestProtocol.opcode() == Opcode.HEARTBEAT_WITH_NEGOTIATE) {
				byte[] body = requestProtocol.body();
				DataInputStream dis = new DataInputStream(new ByteArrayInputStream(body));
				byte idleTimeout = dis.readByte();
				byte idleMaxTimeout = dis.readByte();
				UUID uuid = new UUID(dis.readLong(), dis.readLong());
				byte channelType = dis.readByte();
				short connections = dis.readShort();
				short writeConnections = dis.readShort();
				short channelIndex = dis.readShort();
				boolean reverseIndex = dis.readBoolean();
				// no sense to dis(ByteArrayInputStream) in fact
				dis.close();

				// the client side shall prevail
				byte serverSideIdleTimeout = ctx.channel().attr(OneTime.idleTimeout).get();
				byte serverSideIdleMaxTimeout = ctx.channel().attr(ChannelAttrKeys.maxIdleTimeout).get();
				if (idleTimeout != serverSideIdleTimeout) {
					ChannelHandlerContext idleHandlerContext = ctx.pipeline().context(IdleStateHandler.class);
					ctx.pipeline().replace(IdleStateHandler.class, idleHandlerContext.name(),
							new IdleStateHandler(idleTimeout, 0, 0));
				}
				if (idleMaxTimeout != serverSideIdleMaxTimeout) {
					ctx.channel().attr(ChannelAttrKeys.maxIdleTimeout).set(idleMaxTimeout);
				}

				String uuidStr = uuid.toString();
				//bind uuidStr to Channel
				ctx.channel().attr(ChannelAttrKeys.uuidStr).set(uuidStr);
				ExchangeServer exchangeServer = ctx.channel().attr(ChannelAttrKeys.exchangeServer).get();
				synchronized (uuidStr.intern()) {
					if (channelType == ChannelType.readwrite.code()) {
						negotiateReadWriteChannel(ctx, uuid, uuidStr, exchangeServer, connections, channelIndex,
								reverseIndex);
					} else if (channelType == ChannelType.read.code() || channelType == ChannelType.write.code()) {
						negotiateReadOrWriteChannel(ctx, uuid, uuidStr, exchangeServer, connections, writeConnections,
								channelType, channelIndex, reverseIndex);
					}
				}
			}
			// normal heart beat request
			ctx.writeAndFlush(ResponseProtocol.newHeartbeat());
		} catch (Exception cause) {
			exceptionCaught(ctx, cause);
		} finally {
			ctx.channel().attr(OneTime.idleTimeout).remove();
		}
	}

	private void negotiateReadWriteChannel(ChannelHandlerContext ctx, UUID uuid, String uuidStr,
			ExchangeServer exchangeServer, int connections, int channelIndex, boolean reverseIndex) {
		ExchangeChannelGroup channelGroup = uuid2ChannelGroup.get(uuidStr);
		if(null == channelGroup){
			uuid2ChannelGroup.put(uuidStr,channelGroup = new ServerExchangeChannelGroup(null, exchangeServer.getMsgHandler(), uuid, connections));
		}
		// bind current channel to ExchangeChannelGroup
		ctx.channel().attr(ChannelAttrKeys.channelGroup).set(channelGroup);
		int finalChannelIndex = channelIndex;
		if (reverseIndex) {
			// Reverse channel index to get better io performance
			finalChannelIndex = ArrayUtils.reverseArrayIndex(connections, channelIndex);
		}

		// add child
		((ServerExchangeChannelGroup) channelGroup).retain();
		((ServerExchangeChannelGroup) channelGroup).addChild(new ServerExchangeChannel(channelGroup, ctx.channel()),
				finalChannelIndex);
	}

	private void negotiateReadOrWriteChannel(ChannelHandlerContext ctx, UUID uuid, String uuidStr,
			ExchangeServer exchangeServer, int connections, int writeConnections, int channelType, int channelIndex,
			boolean reverseIndex) {
		// negotiate read write splitting settings
		ExchangeChannelGroup channelGroup = uuid2ChannelGroup.get(uuidStr);
		if(null == channelGroup){
			 uuid2ChannelGroup.putIfAbsent(uuidStr,
						channelGroup = new ReadWriteServerExchangeChannelGroup(exchangeServer.getMsgHandler(), uuid, connections, writeConnections));
		}
		// bind current channel to ExchangeChannelGroup
		ctx.channel().attr(ChannelAttrKeys.channelGroup).set(channelGroup);

		int finalChannelIndex = channelIndex;
		if (channelType == ChannelType.read.code()) {// read to write
			ServerExchangeChannelGroup writeGroup = ((ReadWriteServerExchangeChannelGroup) channelGroup)
					.getWriteGroup();
			if (reverseIndex) {
				// Reverse channel index to get better io performance
				finalChannelIndex = ArrayUtils.reverseArrayIndex(connections - writeConnections, finalChannelIndex);
			}
			((ReadWriteServerExchangeChannelGroup)channelGroup).retain();
			writeGroup.addChild(new ServerExchangeChannel(writeGroup, ctx.channel()), finalChannelIndex);
		} else if (channelType == ChannelType.write.code()) {// write to read
			ServerExchangeChannelGroup readGroup = ((ReadWriteServerExchangeChannelGroup) channelGroup).getReadGroup();
			if (reverseIndex) {
				// Reverse channel index to get better io performance
				finalChannelIndex = ArrayUtils.reverseArrayIndex(writeConnections, finalChannelIndex);
			}
			((ReadWriteServerExchangeChannelGroup)channelGroup).retain();
			readGroup.addChild(new ServerExchangeChannel(readGroup, ctx.channel()), finalChannelIndex);
		}
	}

	@SuppressWarnings("deprecation")
	private void remove(ChannelHandlerContext ctx) {
		try {
			//remove onetime attrs
			ctx.channel().attr(OneTime.uuid).remove();
			ctx.channel().attr(OneTime.channelType).remove();
			ctx.channel().attr(OneTime.connections).remove();
			ctx.channel().attr(OneTime.writeConnections).remove();
			ctx.channel().attr(OneTime.channelIndex).remove();
			ctx.channel().attr(OneTime.reverseIndex).remove();
			
			ChannelPipeline pipeline = ctx.pipeline();
			if (pipeline.context(this) != null) {
				pipeline.remove(this);
			}
			
		} finally {
			negotiateMap.remove(ctx);
		}
	}
}
