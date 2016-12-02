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
package sailfish.remoting.future;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.CharsetUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.Tracer;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.executor.SimpleExecutor;
import sailfish.remoting.utils.ParameterChecker;

/**
 * 
 * @author spccold
 * @version $Id: BytesResponseFuture.java, v 0.1 2016年10月4日 下午3:57:32 jileng Exp $
 */
public class BytesResponseFuture implements ResponseFuture<byte[]> {
	
	private static final Logger logger = LoggerFactory.getLogger(BytesResponseFuture.class);
	
	private static final Timer TIMER = new HashedWheelTimer(
			new DefaultThreadFactory("sailfish-callback-timeout-checker", true));

	private final int packetId;
	private volatile boolean done;
	private volatile boolean successed;
	private ResponseCallback<byte[]> callback;
	private Timeout tout;
	private byte[] data;

	private volatile SailfishException cause;
	private final Tracer tracer;

	public BytesResponseFuture(int packetId, Tracer tracer) {
		this.packetId = packetId;
		this.tracer = tracer;
	}

	@Override
	public boolean isDone() {
		return this.done;
	}

	@Override
	public byte[] get() throws SailfishException, InterruptedException {
		synchronized (this) {
			while (!this.done) {
				wait();
			}
		}
		if (!this.successed) {
			if (null == cause) {
				this.cause = new SailfishException(new String(data, CharsetUtil.UTF_8)).toRemoteException();
			}
			throw cause;
		}
		return data;
	}

	@Override
	public byte[] get(long timeout, TimeUnit unit) throws SailfishException, InterruptedException {
		ParameterChecker.checkPositive(timeout, "timeout");
		synchronized (this) {
			long timeToSleep = unit.toMillis(timeout);
			long deadline = System.currentTimeMillis() + timeToSleep;
			while (!this.done && timeToSleep > 0) {
				wait(timeToSleep);
				timeToSleep = deadline - System.currentTimeMillis();
			}
		}
		if (!this.done) {
			this.done = true;
			removeTrace();
			String msg = String.format("wait response for packetId[%d] timeout", packetId);
			this.cause = new SailfishException(ExceptionCode.RESPONSE_TIMEOUT, msg);
			throw cause;
		}
		if (!this.successed) {
			if (null == cause) {
				this.cause = new SailfishException(new String(data, CharsetUtil.UTF_8)).toRemoteException();
			}
			throw cause;
		}
		return data;
	}

	@Override
	public void putResponse(byte[] data, byte result, SailfishException cause) {
		synchronized (this) {
			this.done = true;
			this.cause = cause;
			this.data = data;
			switch (result) {
			case RemotingConstants.RESULT_SUCCESS:
				this.successed = true;
				break;
			case RemotingConstants.RESULT_FAIL:
				this.successed = false;
				break;
			}
			notifyAll();
		}

		if (null != tout) {
			tout.cancel();
		}

		if (null == callback) {
			return;
		}
		executeCallbackTask();
	}

	@Override
	public void setCallback(final ResponseCallback<byte[]> callback, final int timeout) {
		if (null == callback || this.done) {
			return;
		}
		this.callback = callback;
		tout = TIMER.newTimeout(CallbackCheckTask.newInstance(this), timeout, TimeUnit.MILLISECONDS);
	}

	private void executeCallbackTask() {
		Executor executor = null != this.callback.getExecutor() ? this.callback.getExecutor() : SimpleExecutor.INSTANCE;
		try {
			executor.execute(CallbackTask.newInstance(this));
		} catch (RejectedExecutionException cause) {
			logger.error(String.format("executor[%s] reject to execute callback task, SimpleExecutor will pick task up", executor), cause);
			SimpleExecutor.INSTANCE.execute(CallbackTask.newInstance(this));
		}
	}

	private void removeTrace() {
		this.tracer.remove(packetId);
	}

	static final class CallbackTask implements Runnable {

		private static final Recycler<CallbackTask> RECYCLER = new Recycler<BytesResponseFuture.CallbackTask>() {
			@Override
			protected CallbackTask newObject(Recycler.Handle<CallbackTask> handle) {
				return new CallbackTask(handle);
			}
		};

		public static CallbackTask newInstance(BytesResponseFuture responseFuture) {
			CallbackTask callbackTask = RECYCLER.get();
			callbackTask.responseFuture = responseFuture;
			return callbackTask;
		}

		private final Recycler.Handle<CallbackTask> handle;
		private BytesResponseFuture responseFuture;

		public CallbackTask(Handle<CallbackTask> handle) {
			this.handle = handle;
		}

		@Override
		public void run() {
			try {
				if (!responseFuture.done) {
					responseFuture.done = true;
					responseFuture.removeTrace();
					String msg = String.format("wait response for packetId[%d] timeout", responseFuture.packetId);
					responseFuture.cause = new SailfishException(ExceptionCode.RESPONSE_TIMEOUT, msg);
					responseFuture.callback.handleException(responseFuture.cause);
					return;
				}
				if (responseFuture.successed) {
					responseFuture.callback.handleResponse(responseFuture.data);
					return;
				}
				if (null == responseFuture.cause) {
					responseFuture.cause = new SailfishException(new String(responseFuture.data, CharsetUtil.UTF_8))
							.toRemoteException();
				}
				responseFuture.callback.handleException(responseFuture.cause);
			} finally {
				responseFuture = null;
				handle.recycle(this);
			}
		}
	}

	static final class CallbackCheckTask implements TimerTask {

		private static final Recycler<CallbackCheckTask> RECYCLER = new Recycler<CallbackCheckTask>() {
			@Override
			protected CallbackCheckTask newObject(Recycler.Handle<CallbackCheckTask> handle) {
				return new CallbackCheckTask(handle);
			}
		};

		public static CallbackCheckTask newInstance(BytesResponseFuture responseFuture) {
			CallbackCheckTask checkTask = RECYCLER.get();
			checkTask.responseFuture = responseFuture;
			return checkTask;
		}

		private final Recycler.Handle<CallbackCheckTask> handle;
		private BytesResponseFuture responseFuture;

		public CallbackCheckTask(Handle<CallbackCheckTask> handle) {
			this.handle = handle;
		}

		@Override
		public void run(Timeout timeout) throws Exception {
			try {
				if (responseFuture.done) {
					return;
				}
				responseFuture.executeCallbackTask();
			} finally {
				responseFuture = null;
				handle.recycle(this);
			}
		}
	}
}
