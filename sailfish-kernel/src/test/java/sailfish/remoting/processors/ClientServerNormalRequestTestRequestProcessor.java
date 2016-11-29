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
package sailfish.remoting.processors;

import java.util.concurrent.Executor;

import org.junit.Assert;

import sailfish.remoting.ClientServerTest;

/**
 * @author spccold
 * @version $Id: ClientServerNormalRequestTestRequestProcessor.java, v 0.1 2016年11月25日 下午4:24:26 spccold Exp $
 */
public class ClientServerNormalRequestTestRequestProcessor implements RequestProcessor{
	public static final short OPCODE = 1;
	@Override
	public Executor executor() {
		return null;
	}

	@Override
	public short opcode() {
		return OPCODE;
	}

	@Override
	public void handleRequest(Request request, Output output) {
		Assert.assertNotNull(request.getRequestData());
        Assert.assertTrue(request.getRequestData().length > 0);
        Assert.assertArrayEquals(ClientServerTest.data, request.getRequestData());
        output.response(new Response(true, ClientServerTest.data));
	}

	@Override
	public void onRejectedExecutionException(Request request, Output output) {
	}
}
