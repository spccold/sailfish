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

import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.timeout.IdleStateHandler;
import sailfish.remoting.Address;
import sailfish.remoting.ReconnectManager;
import sailfish.remoting.Tracer;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.protocol.ResponseProtocol;
import sailfish.remoting.utils.ChannelUtil;
import sailfish.remoting.utils.CollectionUtils;
import sailfish.remoting.utils.ParameterChecker;

/**
 * @author spccold
 * @version $Id: SingleConnctionExchangeChannel.java, v 0.1 2016年11月21日 下午11:05:58 spccold Exp $
 */
public abstract class SingleConnctionExchangeChannel extends AbstractExchangeChannel {
	private final Bootstrap reusedBootstrap;
	
	private volatile boolean reconnectting = false;
	private final int reconnectInterval;

	/**
	 * Create a new instance
	 * 
	 * @param parent    			the {@link ExchangeChannelGroup} which is the parent of this instance and belongs to it
	 * @param address				the {@link Address} which be connected to
	 * @param connectTimeout 		connect timeout in milliseconds
	 * @param reconnectInterval 	reconnect interval in milliseconds for {@link ReconnectManager}
	 * @param idleTimeout idle  	timeout in seconds for {@link IdleStateHandler}
	 * @param maxIdleTimeOut 		max idle timeout in seconds for {@link ChannelEventsHandler}
	 * @param doConnect				connect to remote peer or not when initial
	 */
	protected SingleConnctionExchangeChannel(Bootstrap bootstrap, ExchangeChannelGroup parent, Address address,
			int reconnectInterval, boolean doConnect) throws SailfishException {
		super(parent, address);
		this.reusedBootstrap = bootstrap;
		this.reconnectInterval = reconnectInterval;
		if (doConnect) {
			this.channel = doConnect();
		}
	}
	
	@Override
	public Channel doConnect() throws SailfishException {
		try {
			Channel channel = reusedBootstrap.connect().syncUninterruptibly().channel();
			return channel;
		} catch (Throwable cause) {
			throw new SailfishException(cause);
		}
	}
	
	@Override
	public void recover() {
		// add reconnect task
		ReconnectManager.INSTANCE.addReconnectTask(this, reconnectInterval);
	}

	@Override
	public Channel update(Channel newChannel) {
		synchronized (this) {
			this.reconnectting = false;
			if (this.isClosed()) {
				ChannelUtil.closeChannel(newChannel);
				return channel;
			}
			Channel old = channel;
			this.channel = newChannel;
			return old;
		}
	}

	@Override
	public boolean isAvailable() {
		boolean isAvailable = false;
		if (!reconnectting && !(isAvailable = super.isAvailable())) {
			synchronized (this) {
				if (!reconnectting && !(isAvailable = super.isAvailable())) {
					recover();
					this.reconnectting = true;
				}
			}
		}
		return isAvailable;
	}

	@Override
	public void close(int timeout) {
		ParameterChecker.checkNotNegative(timeout, "timeout");
		if (this.isClosed()) {
			return;
		}
		synchronized (this) {
			if (this.isClosed()) {
				return;
			}
			this.closed = true;
			// deal unfinished requests, response with channel closed exception
			long start = System.currentTimeMillis();
			while (CollectionUtils.isNotEmpty(getTracer().peekPendingRequests(this))
					&& (System.currentTimeMillis() - start < timeout)) {
				LockSupport.parkNanos(1000 * 1000 * 10L);
			}
			Map<Integer, Object> pendingRequests = getTracer().popPendingRequests(this);
			if (CollectionUtils.isEmpty(pendingRequests)) {
				return;
			}
			for (Integer packetId : pendingRequests.keySet()) {
				getTracer().erase(ResponseProtocol.newErrorResponse(packetId,
						"unfinished request because of channel:" + channel.toString() + " be closed",
						RemotingConstants.RESULT_FAIL));
			}
			ChannelUtil.closeChannel(channel);
		}
	}

	@Override
	public MsgHandler<Protocol> getMsgHander() {
		return parent().getMsgHander();
	}

	@Override
	public Tracer getTracer() {
		return parent().getTracer();
	}
}
