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
package sailfish.remoting.configuration;

import java.util.ArrayList;
import java.util.List;

import sailfish.remoting.Address;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.processors.RequestProcessor;
import sailfish.remoting.utils.ParameterChecker;

/**
 * 
 * @author spccold
 * @version $Id: AbstractExchangeConfig.java, v 0.1 2016年10月27日 下午5:43:27 jileng Exp $
 */
public abstract class AbstractExchangeConfig {   
    /**
     * remote address for client, local address for server
     */
    protected Address address;
    // in seconds
    protected byte idleTimeout = RemotingConstants.DEFAULT_IDLE_TIMEOUT;
    protected byte maxIdleTimeout = RemotingConstants.DEFAULT_MAX_IDLE_TIMEOUT;
    
    protected List<RequestProcessor> requestProcessors = new ArrayList<>(0);
    
    //check parameters
    public void check(){
        ParameterChecker.checkNotNull(address, "address");
        ParameterChecker.checkBytePositive(idleTimeout);
        ParameterChecker.checkBytePositive(maxIdleTimeout);
        if(idleTimeout > maxIdleTimeout){
            throw new IllegalArgumentException("maxIdleTimeout must be greater than idleTimeout");
        }
    }
    
    public Address address() {
        return address;
    }
    public void address(Address address) {
        this.address = ParameterChecker.checkNotNull(address, "address");
    }
    public byte idleTimeout() {
        return idleTimeout;
    }

    public void idleTimeout(byte idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
    public byte maxIdleTimeout() {
        return maxIdleTimeout;
    }
    public void maxIdleTimeout(byte maxIdleTimeout) {
        this.maxIdleTimeout = maxIdleTimeout;
    }

	public List<RequestProcessor> getRequestProcessors() {
		return requestProcessors;
	}

	public void setRequestProcessors(List<RequestProcessor> requestProcessors) {
		this.requestProcessors = ParameterChecker.checkNotNull(requestProcessors, "requestProcessors");
	}
}
