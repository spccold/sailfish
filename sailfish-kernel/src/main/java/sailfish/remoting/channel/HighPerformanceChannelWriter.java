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
package sailfish.remoting.channel;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.internal.PlatformDependent;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.protocol.Protocol;

/**
 * <pre>
 * 	<a href="https://github.com/netty/netty/issues/1759">Optimize writeAndFlush</a>
 * 	<a href="https://github.com/stepancheg/netty-td">netty-td</a>
 * </pre>
 * 
 * @author spccold
 * @version $Id: HighPerformanceChannelWriter.java, v 0.1 2016年11月30日 下午10:10:40 spccold Exp $
 */
public class HighPerformanceChannelWriter {

	/**
	 *  task for "merge" all pending flushes
	 */
	private final WriteAndFlushTask writeAndFlushTask = new WriteAndFlushTask(this);

	private final AtomicInteger state = new AtomicInteger();
	// lock free(See https://github.com/spccold/JCTools)
	private final Queue<Protocol> queue = PlatformDependent.newMpscQueue();

	private final Channel channel;

	public HighPerformanceChannelWriter(Channel channel) {
		this.channel = channel;
	}

	public static void write(Channel channel, Protocol protocol) {
		final HighPerformanceChannelWriter highPerformanceWriter = getWriter(channel);
		highPerformanceWriter.queue.add(protocol);
		if (highPerformanceWriter.addTask()) {
			channel.eventLoop().execute(highPerformanceWriter.writeAndFlushTask);
		}
	}

	private static HighPerformanceChannelWriter getWriter(Channel channel) {
		Attribute<HighPerformanceChannelWriter> attr = channel.attr(ChannelAttrKeys.highPerformanceWriter);
		HighPerformanceChannelWriter writer = attr.get();
		if (null == writer) {
			HighPerformanceChannelWriter old = attr.setIfAbsent(writer = new HighPerformanceChannelWriter(channel));
			if (null != old) {
				writer = old;
			}
		}
		return writer;
	}

	private void writeAndFlush() {
		while (fetchTask()) {
			Protocol protocol = null;
			while (null != (protocol = queue.poll())) {
				channel.write(protocol, channel.voidPromise());
			}
		}
		channel.flush();
	}

	/**
	 * @return <code>true</code> if we have to recheck queues
	 */
	private boolean fetchTask() {
		int old = state.getAndDecrement();
		if (old == State.RUNNING_GOT_TASKS.ordinal()) {
			return true;
		} else if (old == State.RUNNING_NO_TASKS.ordinal()) {
			return false;
		} else {
			throw new AssertionError();
		}
	}

	/**
	 * @return <code>true</code> if caller has to schedule task execution
	 */
	private boolean addTask() {
		// fast track for high-load applications
		// atomic get is cheaper than atomic swap
		// for both this thread and fetching thread
		if (state.get() == State.RUNNING_GOT_TASKS.ordinal())
			return false;

		int old = state.getAndSet(State.RUNNING_GOT_TASKS.ordinal());
		return old == State.WAITING.ordinal();
	}

	static final class WriteAndFlushTask implements Runnable {

		private final HighPerformanceChannelWriter writer;

		public WriteAndFlushTask(HighPerformanceChannelWriter writer) {
			this.writer = writer;
		}

		@Override
		public void run() {
			writer.writeAndFlush();
		}
	}

	private enum State {
		/** actor is not currently running */
		WAITING,
		/** actor is running, and has no more tasks */
		RUNNING_NO_TASKS,
		/** actor is running, but some queues probably updated, actor needs to recheck them */
		RUNNING_GOT_TASKS,
	}
}
