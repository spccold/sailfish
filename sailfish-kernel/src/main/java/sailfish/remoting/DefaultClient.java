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

import sailfish.remoting.channel.ExchangeChannelGroup;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.ResponseFuture;

/**
 * 
 * @author spccold
 * @version $Id: DefaultClient.java, v 0.1 2016年10月31日 上午10:47:17 jileng Exp $
 */
public class DefaultClient{

    private ExchangeChannelGroup exchanger;
    
    public DefaultClient(ExchangeClientConfig config) throws SailfishException{
        this.exchanger = Exchanger.connect(config);
    }
    
    public void oneway(byte[] data, RequestControl requestControl) throws SailfishException{
        checkAvailable();
        exchanger.oneway(data, requestControl);
    }

    public ResponseFuture<byte[]> request(byte[] data, RequestControl requestControl) throws SailfishException{
        checkAvailable();
        return exchanger.request(data, requestControl);
    }

    public void request(byte[] data, ResponseCallback<byte[]> callback, RequestControl requestControl) throws SailfishException{
        checkAvailable();
        exchanger.request(data, callback, requestControl);
    }

    public void close(){
        this.exchanger.close();
    }
    
    public void close(int timeout){
        this.exchanger.close(timeout);
    }

    public boolean isClosed() {
        return exchanger.isClosed();
    }

    public boolean isAvailable() {
        return exchanger.isAvailable();
    }
    
    private void checkAvailable() throws SailfishException{
        if(!isAvailable()){
            throw new SailfishException(ExceptionCode.EXCHANGER_NOT_AVAILABLE, "exchange channel not available");
        }
    }
}
