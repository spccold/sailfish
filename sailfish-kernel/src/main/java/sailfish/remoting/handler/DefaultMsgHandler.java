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
package sailfish.remoting.handler;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sailfish.remoting.channel.ExchangeChannelGroup;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.processors.Processors;
import sailfish.remoting.processors.RequestProcessor;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;
import sailfish.remoting.utils.ParameterChecker;

/**
 * @author spccold
 * @version $Id: DefaultMsgHandler.java, v 0.1 2016年11月25日 上午11:51:51 spccold Exp $
 */
public class DefaultMsgHandler implements MsgHandler<Protocol> {

	private final Processors processors;

	public DefaultMsgHandler(List<RequestProcessor> requestProcessors) {
		ParameterChecker.checkNotNull(requestProcessors, "requestProcessors");
		processors = new Processors();
		for (RequestProcessor processor : requestProcessors) {
			processors.registerProcessor(processor.opcode(), processor);
		}
	}

	@Override
	public void handle(ExchangeChannelGroup channelGroup, Protocol msg) {
		if (msg.request()) {// request
			RequestProtocol requestProtocol = (RequestProtocol) msg;
			byte[] requestData = requestProtocol.body();
			int opcode = requestProtocol.opcode();
			RequestProcessor processor = processors.findProcessor(opcode);
			processor.handleRequest(requestData, new DefaultOutputImpl(channelGroup, requestProtocol));
		} else {// response
			channelGroup.getTracer().erase((ResponseProtocol) msg);
		}
	}

	private static class DefaultOutputImpl implements RequestProcessor.Output {
		private static final Logger logger = LoggerFactory.getLogger(DefaultOutputImpl.class);

		private final AtomicBoolean responsed = new AtomicBoolean(false);
		private final ExchangeChannelGroup channelGroup;
		private final RequestProtocol requestProtocol;

		public DefaultOutputImpl(ExchangeChannelGroup channelGroup, RequestProtocol requestProtocol) {
			this.channelGroup = channelGroup;
			this.requestProtocol = requestProtocol;
		}

		@Override
		public void response(byte[] responseData, boolean success) {
			if (!responsed.compareAndSet(false, true)) {
				return;
			}
			ResponseProtocol responseProtocol = new ResponseProtocol();
			responseProtocol.packetId(requestProtocol.packetId());
			responseProtocol.body(responseData);
			responseProtocol.result(success ? RemotingConstants.RESULT_SUCCESS : RemotingConstants.RESULT_FAIL);
			try {
				channelGroup.response(responseProtocol);
			} catch (SailfishException cause) {
				logger.error(String.format("response error, RequestProtocol[%s], ResponseProtocol[%s]",
						requestProtocol.toString(), responseProtocol.toString()), cause);
			}
		}
	}
}
