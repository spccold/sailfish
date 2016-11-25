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

/**
 * @author spccold
 * @version $Id: ClientServerTimeoutTestRequestProcessor.java, v 0.1 2016年11月25日 下午4:21:57 spccold Exp $
 */
public class ClientServerTimeoutTestRequestProcessor implements RequestProcessor{
	public static final short OPCODE = 2;
	@Override
	public Executor executor() {
		return null;
	}

	@Override
	public short opcode() {
		return OPCODE;
	}

	@Override
	public void handleRequest(byte[] requestData, Output output) {
		//do nothing, request from remote peer will timeout
	}

	@Override
	public void onRejectedExecutionException(byte[] requestData, Output output) {
	}
}
