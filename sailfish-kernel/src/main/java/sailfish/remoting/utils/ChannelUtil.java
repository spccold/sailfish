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
package sailfish.remoting.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import sailfish.remoting.constants.ChannelAttrKeys;

/**
 * @author spccold
 * @version $Id: ChannelUtil.java, v 0.1 2016年11月23日 下午11:13:40 spccold Exp $
 */
public class ChannelUtil {

	private static final Logger logger = LoggerFactory.getLogger(ChannelUtil.class);

	public static boolean clientSide(ChannelHandlerContext ctx) {
		Attribute<Boolean> clientSideAttr = ctx.channel().attr(ChannelAttrKeys.clientSide);
		return (null != clientSideAttr && null != clientSideAttr.get() && clientSideAttr.get());
	}

	public static void closeChannel(final Channel channel) {
		if (null == channel) {
			return;
		}
		channel.close().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				String log = String.format(
						"closeChannel: close connection to remoteAddress [%s] , localAddress [%s], ret:[%b]",
						null != channel.remoteAddress() ? channel.remoteAddress().toString() : "null",
						null != channel.localAddress() ? channel.localAddress().toString() : "null",
						future.isSuccess());
				logger.info(log);
			}
		});
	}
}
