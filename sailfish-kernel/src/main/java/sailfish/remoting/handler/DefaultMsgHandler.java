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
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.Recycler;
import sailfish.remoting.channel.ExchangeChannelGroup;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.executor.SimpleExecutor;
import sailfish.remoting.processors.Processors;
import sailfish.remoting.processors.RequestProcessor;
import sailfish.remoting.processors.Response;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;
import sailfish.remoting.utils.ParameterChecker;
import sailfish.remoting.utils.StrUtils;

/**
 * @author spccold
 * @version $Id: DefaultMsgHandler.java, v 0.1 2016年11月25日 上午11:51:51 spccold Exp $
 */
public class DefaultMsgHandler implements MsgHandler<Protocol> {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMsgHandler.class);

	private final Processors processors;

	public DefaultMsgHandler(List<RequestProcessor> requestProcessors) {
		ParameterChecker.checkNotNull(requestProcessors, "requestProcessors");
		processors = new Processors();
		for (RequestProcessor processor : requestProcessors) {
			processors.registerProcessor(processor.opcode(), processor);
		}
	}

	@Override
	public void handle(final ExchangeChannelGroup channelGroup, Protocol msg) {
		if (msg.request()) {
			RequestProtocol requestProtocol = (RequestProtocol) msg;
			handleRequest(channelGroup, requestProtocol);
		} else {// response
			channelGroup.getTracer().erase((ResponseProtocol) msg);
		}
	}

	private void handleRequest(final ExchangeChannelGroup channelGroup, final RequestProtocol requestProtocol) {
		int opcode = requestProtocol.opcode();
		final RequestProcessor processor = processors.findProcessor(opcode);
		//cache responseProtocol in DefaultEventExecutor's inner thread(benefit from FastThreadLocalThread)
		final ResponseProtocol responseProtocol = ResponseProtocol.newInstance();
		if (null == processor) {
			String errorMsg = String.format("request processor not found for opcode[%d]", opcode);
			doResponse(channelGroup, requestProtocol, newErrorResponse(responseProtocol, requestProtocol.packetId(), errorMsg));
			return;
		}

		Executor executor = (null != processor.executor()) ? processor.executor() : SimpleExecutor.INSTANCE;
		//cache output instance in DefaultEventExecutor's inner thread(benefit from FastThreadLocalThread)
		final DefaultOutputImpl output = DefaultOutputImpl.newInstance(channelGroup, requestProtocol, responseProtocol);
		try {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						processor.handleRequest(requestProtocol.toRequest(), output);
					} catch (Throwable cause) {
						String errorMsg = StrUtils.exception2String(cause);
						doResponse(channelGroup, requestProtocol, newErrorResponse(responseProtocol, requestProtocol.packetId(),
								errorMsg));
					}
				}
			});
		} catch (RejectedExecutionException cause) {
			processor.onRejectedExecutionException(requestProtocol.toRequest(), output);
		}
	}

	static final class DefaultOutputImpl implements RequestProcessor.Output {
		private static final Recycler<DefaultOutputImpl> RECYCLER = new Recycler<DefaultOutputImpl>() {
			@Override
			protected DefaultOutputImpl newObject(Handle<DefaultOutputImpl> handle) {
				return new DefaultOutputImpl(handle);
			}
		};

		private static DefaultOutputImpl newInstance(ExchangeChannelGroup channelGroup,
				RequestProtocol requestProtocol, ResponseProtocol responseProtocol) {
			DefaultOutputImpl output = RECYCLER.get();
			output.responseProtocol = responseProtocol;
			output.channelGroup = channelGroup;
			output.requestProtocol = requestProtocol;
			return output;
		}

		private final Recycler.Handle<DefaultOutputImpl> handle;
		private ResponseProtocol responseProtocol;
		private ExchangeChannelGroup channelGroup;
		private RequestProtocol requestProtocol;

		private DefaultOutputImpl(Recycler.Handle<DefaultOutputImpl> handle) {
			this.handle = handle;
		}

		public void recycle() {
			responseProtocol = null;
			channelGroup = null;
			requestProtocol = null;
			handle.recycle(this);
		}

		@Override
		public void response(Response response) {
			if (null == response) {
				recycle();
				return;
			}
			responseProtocol.packetId(requestProtocol.packetId());
			responseProtocol.serializeType(response.getSerializeType());
			responseProtocol.compressType(response.getCompressType());
			responseProtocol.body(response.getResponseData());
			responseProtocol
					.result(response.isSuccess() ? RemotingConstants.RESULT_SUCCESS : RemotingConstants.RESULT_FAIL);
			doResponse(channelGroup, requestProtocol, responseProtocol);
			recycle();
		}
	}

	private static void doResponse(ExchangeChannelGroup channelGroup, RequestProtocol requestProtocol,
			ResponseProtocol responseProtocol) {
		try {
			channelGroup.response(responseProtocol);
		} catch (SailfishException cause) {
			logger.error(String.format("response error, RequestProtocol[%s], ResponseProtocol[%s]",
					requestProtocol.toString(), responseProtocol.toString()), cause);
		}finally {
			requestProtocol.recycle();
		}
	}
	
	 public ResponseProtocol newErrorResponse(ResponseProtocol instance, int packetId, String errorStack){
		 instance.packetId(packetId);
		 instance.errorStack(errorStack);
		 instance.result(RemotingConstants.RESULT_FAIL);
	     return instance;
	 }
}