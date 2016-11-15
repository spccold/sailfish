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
import java.util.concurrent.atomic.AtomicInteger;

import sailfish.remoting.RequestControl;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.ResponseFuture;

/**
 * similar to {@link MultiConnsExchangeChannel}, but support read write splitting, 
 * this means some connections used for write only and other connections used for read only
 * 
 * @author spccold
 * @version $Id: ReadWriteSplittingExchangeChannel.java, v 0.1 2016年10月26日 下午9:29:17 jileng Exp $
 */
public class ReadWriteSplittingExchangeChannel extends AbstractExchangeChannel {
    private final int                     writeConns;
    private final int                     readConns;
    private final SimpleExchangeChannel[] writeChannels;
    private final SimpleExchangeChannel[] readChannels;
    private final SimpleExchangeChannel[] deadWriteChannels;
    private final SimpleExchangeChannel[] deadReadChannels;
    private final AtomicInteger           writeChannelIndex = new AtomicInteger(0);
    private final AtomicInteger           readChannelIndex  = new AtomicInteger(0);

    public ReadWriteSplittingExchangeChannel(ExchangeClientConfig config) throws SailfishException {
        //TODO replace by channel id(support in netty 4.1.x)?
        config.uuid(UUID.randomUUID());
        this.writeConns = config.writeConnections();
        this.readConns = config.connections() - writeConns;
        //TODO set channel write only via netty options?(selector only focus on SelectionKey.OP_WRITE)
        this.writeChannels = new SimpleExchangeChannel[this.writeConns];
        this.deadWriteChannels = new SimpleExchangeChannel[this.writeConns];
        //TODO set channel read only via netty options?(selector only focus on SelectionKey.OP_READ)
        this.readChannels = new SimpleExchangeChannel[this.readConns];
        this.deadReadChannels = new SimpleExchangeChannel[this.readConns];
        try {
            for (int i = 0; i < this.writeConns; i++) {
                config.setWriteConnection(true);
                config.channelIndex(i);
                this.writeChannels[i] = new SimpleExchangeChannel(config);
            }
            for (int i = 0; i < this.readConns; i++) {
                config.setWriteConnection(false);
                config.channelIndex(i);
                this.readChannels[i] = new SimpleExchangeChannel(config);
            }
        } catch (Throwable cause) {
            close(2000);
            throw cause;
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
        if(this.isClosed()){
            return;
        }
        synchronized (this) {
            if(this.isClosed()){
                return;
            }
            this.closed = true;

            for (int i = 0; i < this.writeConns; i++) {
                this.deadWriteChannels[i] = null;
                if (null != this.writeChannels[i]) {
                    this.writeChannels[i].close(timeout);
                }
            }
            for (int i = 0; i < this.readConns; i++) {
                this.deadReadChannels[i] = null;
                if (null != this.readChannels[i]) {
                    this.readChannels[i].close(timeout);
                }
            }
        }
    }

    @Override
    public boolean isAvailable() {
        if(this.isClosed()){
            return false;
        }
        
        //can hit most of the time
        if (deadWriteChannels[0] == null || deadWriteChannels[0].isAvailable()) {
            return true;
        }

        for (int i = 1; i < this.writeConns; i++) {
            if (deadWriteChannels[i] == null || deadWriteChannels[i].isAvailable()) {
                return true;
            }
        }
        for (int i = 0; i < this.readConns; i++) {
            if (deadReadChannels[i] == null || deadReadChannels[i].isAvailable()) {
                return true;
            }
        }
        return false;
    }

    //lock free
    private SimpleExchangeChannel next() throws SailfishException {
        int arrayIndex = 0;
        //select write channel first 
        int currentIndex = writeChannelIndex.getAndIncrement();
        for (int i = 0; i < this.writeConns; i++) {
            SimpleExchangeChannel currentChannel = writeChannels[arrayIndex = Math
                .abs((currentIndex++) % this.writeConns)];
            if (currentChannel.isAvailable()) {
                if (null != deadWriteChannels[arrayIndex]) {
                    deadWriteChannels[arrayIndex] = null;
                }
                return currentChannel;
            }
            deadWriteChannels[arrayIndex] = currentChannel;
        }

        //if all write channel unavailable, try to select read channel
        arrayIndex = 0;
        currentIndex = readChannelIndex.getAndIncrement();
        for (int i = 0; i < this.readConns; i++) {
            SimpleExchangeChannel currentChannel = readChannels[arrayIndex = Math
                .abs((currentIndex++) % this.readConns)];
            if (currentChannel.isAvailable()) {
                if (null != deadReadChannels[arrayIndex]) {
                    deadReadChannels[arrayIndex] = null;
                }
                return currentChannel;
            }
            deadReadChannels[arrayIndex] = currentChannel;
        }
        throw new SailfishException(ExceptionCode.EXCHANGER_NOT_AVAILABLE, "exchanger is not available!");
    }
}
