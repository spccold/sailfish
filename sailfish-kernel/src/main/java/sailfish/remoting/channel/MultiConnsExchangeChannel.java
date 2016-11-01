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

import sailfish.remoting.ExchangeClient;
import sailfish.remoting.RequestControl;
import sailfish.remoting.future.ResponseFuture;

/**
 * with multiple {@link SimpleExchangeChannel} or {@link LazyConnectionExchangeChannel} for one or more {@link ExchangeClient}
 * 
 * @author spccold
 * @version $Id: MultiConnsExchangeChannel.java, v 0.1 2016年10月26日 下午9:25:00 jileng Exp $
 */
public class MultiConnsExchangeChannel implements ExchangeChannel{

    @Override
    public void oneway(byte[] data) {
    }

    @Override
    public ResponseFuture<byte[]> request(byte[] data, RequestControl requestControl) {
        return null;
    }

    @Override
    public void close() throws InterruptedException{
    }

    @Override
    public void close(int timeout) throws InterruptedException{
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
