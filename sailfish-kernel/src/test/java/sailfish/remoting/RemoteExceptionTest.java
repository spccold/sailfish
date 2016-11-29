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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import org.junit.Assert;
import org.junit.Test;

import io.netty.util.CharsetUtil;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.configuration.ExchangeServerConfig;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.processors.RemoteExceptionTestRequestHandler;
import sailfish.remoting.processors.RequestProcessor;

/**
 * @author spccold
 * @version $Id: RemoteExceptionTest.java, v 0.1 2016年11月29日 下午8:33:47 spccold Exp $
 */
public class RemoteExceptionTest {
	
	private static final int originPort = 13142;
	private static final String exceptionMsg = "invalid parameter";
	
	@Test
	public void test() throws Exception {
		ExchangeServerConfig serverConfig = new ExchangeServerConfig();
		serverConfig.address(new Address("localhost", originPort));
		List<RequestProcessor> processors = new ArrayList<>();
		processors.add(new RemoteExceptionTestRequestHandler());
		serverConfig.setRequestProcessors(processors);
		ExchangeServer server = Exchanger.bind(serverConfig);
		server.start();
		
		ExchangeClientConfig clientConfig = new ExchangeClientConfig();
		clientConfig.address(new Address("localhost", originPort));
		DefaultExchangeClient client = new DefaultExchangeClient(clientConfig);
		RequestControl control = new RequestControl();
		control.sent(false);
		control.opcode((short)(RemoteExceptionTestRequestHandler.OPCODE-1));
		ResponseFuture<byte[]> future = client.request(exceptionMsg.getBytes(CharsetUtil.UTF_8), control);
		try{
			future.get();
		}catch(Throwable cause){
			//1. test RequestProcessor not found
			Assert.assertTrue(cause instanceof SailfishException);
			Assert.assertTrue(cause.getMessage().contains("request processor not found"));
		}
		
		control.opcode(RemoteExceptionTestRequestHandler.OPCODE);
		future = client.request(exceptionMsg.getBytes(CharsetUtil.UTF_8), control);
		try{
			future.get();
		}catch(Throwable cause){
			//2. test normal request remote exception
			Assert.assertTrue(cause instanceof SailfishException);
			Assert.assertTrue(cause.getMessage().contains(exceptionMsg));
		}
		
		control.timeout(3000);
		final CountDownLatch latch = new CountDownLatch(1);
		client.request(exceptionMsg.getBytes(CharsetUtil.UTF_8), new ResponseCallback<byte[]>() {
			@Override
			public void handleResponse(byte[] resp) {
				Assert.assertFalse(true);
			}
			
			@Override
			public void handleException(Exception cause) {
				//3. test callback request remote exception
				Assert.assertTrue(cause instanceof SailfishException);
				Assert.assertTrue(cause.getMessage().contains(exceptionMsg));
				latch.countDown();
			}
			
			@Override
			public Executor getExecutor() {
				return null;
			}
		},control);
		
		latch.await();
		Assert.assertTrue(latch.getCount() == 0);
		
		client.close();
		server.close();
	}
}
