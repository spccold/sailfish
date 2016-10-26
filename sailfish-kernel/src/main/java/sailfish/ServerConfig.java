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

import sailfish.remoting.MsgHandler;
import sailfish.remoting.protocol.DefaultResponseProtocol;

/**
 * 
 * @author spccold
 * @version $Id: ServerConfig.java, v 0.1 2016年10月4日 下午5:01:33 jileng Exp $
 */
public class ServerConfig extends EndpointConfig{
    private MsgHandler<DefaultResponseProtocol> handler;

    public MsgHandler<DefaultResponseProtocol> getHandler() {
        return handler;
    }

    public void setHandler(MsgHandler<DefaultResponseProtocol> handler) {
        this.handler = handler;
    }
}
