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
package sailfish.remoting.channel2;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import io.netty.channel.Channel;
import sailfish.remoting.utils.ParameterChecker;
import sailfish.remoting.utils.RemotingUtils;

/**
 * @author spccold
 * @version $Id: AbstractExchangeChannel.java, v 0.1 2016年11月21日 下午10:49:12
 *          spccold Exp $
 */
public abstract class AbstractExchangeChannel implements ExchangeChannel {
	protected Channel channel;
	protected volatile boolean reconnectting;
	protected volatile boolean closed = false;
	 
	private final ExchangeChannelGroup parent;
	private final Collection<ExchangeChannel> selfCollection = Collections.<ExchangeChannel>singleton(this);

	protected AbstractExchangeChannel(ExchangeChannelGroup parent) {
		this.parent = parent;
	}

	@Override
	public ExchangeChannelGroup parent() {
		return parent;
	}

	@Override
	public ExchangeChannel next() {
		return this;
	}

	@Override
	public Channel update(Channel newChannel) {
		synchronized (this) {
			this.reconnectting = false;
			if (this.isClosed()) {
				RemotingUtils.closeChannel(newChannel);
				return channel;
			}
			Channel old = channel;
			this.channel = newChannel;
			return old;
		}
	}

	@Override
	public Iterator<ExchangeChannel> iterator() {
		return selfCollection.iterator();
	}

	@Override
	public void close() {
		close(0);
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
            //deal unfinished requests, response with channel closed exception
//            long start = System.currentTimeMillis();
//            while (CollectionUtils.isNotEmpty(Tracer.peekPendingRequests(this))
//                   && (System.currentTimeMillis() - start < timeout)) {
//                LockSupport.parkNanos(1000 * 1000 * 10L);
//            }
//            Map<Integer, Object> pendingRequests = Tracer.popPendingRequests(this);
//            if (CollectionUtils.isEmpty(pendingRequests)) {
//                return;
//            }
//            for (Integer packetId : pendingRequests.keySet()) {
//                Tracer.erase(ResponseProtocol.newErrorResponse(packetId,
//                    "unfinished request because of channel:" + channel.toString() + " be closed",
//                    RemotingConstants.RESULT_FAIL));
//            }
            RemotingUtils.closeChannel(channel);
        }
	}

	@Override
	public boolean isClosed() {
		return closed;
	}
}
