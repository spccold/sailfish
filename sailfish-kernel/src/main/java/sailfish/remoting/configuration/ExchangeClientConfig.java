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
package sailfish.remoting.configuration;

import java.util.UUID;

import sailfish.remoting.channel.ChannelMode;
import sailfish.remoting.channel.ReadWriteSplittingExchangeChannel;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.utils.ParameterChecker;
import sailfish.remoting.utils.StrUtils;

/**
 * 
 * @author spccold
 * @version $Id: ExchangeClientConfig.java, v 0.1 2016年10月26日 下午10:55:22 jileng Exp $
 */
public class ExchangeClientConfig extends AbstractExchangeConfig {
    //milliseconds
    private int     connectTimeout           = RemotingConstants.DEFAULT_CONNECT_TIMEOUT;
    private int     reconnectInterval        = RemotingConstants.DEFAULT_RECONNECT_INTERVAL;

    private int     connections              = 1;
    //enable channels Read/Write Splitting or not when connections greater than one
    private boolean enableReadWriteSplitting = false;
    /**
     * write channels ratio when enableReadWriteSplitting is true, take writeConnections first
     * if you don't specify writeConnections, writeRatio will be used
     */
    private int     writeRatio               = 50;
    private int     writeConnections;
    private boolean lazyConnection           = false;
    /**
     * inner use, user should't set this field
     * identify current connection is write connection when enableReadWriteSplitting is true
     */
    private boolean writeConnection          = false;
    /**
     * inner use, user should't set this field
     * identify current {@link ReadWriteSplittingExchangeChannel} when enableReadWriteSplitting is true
     */
    private UUID  uuid;
    //inner use, user should't set this field
    private int channelIndex;
    
    public ChannelMode mode() {
        if (connections == 1) {
            return ChannelMode.simple;
        }
        if (enableReadWriteSplitting) {
            return ChannelMode.readwrite;
        }
        return ChannelMode.multiconns;
    }

    @Override
    public void check() {
        super.check();
        if (this.enableReadWriteSplitting) {
            if (this.connections <= 1) {
                throw new IllegalArgumentException("connections must greater than one when enableReadWriteSplitting");
            }
            if (this.writeConnections == 0) {
                this.writeConnections = connections * (writeRatio / 100);
            }
            if (this.writeConnections == 0 || this.writeConnections == this.connections) {
                throw new IllegalArgumentException("writeConnections:" + this.writeConnections
                                                   + ", you should specify an appropriate writeConnections or writeRatio");
            }
        }
        if (StrUtils.isBlank(codecThreadName)) {
            this.codecThreadName = "sailfish-client-codec";
        }
    }

    public int connections() {
        return connections;
    }

    public void connections(int connections) {
        this.connections = ParameterChecker.checkPositive(connections, "connections");
    }

    public int writeConnections() {
        return writeConnections;
    }

    public void writeConnections(int writeConnections) {
        this.writeConnections = ParameterChecker.checkPositive(writeConnections, "writeConnections");
    }

    public int reconnectInterval() {
        return reconnectInterval;
    }

    public void reconnectInterval(int reconnectInterval) {
        this.reconnectInterval = ParameterChecker.checkPositive(reconnectInterval, "reconnectInterval");
    }

    public int connectTimeout() {
        return connectTimeout;
    }

    public void connectTimeout(int connectTimeout) {
        this.connectTimeout = ParameterChecker.checkPositive(connectTimeout, "connectTimeout");
    }

    public boolean enableReadWriteSplitting() {
        return enableReadWriteSplitting;
    }

    public void enableReadWriteSplitting(boolean enableReadWriteSplitting) {
        this.enableReadWriteSplitting = enableReadWriteSplitting;
    }

    public int writeRatio() {
        return writeRatio;
    }

    public void writeRatio(int writeRatio) {
        if (writeRatio <= 0 || writeRatio > 100) {
            throw new IllegalArgumentException("writeRatio: " + writeRatio + " (expected: 0 < writeRatio <= 100)");
        }
        this.writeRatio = writeRatio;
    }

    public boolean isLazyConnection() {
        return lazyConnection;
    }

    public void setLazyConnection(boolean lazyConnection) {
        this.lazyConnection = lazyConnection;
    }

    public boolean isWriteConnection() {
        return writeConnection;
    }

    public void setWriteConnection(boolean writeConnection) {
        this.writeConnection = writeConnection;
    }

    public UUID uuid() {
        return uuid;
    }

    public void uuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int channelIndex() {
        return channelIndex;
    }

    public void channelIndex(int channelIndex) {
        this.channelIndex = channelIndex;
    }
    
    public ExchangeClientConfig deepCopy(){
        ExchangeClientConfig copy = new ExchangeClientConfig();
        //from parent
        copy.address(this.address);
        copy.idleTimeout(this.idleTimeout);
        copy.maxIdleTimeout(this.maxIdleTimeout);
        copy.codecThreads(this.codecThreads);
        copy.codecThreadName(this.codecThreadName);
        //from self
        copy.connectTimeout(this.connectTimeout);
        copy.connections(this.connections);
        copy.setLazyConnection(this.lazyConnection);
        if(this.enableReadWriteSplitting){
            copy.enableReadWriteSplitting(this.enableReadWriteSplitting);
            copy.writeConnections(this.writeConnections);
            copy.setWriteConnection(this.writeConnection);
            copy.writeRatio(this.writeRatio);
            copy.channelIndex(this.channelIndex);
            copy.uuid(this.uuid);
        }
        return copy;
    }
}
