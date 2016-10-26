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
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import sailfish.common.ResponseFuture;
import sailfish.exceptions.GetEndpointFailedException;
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
    public void testSendAndReceive() throws GetEndpointFailedException, InterruptedException, ExecutionException{
        final byte data[] = "hello sailfish!".getBytes(CharsetUtil.UTF_8);
        ExchangeServer server = new ExchangeServer(new InetSocketAddress("localhost", 13141), new MsgHandler<Protocol>() {
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
        
        RemotingConfig config = new RemotingConfig();
        config.setConnections(1);
        config.setRemoteAddress(new InetSocketAddress("localhost", 13141));
        config.setIoThreads(Runtime.getRuntime().availableProcessors());
        config.setIoThreadName("sailfish-io-client");
        ExchangeClient client = new ExchangeClient(config);
        ResponseFuture<byte[]> future = client.request(data);
        byte[] result = future.get();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.length > 0);
        Assert.assertArrayEquals(data, result);
    }
}
