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

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.configuration.ExchangeServerConfig;
import sailfish.remoting.exceptions.RemotingException;
import sailfish.remoting.protocol.DefaultRequestProtocol;
import sailfish.remoting.protocol.DefaultResponseProtocol;
import sailfish.remoting.protocol.Protocol;

/**
 * 
 * @author spccold
 * @version $Id: ClientServerTest.java, v 0.1 2016年10月26日 下午4:36:23 jileng Exp $
 */
public class ClientServerTest {

    @Test
    public void testSendAndReceive() throws InterruptedException, ExecutionException, RemotingException{
        final byte data[] = "hello sailfish!".getBytes(CharsetUtil.UTF_8);
        ExchangeServerConfig serverConfig = new ExchangeServerConfig();
        serverConfig.address(new Address("localhost", 13141));
        ExchangeServer server = Exchanger.bind(serverConfig, new MsgHandler<Protocol>() {
            @Override
            public void handle(ChannelHandlerContext ctx, Protocol msg) {
                if(msg.request()){
                    DefaultRequestProtocol requestProtocol = (DefaultRequestProtocol)msg;
                    Assert.assertNotNull(requestProtocol.body());
                    Assert.assertTrue(requestProtocol.body().length > 0);
                    Assert.assertArrayEquals(data, requestProtocol.body());
                    
                    DefaultResponseProtocol responseProtocol = new DefaultResponseProtocol();
                    responseProtocol.setBody(data);
                    responseProtocol.setPackageId(requestProtocol.packageId());
                    responseProtocol.setResult((byte)1);
                    ctx.writeAndFlush(responseProtocol);
                }
            }
        });
        server.start();
        
        ExchangeClientConfig clientConfig = new ExchangeClientConfig();
        clientConfig.address(new Address("localhost", 13141));
        ExchangeClient client = new ExchangeClient(clientConfig);
        ResponseFuture<byte[]> future = client.request(data);
        byte[] result = future.get();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.length > 0);
        Assert.assertArrayEquals(data, result);
    }
}
