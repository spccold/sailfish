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
package sailfish.remoting.eventgroup;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import sailfish.remoting.NettyPlatformIndependent;

/**
 * @author spccold
 * @version $Id: AbstractReusedEventGroup.java, v 0.1 2016年11月20日 下午7:29:59
 *          spccold Exp $
 */
public abstract class AbstractReusedEventGroup implements ReusedEventGroup {
	protected final EventLoopGroup reusedEventLoopGroup;
	protected final EventExecutorGroup reusedEventExecutorGroup;

	protected AbstractReusedEventGroup(int ioThreads, String ioThreadName, int eventThreads, String eventThreadName) {
		this.reusedEventLoopGroup = NettyPlatformIndependent.newEventLoopGroup(ioThreads,
				new DefaultThreadFactory(ioThreadName));
		this.reusedEventExecutorGroup = new DefaultEventExecutorGroup(eventThreads,
				new DefaultThreadFactory(eventThreadName));
	}

	@Override
	public void destory() {
		reusedEventLoopGroup.shutdownGracefully();
		reusedEventExecutorGroup.shutdownGracefully();
	}

	@Override
	public EventLoopGroup getLoopGroup() {
		return reusedEventLoopGroup;
	}

	@Override
	public EventExecutorGroup getExecutorGroup() {
		return reusedEventExecutorGroup;
	}

}
