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
import sailfish.remoting.RemotingConstants;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;
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
    public void encode(Protocol protocol, ByteBuf buffer) throws SailfishException {
        protocol.serialize(buffer);
    }

    @Override
    public Protocol decode(ByteBuf buffer) throws SailfishException {
        short magic = buffer.readShort();
        if(RemotingConstants.SAILFISH_MAGIC != magic){
            throw new SailfishException(ExceptionCode.BAD_PACKAGE,  
                "bad package, expected magic:"+RemotingConstants.SAILFISH_MAGIC+", but actual:"+magic+", current channel will be closed!");
        }
        
        int totalLength = buffer.readInt();
        byte compactByte = buffer.getByte(buffer.readerIndex());
        boolean request = ((compactByte & RequestProtocol.REQUEST_FLAG) != 0);
        Protocol protocol;
        
        //TODO may be replaced with RecycleProtocol in the future
        if(request){//request
            protocol = new RequestProtocol();
        }else{//response
            protocol = new ResponseProtocol();
        }
        protocol.deserialize(buffer, totalLength);
        return protocol;
    }
}
