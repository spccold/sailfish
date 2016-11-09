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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateHandler;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.constants.Opcode;
import sailfish.remoting.constants.RemotingConstants;
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
public class ShareableSimpleChannelInboundHandler extends SimpleChannelInboundHandler<Protocol> {
    private static final ConcurrentMap<String, ChannelHandlerContexts> readWriteContexts = new ConcurrentHashMap<>();
    private static final ConcurrentMap<ChannelHandlerContext, String>  context2Uuid      = new ConcurrentHashMap<>();

    private final MsgHandler<Protocol>                                 msgHandler;
    private final boolean                                              clientSide;

    public ShareableSimpleChannelInboundHandler(MsgHandler<Protocol> msgHandler, boolean clientSide) {
        this.msgHandler = msgHandler;
        this.clientSide = clientSide;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Protocol msg) throws Exception {
        if (msg.request() && msg.heartbeat()) {//deal heart beat request
            RequestProtocol requestProtocol = (RequestProtocol) msg;
            if (requestProtocol.opcode() == Opcode.HEARTBEAT_WITH_NEGOTIATE) {//negotiate idle timeout
                byte[] body = requestProtocol.body();
                int idleTimeout = RemotingConstants.DEFAULT_IDLE_TIMEOUT;
                int idleMaxTimeout = RemotingConstants.DEFAULT_MAX_IDLE_TIMEOUT;

                UUID uuid = null;
                boolean writeChannel = false;
                int channelIndex = 0;
                if (body.length == 2) {
                    idleTimeout = requestProtocol.body()[0];
                    idleMaxTimeout = requestProtocol.body()[1];
                } else {
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(body));
                    idleTimeout = dis.readByte();
                    idleMaxTimeout = dis.readByte();
                    writeChannel = dis.readBoolean();
                    channelIndex = dis.readInt();
                    uuid = new UUID(dis.readLong(), dis.readLong());
                }
                IdleStateHandler old = ctx.pipeline().get(IdleStateHandler.class);
                if (null != old) {
                    ctx.pipeline().replace(IdleStateHandler.class, "idleStateHandler",
                        new IdleStateHandler(idleTimeout, 0, 0));
                    ctx.channel().attr(ChannelAttrKeys.maxIdleTimeout).set(idleMaxTimeout);
                }

                if (null != uuid) {//negotiate read write splitting settings
                    String uuidStr = uuid.toString();
                    ChannelHandlerContexts contexts = readWriteContexts.get(uuidStr);
                    if (null == contexts) {
                        ChannelHandlerContexts existed = readWriteContexts.putIfAbsent(uuidStr,
                            contexts = new ChannelHandlerContexts());
                        if (null != existed) {
                            contexts = existed;
                        }
                    }
                    // contrary to remote peer, read to write, write to read
                    if (writeChannel) {
                        contexts.addReadChannelHandlerContext(ctx, channelIndex);
                    } else {
                        contexts.addWriteChannelHandlerContext(ctx, channelIndex);
                    }
                    context2Uuid.put(ctx, uuidStr);
                }
            }
            //normal heart beat request
            ctx.writeAndFlush(ResponseProtocol.newHeartbeat());
            return;
        }

        String uuid = null;
        ChannelHandlerContexts contexts = null;
        if (!clientSide && (null != (uuid = context2Uuid.get(ctx)))
            && (null != (contexts = readWriteContexts.get(uuid)))) {
            msgHandler.handle(contexts, msg);
        } else {
            msgHandler.handle(ctx, msg);
        }
    }
}
