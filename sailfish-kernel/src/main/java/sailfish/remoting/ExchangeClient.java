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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import sailfish.common.ResponseFuture;
import sailfish.remoting.protocol.DefaultRequestProtocol;
import sailfish.remoting.protocol.Protocol;

/**
 * 
 * @author spccold
 * @version $Id: ExchangeClient.java, v 0.1 2016年10月4日 下午4:13:49 jileng Exp $
 */
public class ExchangeClient implements Exchanger{
    private Channel channel;
    private volatile AtomicBoolean closed = new AtomicBoolean(false);
    private AtomicLong packageIdGenerator = new AtomicLong(0);
    public ExchangeClient(RemotingConfig config, MsgHandler<Protocol> handler) {
        this.channel = Transporters.connect(config, handler);
    }
  
    @Override
    public void close() {
        if(closed.compareAndSet(false, true)){
            channel.close();
        }
    }

    @Override
    public void close(int timeout) {
        //FIXME need do some special things
        close();
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void oneway(byte[] data) {
        
    }

    @Override
    public ResponseFuture<byte[]> request(byte[] data) {
        DefaultRequestProtocol protocol = new DefaultRequestProtocol();
        protocol.setOneway(true);
        protocol.setBody(data);
        protocol.setPackageId(packageIdGenerator.incrementAndGet());
        channel.send(protocol);
        ResponseFuture<byte[]> future = new BytesResponseFuture(protocol.getPackageId());
        Tracer.trace(protocol.getPackageId(), future);
        return future;
    }

    @Override
    public ResponseFuture<byte[]> request(byte[] data, int timeout) {
        return null;
    }

}
