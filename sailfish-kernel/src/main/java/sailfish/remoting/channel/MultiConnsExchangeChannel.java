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

import java.util.concurrent.atomic.AtomicInteger;

import sailfish.remoting.RequestControl;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.ResponseFuture;

/**
 * with multiple {@link SimpleExchangeChannel} for one or more {@link ExchangeClient}
 * 
 * @author spccold
 * @version $Id: MultiConnsExchangeChannel.java, v 0.1 2016年10月26日 下午9:25:00 jileng Exp $
 */
public class MultiConnsExchangeChannel implements ExchangeChannel {
    private final SimpleExchangeChannel[] allChannels;
    private final SimpleExchangeChannel[] deadChannels;
    private final int                     connections;
    private final AtomicInteger           channelIndex = new AtomicInteger(0);

    public MultiConnsExchangeChannel(ExchangeClientConfig clientConfig) throws SailfishException {
        this.connections = clientConfig.connections();
        this.allChannels = new SimpleExchangeChannel[this.connections];
        this.deadChannels = new SimpleExchangeChannel[this.connections];
        try {
            for (int i = 0; i < this.connections; i++) {
                allChannels[i] = new SimpleExchangeChannel(clientConfig);
            }
        } catch (Throwable cause) {
            close(2000);
            throw cause;
        }
    }

    private void destory(int timeout) {
        for (int i = 0; i < allChannels.length; i++) {
            deadChannels[i] = null;
            if (null != allChannels[i]) {
                allChannels[i].close(timeout);
            }
        }
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
    public void request(byte[] data, ResponseCallback<byte[]> callback,
                        RequestControl requestControl) throws SailfishException {
        next().request(data, callback, requestControl);
    }

    @Override
    public void close() {
        close(0);
    }

    @Override
    public void close(int timeout) {
        destory(timeout);
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isAvailable() {
        //can hit most of the time
        if (deadChannels[0] == null || deadChannels[0].isAvailable()) {
            return true;
        }

        for (int i = 1; i < this.connections; i++) {
            if (deadChannels[i] == null || deadChannels[i].isAvailable()) {
                return true;
            }
        }
        return false;
    }

    //lock free
    private SimpleExchangeChannel next() throws SailfishException {
        int arrayIndex = 0;
        int currentIndex = channelIndex.getAndIncrement();
        for (int i = 0; i < this.connections; i++) {
            SimpleExchangeChannel currentChannel = allChannels[arrayIndex = ((currentIndex++) % this.connections)];
            if (currentChannel.isAvailable()) {
                if (null != deadChannels[arrayIndex]) {
                    deadChannels[arrayIndex] = null;
                }
                return currentChannel;
            }
            deadChannels[arrayIndex] = currentChannel;
        }
        throw new SailfishException(ExceptionCode.EXCHANGER_NOT_AVAILABLE, "exchanger is not available!");
    }
}