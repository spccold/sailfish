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
import sailfish.remoting.channel.MultiConnsExchangeChannel;
import sailfish.remoting.channel.ReadWriteSplittingExchangeChannel;
import sailfish.remoting.channel.SimpleExchangeChannel;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.configuration.ExchangeServerConfig;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.utils.ParameterChecker;

/**
 * 
 * @author spccold
 * @version $Id: Exchanger.java, v 0.1 2016年10月26日 下午11:40:38 jileng Exp $
 */
public class Exchanger {
    public static ExchangeChannel connect(ExchangeClientConfig config) throws SailfishException {
        ParameterChecker.checkNotNull(config, "ExchangeClientConfig");
        config.check();
        ExchangeChannel channel = null;
        switch (config.mode()) {
            case simple:
                channel = new SimpleExchangeChannel(config);
                break;
            case multiconns:
                channel = new MultiConnsExchangeChannel(config);
                break;
            case readwrite:
                channel = new ReadWriteSplittingExchangeChannel();
                break;
            default:
                throw new SailfishException(new IllegalArgumentException("invalid channel mode"));
        }
        return channel;
    }

    public static ExchangeServer bind(ExchangeServerConfig config, MsgHandler<Protocol> handler) throws SailfishException{
        config.check();
        return new ExchangeServer(config, handler);
    }
}
