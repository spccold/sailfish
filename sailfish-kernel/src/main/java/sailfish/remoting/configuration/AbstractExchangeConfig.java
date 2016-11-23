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

import sailfish.remoting.Address;
import sailfish.remoting.constants.RemotingConstants;
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
    protected int idleTimeout = RemotingConstants.DEFAULT_IDLE_TIMEOUT;
    protected int maxIdleTimeout = RemotingConstants.DEFAULT_MAX_IDLE_TIMEOUT;
    //check parameters
    protected void check(){
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
    public int idleTimeout() {
        return idleTimeout;
    }

    public void idleTimeout(int idleTimeout) {
        this.idleTimeout = ParameterChecker.checkBytePositive(idleTimeout);
    }
    public int maxIdleTimeout() {
        return maxIdleTimeout;
    }
    public void maxIdleTimeout(int maxIdleTimeout) {
        this.maxIdleTimeout = ParameterChecker.checkBytePositive(maxIdleTimeout);
    }
}
