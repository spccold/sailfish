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
package sailfish;

import java.net.InetSocketAddress;

/**
 * 
 * @author spccold
 * @version $Id: ClientConfig.java, v 0.1 2016年10月4日 下午4:58:35 jileng Exp $
 */
public class ClientConfig extends EndpointConfig{
    private InetSocketAddress remoteAddress;
    private int connections;
    private int connectTimeout;
    private int timeout;
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
    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    public int getConnectTimeout() {
        return connectTimeout;
    }
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
