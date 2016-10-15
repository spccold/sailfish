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
package sailfish.remoting.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import sailfish.common.Constants;
import sailfish.exceptions.ProtocolCodecException;
import sailfish.remoting.RemotingConstants;
import sailfish.remoting.protocol.DefaultRequestProtocol;
import sailfish.remoting.protocol.DefaultResponseProtocol;
import sailfish.remoting.protocol.Protocol;

/**
 * 
 * @author spccold
 * @version $Id: DefaultRemotingCodec.java, v 0.1 2016年10月15日 下午4:52:20 jileng Exp $
 */
public class DefaultRemotingCodec implements RemotingCodec{
    public static final DefaultRemotingCodec INSTANCE = new DefaultRemotingCodec();
    private DefaultRemotingCodec(){}
    @Override
    public void encode(Protocol protocol, ByteBuf buffer) throws ProtocolCodecException {
        protocol.serialize(new ByteBufOutputStream(buffer));
    }

    @Override
    public Protocol decode(ByteBuf buffer) throws ProtocolCodecException {
        int totalLength = buffer.readInt();
        int magic = buffer.readInt();
        if(RemotingConstants.SAILFISH_MAGIC != magic){
            throw new ProtocolCodecException(Constants.BAD_PACKAGE, 
                "bad package, expected magic:"+RemotingConstants.SAILFISH_MAGIC+"but actual:"+magic+", current channel will be closed!");
        }
        byte direction = buffer.readByte();
        Protocol protocol;
        //FIXME may be replaced with RecycleProtocol in the future
        if(RemotingConstants.DIRECTION_REQUEST == direction){//request
            protocol = new DefaultRequestProtocol();
        }else{//response
            protocol = new DefaultResponseProtocol();
        }
        protocol.deserialize(new ByteBufInputStream(buffer), totalLength);
        return protocol;
    }
}
