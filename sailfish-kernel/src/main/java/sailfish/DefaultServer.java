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
package sailfish;

import java.net.InetSocketAddress;

import sailfish.remoting.ExchangeServer;
import sailfish.remoting.MsgHandler;
import sailfish.remoting.protocol.Protocol;

/**
 * 
 * @author spccold
 * @version $Id: DefaultServer.java, v 0.1 2016年10月26日 下午4:18:30 jileng Exp $
 */
public class DefaultServer implements Server{
    private ExchangeServer exchangeServer;
    
    public DefaultServer(InetSocketAddress localAddress, MsgHandler<Protocol> handler) {
        exchangeServer= new ExchangeServer(localAddress, handler);
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

}
