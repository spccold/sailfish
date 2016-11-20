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
package sailfish.remoting.eventloopgroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.EventLoopGroup;
import io.netty.util.internal.SystemPropertyUtil;
import sailfish.remoting.constants.RemotingConstants;

/**
 * @author spccold
 * @version $Id: ClientEventLoopGroup.java, v 0.1 2016年11月20日 下午7:29:07 spccold Exp $
 */
public class ClientEventLoopGroup extends AbstractReusedEventLoopGroup{
	private static final Logger logger = LoggerFactory.getLogger(ClientEventLoopGroup.class);
	public static final ClientEventLoopGroup INSTANCE = new ClientEventLoopGroup();
	
	private static final int IO_THREADS;
	static{
		IO_THREADS = Math.max(1,
				SystemPropertyUtil.getInt("sailfish.remoting.client.ioThreads", RemotingConstants.DEFAULT_IO_THREADS));
		if (logger.isDebugEnabled()) {
			logger.debug("-Dsailfish.remoting.client.ioThreads: {}", IO_THREADS);
		}
	}
	
	private ClientEventLoopGroup() {
		super(IO_THREADS, RemotingConstants.CLIENT_IO_THREADNAME);
	}
	
	@Override
	public EventLoopGroup get() {
		return reusedEventLoopGroup;
	}
}
