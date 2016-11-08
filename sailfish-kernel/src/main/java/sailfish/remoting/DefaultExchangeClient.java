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

import sailfish.remoting.channel.ExchangeChannel;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.ResponseFuture;

/**
 * 
 * @author spccold
 * @version $Id: DefaultExchangeClient.java, v 0.1 2016年10月31日 上午10:47:17 jileng Exp $
 */
public class DefaultExchangeClient implements ExchangeClient{

    private ExchangeChannel exchanger;
    
    public DefaultExchangeClient(ExchangeClientConfig config) throws SailfishException{
        this.exchanger = Exchanger.connect(config);
    }
    
    @Override
    public void oneway(byte[] data, RequestControl requestControl) throws SailfishException{
        exchanger.oneway(data, requestControl);
    }

    @Override
    public ResponseFuture<byte[]> request(byte[] data, RequestControl requestControl) throws SailfishException{
        return exchanger.request(data, requestControl);
    }

    @Override
    public void request(byte[] data, ResponseCallback<byte[]> callback, RequestControl requestControl) throws SailfishException{
        exchanger.request(data, requestControl).setCallback(callback, requestControl.timeout());
    }

    @Override
    public void close(){
        this.exchanger.close();
    }
    
    @Override
    public void close(int timeout){
        this.exchanger.close(timeout);
    }

    @Override
    public boolean isClosed() {
        return exchanger.isClosed();
    }

    @Override
    public boolean isAvailable() {
        return exchanger.isAvailable();
    }
}
