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

import io.netty.bootstrap.Bootstrap;
import sailfish.remoting.Address;
import sailfish.remoting.RequestControl;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.ResponseFuture;

/**
 * @author spccold
 * @version $Id: LazyExchangeChannel.java, v 0.1 2016年11月21日 下午11:18:11 spccold Exp $
 */
public final class LazyExchangeChannel extends SingleConnctionExchangeChannel {
	
	private volatile boolean lazyWithOutInit = true;

	LazyExchangeChannel(Bootstrap bootstrap, ExchangeChannelGroup parent, Address address, int reconnectInterval)
			throws SailfishException {
		super(bootstrap, parent, address, reconnectInterval, false);
	}

	@Override
	public void oneway(byte[] data, RequestControl requestControl) throws SailfishException {
		initChannel();
		super.oneway(data, requestControl);
	}

	@Override
	public ResponseFuture<byte[]> request(byte[] data, RequestControl requestControl) throws SailfishException {
		initChannel();
		return super.request(data, requestControl);
	}

	@Override
	public void request(byte[] data, ResponseCallback<byte[]> callback, RequestControl requestControl)
			throws SailfishException {
		initChannel();
		super.request(data, callback, requestControl);
	}

	private void initChannel() throws SailfishException {
		if (null != channel) {
			return;
		}
		synchronized (this) {
			if (null != channel) {
				return;
			}
			this.channel = doConnect();
			this.lazyWithOutInit = false;
		}
	}

	@Override
	public boolean isAvailable() {
		if (isClosed()) {
			return false;
		}
		if (this.lazyWithOutInit) {
			return true;
		}
		return super.isAvailable();
	}
}
