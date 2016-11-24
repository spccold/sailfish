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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.utils.ChannelUtil;

/**
 * 
 * @author spccold
 * @version $Id: RemotingDecoder.java, v 0.1 2016年10月15日 上午11:20:55 jileng Exp $
 */
public class RemotingDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger logger = LoggerFactory.getLogger(RemotingDecoder.class);
    public RemotingDecoder() {
        super(RemotingConstants.DEFAULT_PAYLOAD, 2, 4);
    }
    
    //all read exceptions will be fired by exceptionCaught()
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf buffer = (ByteBuf) super.decode(ctx, in);
        if (null == buffer) {
            return null;
        }
        try {
            return DefaultRemotingCodec.INSTANCE.decode(buffer);
        } catch (SailfishException e) {
            if (e.code() == ExceptionCode.BAD_PACKAGE) {
                String log = String.format("packet from remoteAddress [%s] invalid, begin to close channel to [%s], detail: %s",
                    ctx.channel().remoteAddress().toString(), ctx.channel().remoteAddress().toString(), e.getMessage());
                logger.error(log);
                ChannelUtil.closeChannel(ctx.channel());
                return null;
            }
            throw e;
        } finally {
            buffer.release();
        }
    }
}
