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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import org.junit.Assert;
import org.junit.Test;

import io.netty.util.CharsetUtil;
import sailfish.remoting.channel.ExchangeChannelGroup;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.configuration.ExchangeServerConfig;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.processors.ClientRequestProcessor;
import sailfish.remoting.processors.RequestProcessor;
import sailfish.remoting.processors.ServerRequestProcessor;

/**
 * @author spccold
 * @version $Id: ServerClientTest.java, v 0.1 2016年11月29日 下午10:09:08 spccold Exp $
 */
public class ServerClientTest {
	
	private static final int originPort = 13143;
	private static final byte[] clientRequestData = "client request".getBytes(CharsetUtil.UTF_8);
	private static final byte[] serverRequestData = "server request".getBytes(CharsetUtil.UTF_8);

	@Test
	public void test() throws Exception{
		ExchangeServerConfig serverConfig = new ExchangeServerConfig();
		serverConfig.address(new Address("localhost", originPort));
		List<RequestProcessor> processors = new ArrayList<>();
		processors.add(new ServerRequestProcessor());
		serverConfig.setRequestProcessors(processors);
		DefaultServer server = Exchanger.bind(serverConfig);
		server.start();
		
		ExchangeClientConfig clientConfig = new ExchangeClientConfig();
		processors.clear();
		processors.add(new ClientRequestProcessor());
		clientConfig.setRequestProcessors(processors);
		clientConfig.address(new Address("localhost", originPort));
		DefaultClient client = new DefaultClient(clientConfig);
		RequestControl control = new RequestControl();
		
		//wait server side deal negotiate finished
		LockSupport.parkNanos(1000 * 1000 * 100);
		//server send request to client
		Collection<ExchangeChannelGroup> channelGroups = server.listChannelGroups();
		Assert.assertNotNull(channelGroups);
		Assert.assertTrue(channelGroups.size() == 1);
		ExchangeChannelGroup channelGroup = (ExchangeChannelGroup)channelGroups.toArray()[0];
		ResponseFuture<byte[]> serverFuture = channelGroup.request(serverRequestData, control);
		Assert.assertArrayEquals(serverRequestData, serverFuture.get());
		
		//client send request to server
		ResponseFuture<byte[]> clientFuture = client.request(clientRequestData, control);
		Assert.assertArrayEquals(clientRequestData, clientFuture.get());
	}
}
