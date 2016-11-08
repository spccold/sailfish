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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import sailfish.remoting.channel.ExchangeChannel;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.configuration.ExchangeServerConfig;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.executor.SimpleExecutor;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;
import sailfish.remoting.utils.Bytes;
import sailfish.remoting.protocol.Protocol;

/**
 * 
 * @author spccold
 * @version $Id: ClientServerTest.java, v 0.1 2016年10月26日 下午4:36:23 jileng Exp $
 */
public class ClientServerTest {
    private static byte                               data[]                   = "hello sailfish!"
        .getBytes(CharsetUtil.UTF_8);
    private static AtomicInteger                      ONEWAY_PAYLOAD_GENERATOR = new AtomicInteger(0);
    private static final Map<Integer, CountDownLatch> RECORDS                  = new HashMap<>();
    private static ExchangeServer                     server;
    private static int                                originPort               = 13141;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ExchangeServerConfig serverConfig = new ExchangeServerConfig();
        serverConfig.address(new Address("localhost", originPort));
        server = Exchanger.bind(serverConfig, new MsgHandler<Protocol>() {
            @Override
            public void handle(ChannelHandlerContext ctx, Protocol msg) {
                if (msg.request()) {
                    RequestProtocol requestProtocol = (RequestProtocol) msg;
                    if (requestProtocol.oneway()) {
                        RECORDS.get(Bytes.bytes2int(requestProtocol.body())).countDown();
                    } else {
                        Assert.assertNotNull(requestProtocol.body());
                        Assert.assertTrue(requestProtocol.body().length > 0);
                        Assert.assertArrayEquals(data, requestProtocol.body());

                        ResponseProtocol responseProtocol = new ResponseProtocol();
                        responseProtocol.body(data);
                        responseProtocol.packetId(requestProtocol.packetId());
                        responseProtocol.result((byte) 0);
                        ctx.writeAndFlush(responseProtocol);
                    }
                }
            }
        });
        server.start();
    }

    @AfterClass
    public static void afterClass() {
        if (null != server) {
            server.close();
        }
    }

    private ExchangeClientConfig newBaseConfig(int port) {
        ExchangeClientConfig clientConfig = new ExchangeClientConfig();
        clientConfig.address(new Address("localhost", port));
        return clientConfig;
    }

    private void testSendAndReceive(ExchangeChannel client, RequestControl control) throws Exception {
        //sync oneway invoke
        CountDownLatch onewayLatch = new CountDownLatch(1);
        int payload = ONEWAY_PAYLOAD_GENERATOR.getAndIncrement();
        RECORDS.put(payload, onewayLatch);
        client.oneway(Bytes.int2bytes(payload), control);
        onewayLatch.await(2, TimeUnit.SECONDS);
        Assert.assertTrue(onewayLatch.getCount() == 0);

        //sync request-response invoke
        ResponseFuture<byte[]> future = client.request(data, control);
        byte[] result = future.get();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.length > 0);
        Assert.assertArrayEquals(data, result);

        //sync request-response invoke with timeout
        future = client.request(data, control);
        result = future.get(2, TimeUnit.SECONDS);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.length > 0);
        Assert.assertArrayEquals(data, result);

        //asynchronous callback invoke
        final CountDownLatch callbackLatch = new CountDownLatch(1);
        client.request(data, new ResponseCallback<byte[]>() {
            @Override
            public void handleResponse(byte[] resp) {
                Assert.assertNotNull(resp);
                Assert.assertTrue(resp.length > 0);
                Assert.assertArrayEquals(data, resp);
                callbackLatch.countDown();
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

        callbackLatch.await(2000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(callbackLatch.getCount() == 0);
    }

    @Test
    public void testSimpleChannel() throws Exception {
        ExchangeClientConfig config = newBaseConfig(originPort);
        config.connections(1);
        ExchangeChannel client = new DefaultExchangeClient(config);
        RequestControl control = new RequestControl();
        control.timeout(2000);
        //sent false
        testSendAndReceive(client, control);
        //sent true
        control.sent(true);
        testSendAndReceive(client, control);
    }

    @Test
    public void testSimpleLazyChannel() throws Exception {
        ExchangeClientConfig config = newBaseConfig(originPort);
        config.connections(1);
        config.setLazyConnection(true);
        ExchangeChannel client = new DefaultExchangeClient(config);
        RequestControl control = new RequestControl();
        control.timeout(2000);
        //sent false
        testSendAndReceive(client, control);
        //sent true
        control.sent(true);
        testSendAndReceive(client, control);
    }

    @Test
    public void testMultiConnsChannel() throws Exception {
        int conns = 3;
        ExchangeClientConfig config = newBaseConfig(originPort);
        config.connections(conns);
        ExchangeChannel client = new DefaultExchangeClient(config);
        RequestControl control = new RequestControl();
        control.timeout(2000);
        //sent false
        for (int i = 0; i < conns + 1; i++) {
            testSendAndReceive(client, control);
        }
        //sent true
        control.sent(true);
        for (int i = 0; i < conns + 1; i++) {
            testSendAndReceive(client, control);
        }
    }

    @Test
    public void testMultiConnsWithLazyChannel() throws Exception {
        int conns = 3;
        ExchangeClientConfig config = newBaseConfig(originPort);
        config.connections(conns);
        config.setLazyConnection(true);
        ExchangeChannel client = new DefaultExchangeClient(config);
        RequestControl control = new RequestControl();
        control.timeout(2000);
        //sent false
        for (int i = 0; i < conns + 1; i++) {
            testSendAndReceive(client, control);
        }
        //sent true
        control.sent(true);
        for (int i = 0; i < conns + 1; i++) {
            testSendAndReceive(client, control);
        }
    }

    @Test
    public void testTimeout() throws Exception {
        int port = originPort + 1;
        ExchangeServerConfig serverConfig = new ExchangeServerConfig();
        serverConfig.address(new Address("localhost", port));
        ExchangeServer server = Exchanger.bind(serverConfig, new MsgHandler<Protocol>() {
            @Override
            public void handle(ChannelHandlerContext ctx, Protocol msg) {
                //do nothing, request from remote peer will timeout
            }
        });
        server.start();

        byte[] requestData = "".getBytes(CharsetUtil.UTF_8);
        ExchangeChannel client = new DefaultExchangeClient(newBaseConfig(port));
        //test request-response
        RequestControl control = new RequestControl();
        ResponseFuture<byte[]> future = client.request(requestData, control);
        try {
            future.get(2000, TimeUnit.MILLISECONDS);
        } catch (Throwable cause) {
            Assert.assertTrue(cause instanceof SailfishException);
            Assert.assertEquals(ExceptionCode.TIMEOUT, ((SailfishException) cause).code());
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
                Assert.assertEquals(ExceptionCode.TIMEOUT, ((SailfishException) cause).code());
                latch.countDown();
            }

            @Override
            public Executor getExecutor() {
                return SimpleExecutor.instance();
            }
        }, control);

        latch.await(2500, TimeUnit.MILLISECONDS);
        Assert.assertTrue(latch.getCount() == 0);

        client.close();
        server.close();
    }
}
