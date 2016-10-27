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

import sailfish.remoting.Address;
import sailfish.remoting.RemotingConstants;
import sailfish.remoting.channel.ChannelMode;
import sailfish.remoting.utils.ParameterChecker;
import sailfish.remoting.utils.StrUtils;

/**
 * 
 * @author spccold
 * @version $Id: ExchangeClientConfig.java, v 0.1 2016年10月26日 下午10:55:22 jileng Exp $
 */
public class ExchangeClientConfig extends AbstractExchangeConfig{
    private Address localAddress;
    //ms
    private int connectTimeout = RemotingConstants.DEFAULT_CONNECT_TIMEOUT;

    private int connections = 1;
    //enable channels Read/Write Splitting or not when connections greater than one
    private boolean enableReadWriteSplitting = false;
    //write channels ratio when enableReadWriteSplitting is true 
    private int writeRatio = 50;
    private int writeConnections;
    private boolean lazyConnection = false;
    
    public ChannelMode mode(){
        if(connections == 1){
            if(lazyConnection){
                return ChannelMode.lazy;
            }
            return ChannelMode.simple;
        }
        if(enableReadWriteSplitting){
            return ChannelMode.readwrite;
        }
        return ChannelMode.multiconns;
    }
    
    @Override
    public void check(){
        super.check();
        if(this.enableReadWriteSplitting){
            if(this.connections <= 1){
                throw new IllegalArgumentException("connections must greater than one when enableReadWriteSplitting");
            }
            this.writeConnections = connections * (writeRatio / 100);
            if(this.writeConnections == 0 || this.writeConnections == this.connections){
                throw new IllegalArgumentException("you should specify an appropriate writeRatio");
            }
        }
        if(StrUtils.isBlank(ioThreadName)){
            this.ioThreadName = "sailfish-client-io";
        }
        if(StrUtils.isBlank(codecThreadName)){
            this.codecThreadName= "sailfish-client-codec";
        }
    }

    public int connections() {
        return connections;
    }

    public void connections(int connections) {
        this.connections = ParameterChecker.checkPositive(connections, "connections");
    }

    public int Connecttimeout() {
        return connectTimeout;
    }

    public void Connecttimeout(int connectTimeout) {
        this.connectTimeout = ParameterChecker.checkPositive(connectTimeout, "connectTimeout");
    }

    public boolean isEnableReadWriteSplitting() {
        return enableReadWriteSplitting;
    }

    public void setEnableReadWriteSplitting(boolean enableReadWriteSplitting) {
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

    public Address localAddress() {
        return localAddress;
    }

    public void localAddress(Address localAddress) {
        this.localAddress = ParameterChecker.checkNotNull(localAddress, "localAddress");
    }
}
