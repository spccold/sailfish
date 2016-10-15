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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import sailfish.remoting.Endpoint;
import sailfish.remoting.EndpointManager;
import sailfish.remoting.protocol.DefaultResponseProtocol;

/**
 * 
 * @author spccold
 * @version $Id: DefaultClientManager.java, v 0.1 2016年10月3日 下午2:05:42 jileng Exp $
 */
public class DefaultClientManager implements EndpointManager{
    public static final DefaultClientManager INSTANCE = new DefaultClientManager();
    private DefaultClientManager(){}
    
    @Override
    public Endpoint getEndpoint(EndpointConfig config){
        Endpoint client = new DefaultClient((ClientConfig)config, new SimpleChannelInboundHandler<DefaultResponseProtocol>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, DefaultResponseProtocol msg) throws Exception {
                doMessageReceived(msg);
            }
        });
        return client;
    }
    
    private void doMessageReceived(DefaultResponseProtocol response){
        
    }
}
