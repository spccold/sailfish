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

import sailfish.remoting.ExchangeClient;
import sailfish.remoting.RequestControl;
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
        this.deadChannels = new SimpleExchangeChannel[this.connections];
        this.allChannels = new SimpleExchangeChannel[this.connections];
        try {
            for (int i = 0; i < this.connections; i++) {
                allChannels[i] = new SimpleExchangeChannel(clientConfig);
            }
        } catch (Throwable cause) {
            destory();
            throw cause;
        }
    }

    private void destory() {
        for (int i = 0; i < allChannels.length; i++) {
            deadChannels[i] = null;
            if (null != allChannels[i]) {
                allChannels[i].close();
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
    public void close() {
        close(0);
    }

    @Override
    public void close(int timeout) {
        //FIXME
        destory();
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isAvailable() {
        for(int i = 0; i< this.connections ; i++){
            if(deadChannels[i] == null){
                return true;
            }
        }
        return false;
    }
    
    private SimpleExchangeChannel next() throws SailfishException{
        int currentIndex = Math.abs(channelIndex.getAndIncrement() % this.connections);
        for(int i=0; i< this.connections; i++){
            SimpleExchangeChannel currentChannel = allChannels[(currentIndex++) % this.connections];
            if(currentChannel.isAvailable()){
                if(null != deadChannels[currentIndex]){
                    deadChannels[currentIndex] = null;
                }
                return currentChannel;
            }
            deadChannels[currentIndex] = currentChannel;
        }
        throw new SailfishException(ExceptionCode.EXCHANGER_NOT_AVAILABLE, "exchanger is not available!");
    }
}
