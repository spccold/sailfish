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

import io.netty.channel.ChannelHandler;
import sailfish.remoting.ExchangeClient;
import sailfish.remoting.RemotingConfig;

/**
 * 
 * @author spccold
 * @version $Id: DefaultClient.java, v 0.1 2016年10月15日 下午11:34:59 jileng Exp $
 */
public class DefaultClient implements Client{
    private ExchangeClient client;
    
    public DefaultClient(ClientConfig config, ChannelHandler handler) {
        RemotingConfig remotingConfig = new RemotingConfig();
        remotingConfig.setRemoteAddress(config.getRemoteAddress());
        remotingConfig.setConnections(config.getConnections());
        remotingConfig.setConnectTimeout(config.getTimeout());
        client= new ExchangeClient(remotingConfig, handler);
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public void close(int timeout) {
        
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void oneway(Object request, RequestControl control) {
        
    }

    @Override
    public Object syncInvoke(Object request, RequestControl control) {
        return null;
    }

    @Override
    public ObjectResponseFuture futureInvoke(Object request, RequestControl control) {
        return null;
    }

    @Override
    public void callBackInvoke(Object request, RequestControl control, ResponseCallback callback) {
    }

}
