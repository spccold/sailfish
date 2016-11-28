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

import static io.netty.util.internal.ObjectUtil.checkPositive;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import io.netty.util.IllegalReferenceCountException;
import io.netty.util.internal.PlatformDependent;
import sailfish.remoting.configuration.NegotiateConfig;
import sailfish.remoting.handler.NegotiateChannelHandler;

/**
 * @author spccold
 * @version $Id: ReferenceCountedServerExchangeChannelGroup.java, v 0.1 2016年11月26日 下午5:05:43
 *          spccold Exp $
 */
public abstract class ReferenceCountedServerExchangeChannelGroup extends AbstractExchangeChannelGroup {
	
	private static final AtomicIntegerFieldUpdater<ReferenceCountedServerExchangeChannelGroup> refCntUpdater;

	static {
		AtomicIntegerFieldUpdater<ReferenceCountedServerExchangeChannelGroup> updater = PlatformDependent
				.newAtomicIntegerFieldUpdater(ReferenceCountedServerExchangeChannelGroup.class, "refCnt");
		if (updater == null) {
			updater = AtomicIntegerFieldUpdater.newUpdater(ReferenceCountedServerExchangeChannelGroup.class, "refCnt");
		}
		refCntUpdater = updater;
	}

	private volatile int refCnt = 0;

	protected ReferenceCountedServerExchangeChannelGroup(UUID id) {
		super(id);
	}

	public ReferenceCountedServerExchangeChannelGroup retain() {
		return retain0(1);
	}

	public ReferenceCountedServerExchangeChannelGroup retain(int increment) {
		return retain0(checkPositive(increment, "increment"));
	}

	private ReferenceCountedServerExchangeChannelGroup retain0(int increment) {
		for (;;) {
			int refCnt = this.refCnt;
			final int nextCnt = refCnt + increment;
			if (refCntUpdater.compareAndSet(this, refCnt, nextCnt)) {
				break;
			}
		}
		return this;
	}
	
	public boolean release(){
		return release0(1);
	}
	
	public boolean release(int decrement) {
		return release0(checkPositive(decrement, "decrement"));
	}

	private boolean release0(int decrement) {
		for (;;) {
			int refCnt = this.refCnt;
			if (refCnt < decrement) {
				throw new IllegalReferenceCountException(refCnt, -decrement);
			}

			if (refCntUpdater.compareAndSet(this, refCnt, refCnt - decrement)) {
				if (refCnt == decrement) {
					deallocate();
					return true;
				}
				return false;
			}
		}
	}

	private void deallocate(){
		NegotiateChannelHandler.uuid2ChannelGroup.remove(id().toString());
	}
	
	public abstract void addChild(ExchangeChannel channel, NegotiateConfig config);
}
