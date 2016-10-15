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
package sailfish.remoting;

import java.net.InetSocketAddress;

/**
 * 
 * @author spccold
 * @version $Id: RemotingConfig.java, v 0.1 2016年10月9日 下午9:09:03 jileng Exp $
 */
public class RemotingConfig {
    private InetSocketAddress remoteAddress;
    private int connections;
    private int connectTimeout;
    //enable channels Read/Write Splitting or not when connections greater than one
    private boolean enableReadWriteSplitting;
    //read channel ratio when enableReadWriteSplitting is true 
    private int readRatio;
    
    
    private int ioThreads;
    private String ioThreadName;
    
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public boolean isEnableReadWriteSplitting() {
        return enableReadWriteSplitting;
    }

    public void setEnableReadWriteSplitting(boolean enableReadWriteSplitting) {
        this.enableReadWriteSplitting = enableReadWriteSplitting;
    }

    public int getReadRatio() {
        return readRatio;
    }

    public void setReadRatio(int readRatio) {
        this.readRatio = readRatio;
    }

    public int getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(int ioThreads) {
        this.ioThreads = ioThreads;
    }

    public String getIoThreadName() {
        return ioThreadName;
    }

    public void setIoThreadName(String ioThreadName) {
        this.ioThreadName = ioThreadName;
    }
}
