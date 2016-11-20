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
package sailfish.remoting;

import java.util.concurrent.ThreadFactory;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import sailfish.remoting.utils.PlatformUtil;

/**
 * <a href="http://netty.io/wiki/native-transports.html">netty
 * native-transports</a> <a href=
 * "http://stackoverflow.com/questions/23465401/why-native-epoll-support-is-introduced-in-netty">Why
 * native epoll support is introduced in Netty?</a>
 * 
 * @author spccold
 * @version $Id: NettyPlatformIndependent.java, v 0.1 2016年11月14日 下午4:51:01
 *          jileng Exp $
 */
public class NettyPlatformIndependent {

	public static EventLoopGroup newEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
		if (PlatformUtil.isLinux()) {
			return new EpollEventLoopGroup(nThreads, threadFactory);
		}
		return new NioEventLoopGroup(nThreads, threadFactory);
	}

	public static Class<? extends Channel> channelClass() {
		if (PlatformUtil.isLinux()) {
			return EpollSocketChannel.class;
		}
		return NioSocketChannel.class;
	}

	public static Class<? extends ServerChannel> serverChannelClass() {
		if (PlatformUtil.isLinux()) {
			return EpollServerSocketChannel.class;
		}
		return NioServerSocketChannel.class;
	}
}
