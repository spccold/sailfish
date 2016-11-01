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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.configuration.ExchangeServerConfig;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.executor.SimpleExecutor;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;
import sailfish.remoting.protocol.Protocol;

/**
 * 
 * @author spccold
 * @version $Id: ClientServerTest.java, v 0.1 2016年10月26日 下午4:36:23 jileng Exp $
 */
public class ClientServerTest {
    @Test
    public void testSendAndReceive() throws Exception{
        int port= 13141;
        final byte data[] = "hello sailfish!".getBytes(CharsetUtil.UTF_8);
        ExchangeServerConfig serverConfig = new ExchangeServerConfig();
        serverConfig.address(new Address("localhost", port));
        ExchangeServer server = Exchanger.bind(serverConfig, new MsgHandler<Protocol>() {
            @Override
            public void handle(ChannelHandlerContext ctx, Protocol msg) {
                if(msg.request()){
                    RequestProtocol requestProtocol = (RequestProtocol)msg;
                    Assert.assertNotNull(requestProtocol.getBody());
                    Assert.assertTrue(requestProtocol.getBody().length > 0);
                    Assert.assertArrayEquals(data, requestProtocol.getBody());
                    
                    ResponseProtocol responseProtocol = new ResponseProtocol();
                    responseProtocol.setBody(data);
                    responseProtocol.setPacketId(requestProtocol.getPacketId());
                    responseProtocol.setResult((byte)0);
                    ctx.writeAndFlush(responseProtocol);
                }
            }
        });
        server.start();
        
        ExchangeClientConfig clientConfig = new ExchangeClientConfig();
        clientConfig.address(new Address("localhost", port));
        ExchangeClient client = new DefaultExchangeClient(clientConfig);
        //test request-response
        RequestControl control = new RequestControl();
        ResponseFuture<byte[]> future = client.request(data, control);
        byte[] result = future.get();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.length > 0);
        Assert.assertArrayEquals(data, result);
        
        //test callback
        control.timeout(2000);
        final CountDownLatch latch = new CountDownLatch(1);
        client.request(data, new ResponseCallback<byte[]>() {
            @Override
            public void handleResponse(byte[] resp) {
                Assert.assertNotNull(resp);
                Assert.assertTrue(resp.length > 0);
                Assert.assertArrayEquals(data, resp);
                latch.countDown();
            }
            
            @Override
            public void handleException(Exception cause) {
                Assert.assertFalse(true);
            }

            @Override
            public Executor getExecutor() {
                return SimpleExecutor.instance();
            }
        }, control);
        
        latch.await(2000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(latch.getCount() == 0);
        
        server.close();
    }
    
    @Test
    public void testTimeout() throws Exception{
        int port = 13142;
        ExchangeServerConfig serverConfig = new ExchangeServerConfig();
        serverConfig.address(new Address("localhost", port));
        ExchangeServer server = Exchanger.bind(serverConfig, new MsgHandler<Protocol>() {
            @Override
            public void handle(ChannelHandlerContext ctx, Protocol msg) {
                //do nothing
            }
        });
        server.start();

        byte[] requestData = "".getBytes(CharsetUtil.UTF_8);
        ExchangeClientConfig clientConfig = new ExchangeClientConfig();
        clientConfig.address(new Address("localhost", port));
        ExchangeClient client = new DefaultExchangeClient(clientConfig);
        //test request-response
        RequestControl control = new RequestControl();
        ResponseFuture<byte[]> future = client.request(requestData, control);
        try{
            future.get(2000, TimeUnit.MILLISECONDS);
        }catch(Throwable cause){
            Assert.assertTrue(cause instanceof SailfishException);
            Assert.assertEquals(ExceptionCode.TIMEOUT, ((SailfishException)cause).code());
        }
        
        //test callback
        final CountDownLatch latch = new CountDownLatch(1);
        control.timeout(2000);
        client.request(requestData, new ResponseCallback<byte[]>() {
            @Override
            public void handleResponse(byte[] resp) {
            }
            
            @Override
            public void handleException(Exception cause) {
                Assert.assertTrue(cause instanceof SailfishException);
                Assert.assertEquals(ExceptionCode.TIMEOUT, ((SailfishException)cause).code());
                latch.countDown();
            }

            @Override
            public Executor getExecutor() {
                return SimpleExecutor.instance();
            }
        }, control);
        
        latch.await(2500, TimeUnit.MILLISECONDS);
        Assert.assertTrue(latch.getCount() == 0);

        server.close();
    }
}
