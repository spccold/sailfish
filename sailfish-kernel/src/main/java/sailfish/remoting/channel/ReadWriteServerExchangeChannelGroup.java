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
package sailfish.remoting.channel;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sailfish.remoting.Tracer;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;

/**
 * @author spccold
 * @version $Id: ReadWriteServerExchangeChannelGroup.java, v 0.1 2016年11月24日 下午6:40:27 spccold Exp $
 */
public final class ReadWriteServerExchangeChannelGroup extends ReferenceCountedServerExchangeChannelGroup{
	
	private static final Logger logger = LoggerFactory.getLogger(ReadWriteServerExchangeChannelGroup.class);
	
	private ServerExchangeChannelGroup readGroup;
	private ServerExchangeChannelGroup writeGroup;
	
	private final MsgHandler<Protocol> msgHandler;
	private final Tracer tracer;
	
	public ReadWriteServerExchangeChannelGroup(MsgHandler<Protocol> msgHandler, UUID id, int connctions, int writeConnections) {
		super(id);

		this.msgHandler = msgHandler;
		this.tracer = new Tracer();
		//read to write, write to read
		this.writeGroup = new ServerExchangeChannelGroup(tracer, msgHandler, id, connctions - writeConnections);
		this.readGroup = new ServerExchangeChannelGroup(tracer, msgHandler, id, writeConnections);
	}
	
	public ServerExchangeChannelGroup getReadGroup(){
		return readGroup;
	}
	
	public ServerExchangeChannelGroup getWriteGroup(){
		return writeGroup;
	}
	
	@Override
	public ExchangeChannel next() throws SailfishException {
		try {
			return writeGroup.next();
		} catch (SailfishException cause) {
			logger.warn("writeGroup not available, try to choose readGroup", cause);
		}
		//try readGroup if writeGroup no available
		return readGroup.next();
	}

	@Override
	public boolean isAvailable() {
		if (writeGroup.isAvailable()) {
			return true;
		}
		return readGroup.isAvailable();
	}

	@Override
	public void close(int timeout) {
		throw new UnsupportedOperationException("close with timeout: "+timeout);
	}

	@Override
	public MsgHandler<Protocol> getMsgHander() {
		return msgHandler;
	}

	@Override
	public Tracer getTracer() {
		return tracer;
	}
}
