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
package sailfish.remoting.executor;

import java.util.LinkedList;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.SystemPropertyUtil;

/**
 * similar implementation
 * 
 * <pre>
 * 		<a href="https://github.com/eclipse/vert.x/blob/65a1050b0922de38329cdbe90c5ecd8094a93a04/
 * 		src/main/java/io/vertx/core/impl/OrderedExecutorFactory.java">OrderedExecutorFactory In Vert.x</a>
 * 		
 * 		<a href=https://github.com/netty/netty/blob/eb7f751ba519cbcab47d640cd18757f09d077b55/common
 * 		/src/main/java/io/netty/util/concurrent/GlobalEventExecutor.java">GlobalEventExecutor In Netty</a>
 * </pre>
 * need long-running?
 * 
 * @author spccold
 * @version $Id: SimpleExecutor.java, v 0.1 2016年11月1日 下午3:14:52 jileng Exp $
 */
public class SimpleExecutor implements Executor, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(SimpleExecutor.class);
	
	private static final boolean preferGlobalEventExecutor;
	static{
		preferGlobalEventExecutor = SystemPropertyUtil.getBoolean("sailfish.simpleExector.preferGlobalEventExecutor", true);
		if(logger.isDebugEnabled()){
			logger.debug("-Dsailfish.simpleExector.preferGlobalEventExecutor: {}", preferGlobalEventExecutor);
		}
	}
	
	// produce FastThreadLocalThread(can benefit from FastThreadLocal(e.g. Recycler))
	private final DefaultThreadFactory THREADFACTORY = new DefaultThreadFactory("sailfish-simpleexecutor", true);

	public static final SimpleExecutor INSTANCE = new SimpleExecutor();
	private final LinkedList<Runnable> tasks = new LinkedList<>();
	private volatile boolean isRunning = false;

	private SimpleExecutor() { }

	@Override
	public void execute(Runnable task) {
		if(preferGlobalEventExecutor){
			GlobalEventExecutor.INSTANCE.execute(task);
			return;
		}
		synchronized (tasks) {
			tasks.add(task);
			if (!this.isRunning) {
				this.isRunning = true;
				newThread().start();
			}
		}
	}

	private Thread newThread() {
		return THREADFACTORY.newThread(this);
	}

	@Override
	public void run() {
		for (;;) {
			Runnable currentTask = null;
			synchronized (tasks) {
				currentTask = tasks.poll();
				if (null == currentTask) {
					this.isRunning = false;
					break;
				}
			}
			try {
				currentTask.run();
			} catch (Throwable cause) {
				logger.error("catch exception by SimpleExecutor", cause);
			}
		}
	}
}
