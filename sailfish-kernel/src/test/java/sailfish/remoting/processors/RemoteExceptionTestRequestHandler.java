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

import io.netty.util.CharsetUtil;

/**
 * @author spccold
 * @version $Id: RemoteExceptionTestRequestHandler.java, v 0.1 2016年11月29日 下午8:42:49 spccold Exp $
 */
public class RemoteExceptionTestRequestHandler implements RequestProcessor{
	public static final short OPCODE = 0;
	
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
		throw new RuntimeException(new String(request.getRequestData(), CharsetUtil.UTF_8));
	}

	@Override
	public void onRejectedExecutionException(Request request, Output output) {
	}

}
