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

import java.net.InetSocketAddress;

import sailfish.common.ResponseFuture;
import sailfish.remoting.protocol.Protocol;

/**
 * 
 * @author spccold
 * @version $Id: ExchangeServer.java, v 0.1 2016年10月26日 下午3:52:19 jileng Exp $
 */
public class ExchangeServer implements Exchanger{
    private InetSocketAddress localAddress;
    private MsgHandler<Protocol> handler;
    public ExchangeServer(InetSocketAddress localAddress, MsgHandler<Protocol> handler){
        this.localAddress = localAddress;
        this.handler = handler;
    }
    
    public void start(){
        Transporters.bind(localAddress, handler);
    }
    
    @Override
    public void close() {
        
    }

    @Override
    public void close(int timeout) {
        
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void oneway(byte[] data) {
        
    }

    @Override
    public ResponseFuture<byte[]> request(byte[] data) {
        return null;
    }

    @Override
    public ResponseFuture<byte[]> request(byte[] data, int timeout) {
        return null;
    }
}
