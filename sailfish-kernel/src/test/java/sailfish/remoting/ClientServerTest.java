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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.netty.util.CharsetUtil;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.configuration.ExchangeServerConfig;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.executor.SimpleExecutor;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.processors.ClientServerNormalRequestTestRequestProcessor;
import sailfish.remoting.processors.ClientServerOnewayTestRequestProcessor;
import sailfish.remoting.processors.ClientServerTimeoutTestRequestProcessor;
import sailfish.remoting.processors.RequestProcessor;
import sailfish.remoting.utils.Bytes;

/**
 * 
 * @author spccold
 * @version $Id: ClientServerTest.java, v 0.1 2016年10月26日 下午4:36:23 jileng Exp $
 */
public class ClientServerTest {
    public volatile static byte                               data[]                   = "hello sailfish!"
        .getBytes(CharsetUtil.UTF_8);
    private volatile static AtomicInteger                      ONEWAY_PAYLOAD_GENERATOR = new AtomicInteger(0);
    public static final Map<Integer, CountDownLatch> RECORDS                  = new HashMap<>();
    private static DefaultServer                     server;
    private static int                                originPort               = 13141;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ExchangeServerConfig serverConfig = new ExchangeServerConfig();
        serverConfig.address(new Address("localhost", originPort));
        List<RequestProcessor> processors = new ArrayList<>(3);
        processors.add(new ClientServerOnewayTestRequestProcessor());
        processors.add(new ClientServerNormalRequestTestRequestProcessor());
        processors.add(new ClientServerTimeoutTestRequestProcessor());
        serverConfig.setRequestProcessors(processors);

        server = Exchanger.bind(serverConfig);
        server.start();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        if (null != server) {
        	server.close();
        }
    }

    private ExchangeClientConfig newBaseConfig(int port) {
        ExchangeClientConfig clientConfig = new ExchangeClientConfig();
        clientConfig.address(new Address("localhost", port));
        return clientConfig;
    }

    private void testSendAndReceive(DefaultClient client, RequestControl control) throws Exception {
        //sync oneway invoke
        CountDownLatch onewayLatch = new CountDownLatch(1);
        int payload = ONEWAY_PAYLOAD_GENERATOR.getAndIncrement();
        RECORDS.put(payload, onewayLatch);
        control.opcode(ClientServerOnewayTestRequestProcessor.OPCODE);
        client.oneway(Bytes.int2bytes(payload), control);
        onewayLatch.await(2, TimeUnit.SECONDS);
        Assert.assertTrue(onewayLatch.getCount() == 0);

        //sync request-response invoke
        control.opcode(ClientServerNormalRequestTestRequestProcessor.OPCODE);
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
                return SimpleExecutor.INSTANCE;
            }
        }, control);

        callbackLatch.await(2000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(callbackLatch.getCount() == 0);
    }

    @Test
    public void testSimpleChannel() throws Exception {
        ExchangeClientConfig config = newBaseConfig(originPort);
        config.connections((short)1);
        DefaultClient client = new DefaultClient(config);
        RequestControl control = new RequestControl();
        control.timeout(2000);
        //sent false
        testSendAndReceive(client, control);
        //sent true
        control.sent(true);
        testSendAndReceive(client, control);
        client.close();
    }

    @Test
    public void testSimpleLazyChannel() throws Exception {
        ExchangeClientConfig config = newBaseConfig(originPort);
        config.connections((short)1);
        config.setLazyConnection(true);
        DefaultClient client = new DefaultClient(config);
        RequestControl control = new RequestControl();
        control.timeout(2000);
        //sent false
        testSendAndReceive(client, control);
        //sent true
        control.sent(true);
        testSendAndReceive(client, control);
        client.close();
    }

    @Test
    public void testMultiConnsChannel() throws Exception {
        int conns = 3;
        ExchangeClientConfig config = newBaseConfig(originPort);
        config.connections((short)conns);
        DefaultClient client = new DefaultClient(config);
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
        client.close();
    }

    @Test
    public void testMultiConnsWithLazyChannel() throws Exception {
        int conns = 3;
        ExchangeClientConfig config = newBaseConfig(originPort);
        config.connections((short)conns);
        config.setLazyConnection(true);
        DefaultClient client = new DefaultClient(config);
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
        client.close();
    }

    @Test
    public void testReadWriteSplittingChannel() throws Exception {
        short writeConns = 2;
        short readConns = 2;
        ExchangeClientConfig config = newBaseConfig(originPort);
        config.enableReadWriteSplitting(true);
        config.connections((short)(writeConns + readConns));
        config.writeConnections(writeConns);
        DefaultClient client = new DefaultClient(config);
        RequestControl control = new RequestControl();
        control.timeout(2000);
        //sent false
        for (int i = 0; i < writeConns + readConns + 1; i++) {
            testSendAndReceive(client, control);
        }
        //sent true
        control.sent(true);
        for (int i = 0; i < writeConns + readConns + 1; i++) {
            testSendAndReceive(client, control);
        }
        client.close();
    }

    @Test
    public void testReadWriteSplittingWithLazyChannel() throws Exception {
        short writeConns = 2;
        short readConns = 2;
        ExchangeClientConfig config = newBaseConfig(originPort);
        config.enableReadWriteSplitting(true);
        config.setLazyConnection(true);
        config.connections((short)(writeConns + readConns));
        config.writeConnections(writeConns);
        DefaultClient client = new DefaultClient(config);
        RequestControl control = new RequestControl();
        control.timeout(2000);
        //sent false
        for (int i = 0; i < writeConns + readConns + 1; i++) {
            testSendAndReceive(client, control);
        }
        //sent true
        control.sent(true);
        for (int i = 0; i < writeConns + readConns + 1; i++) {
            testSendAndReceive(client, control);
        }
        client.close();
    }

    @Test
    public void testTimeout() throws Exception {
        int port = originPort;
        byte[] requestData = "".getBytes(CharsetUtil.UTF_8);
        DefaultClient client = new DefaultClient(newBaseConfig(port));
        //test request-response
        RequestControl control = new RequestControl();
        control.opcode(ClientServerTimeoutTestRequestProcessor.OPCODE);
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
                return SimpleExecutor.INSTANCE;
            }
        }, control);

        latch.await(2500, TimeUnit.MILLISECONDS);
        Assert.assertTrue(latch.getCount() == 0);

        client.close();
    }
}