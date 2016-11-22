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

import java.util.UUID;

import sailfish.remoting.RequestControl;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.ResponseFuture;

/**
 * @author spccold
 * @version $Id: AbstractExchangeChannelGroup.java, v 0.1 2016年11月22日 下午3:57:38 spccold Exp $
 */
public abstract class AbstractExchangeChannelGroup implements ExchangeChannelGroup{
	private final UUID id;
	protected volatile boolean closed = false;
	
	protected AbstractExchangeChannelGroup() {
		this.id = UUID.randomUUID();
	}
	
	@Override
	public UUID id() {
		return id;
	}

	@Override
	public void close() {
		close(0);
	}

	@Override
	public boolean isClosed() {
		return this.closed;
	}

	@Override
	public void oneway(byte[] data, RequestControl requestControl) throws SailfishException {
		next().oneway(data, requestControl);
	}

	@Override
	public ResponseFuture<byte[]> request(byte[] data, RequestControl requestControl) throws SailfishException {
		return next().request(data, requestControl);
	}

	@Override
	public void request(byte[] data, ResponseCallback<byte[]> callback, RequestControl requestControl)
			throws SailfishException {
		next().request(data, callback, requestControl);
	}
}
