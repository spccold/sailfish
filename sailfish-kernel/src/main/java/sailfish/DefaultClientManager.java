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

import java.util.concurrent.ExecutionException;

import sailfish.exceptions.GetEndpointFailedException;
import sailfish.remoting.Endpoint;

/**
 * 
 * @author spccold
 * @version $Id: DefaultClientManager.java, v 0.1 2016年10月3日 下午2:05:42 jileng Exp $
 */
public class DefaultClientManager implements ClientManager{
    public static final DefaultClientManager INSTANCE = new DefaultClientManager();
    private DefaultClientManager(){}
    
    @Override
    public Endpoint getEndpoint() throws GetEndpointFailedException{
        try {
            return getAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new GetEndpointFailedException("get client fail!", e);
        }
    }

    @Override
    public ClientFuture getAsync() {
        return null;
    }
}
