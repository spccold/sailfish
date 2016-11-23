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

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import sailfish.remoting.constants.ChannelAttrKeys;

/**
 * @author spccold
 * @version $Id: ChannelUtil.java, v 0.1 2016年11月23日 下午11:13:40 spccold Exp $
 */
public class ChannelUtil {

	public static boolean clientSide(ChannelHandlerContext ctx) {
		Attribute<Boolean> clientSideAttr = ctx.channel().attr(ChannelAttrKeys.clientSide);
		return (null != clientSideAttr && null != clientSideAttr.get() && clientSideAttr.get());
	}

}
