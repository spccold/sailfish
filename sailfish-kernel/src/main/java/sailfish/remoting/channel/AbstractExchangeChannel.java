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
package sailfish.remoting.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import sailfish.remoting.RequestControl;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.utils.PacketIdGenerator;

/**
 * 
 * @author spccold
 * @version $Id: AbstractExchangeChannel.java, v 0.1 2016年10月27日 上午11:42:58 jileng Exp $
 */
public abstract class AbstractExchangeChannel {

    protected Bootstrap newBootstrap(){
        Bootstrap boot = new Bootstrap();
        boot.channel(NioSocketChannel.class);
        boot.option(ChannelOption.TCP_NODELAY, true);
        //replace by heart beat
        boot.option(ChannelOption.SO_KEEPALIVE, false);
        return boot;
    }
    
    protected RequestProtocol newRequest(RequestControl requestControl){
        RequestProtocol protocol = new RequestProtocol();
        protocol.packetId(PacketIdGenerator.nextId());
        protocol.opcode(requestControl.opcode());
        protocol.compressType(requestControl.compressType());
        protocol.serializeType(requestControl.serializeType());
        return protocol;
    }
    protected abstract Channel doConnect(ExchangeClientConfig config) throws SailfishException;
}
