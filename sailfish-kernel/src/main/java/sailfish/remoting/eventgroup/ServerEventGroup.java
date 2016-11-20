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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.internal.SystemPropertyUtil;
import sailfish.remoting.constants.RemotingConstants;

/**
 * @author spccold
 * @version $Id: ServerEventGroup.java, v 0.1 2016年11月20日 下午7:29:27 spccold Exp
 *          $
 */
public class ServerEventGroup extends AbstractReusedEventGroup {
	public static final Logger logger = LoggerFactory.getLogger(ServerEventGroup.class);

	private static final int IO_THREADS;
	private static final int EVENT_THREADS;
	public static final ServerEventGroup INSTANCE;
	static {
		IO_THREADS = Math.max(1,
				SystemPropertyUtil.getInt("sailfish.remoting.server.ioThreads", RemotingConstants.DEFAULT_IO_THREADS));
		EVENT_THREADS = Math.max(1, SystemPropertyUtil.getInt("sailfish.remoting.server.eventThreads",
				RemotingConstants.DEFAULT_EVENT_THREADS));
		if (logger.isDebugEnabled()) {
			logger.debug("-Dsailfish.remoting.server.ioThreads: {}", IO_THREADS);
			logger.debug("-Dsailfish.remoting.server.eventThreads: {}", EVENT_THREADS);
		}
		INSTANCE = new ServerEventGroup();
	}

	private ServerEventGroup() {
		super(IO_THREADS, RemotingConstants.SERVER_IO_THREADNAME, EVENT_THREADS,
				RemotingConstants.SERVER_EVENT_THREADNAME);
	}
}
