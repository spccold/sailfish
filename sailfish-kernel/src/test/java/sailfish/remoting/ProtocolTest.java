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

import org.junit.Assert;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import sailfish.remoting.constants.CompressType;
import sailfish.remoting.constants.LangType;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.constants.SerializeType;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;

/**
 * 
 * @author spccold
 * @version $Id: ProtocolTest.java, v 0.1 2016年11月3日 上午11:17:13 jileng Exp $
 */
public class ProtocolTest {

    @Test
    public void testRequestProtocol() throws SailfishException{
        RequestProtocol send = new RequestProtocol();
        send.body(new byte[]{1,2,3,4});
        send.compressType(CompressType.NON_COMPRESS);
        send.heartbeat(false);
        send.langType(LangType.JAVA);
        send.oneway(false);
        send.opcode((short)1);
        send.packetId(1);
        send.serializeType(SerializeType.NON_SERIALIZE);
        
        ByteBuf output = ByteBufAllocator.DEFAULT.buffer(128);
        send.serialize(output);
        
        Assert.assertTrue(output.readShort() == RemotingConstants.SAILFISH_MAGIC);
        RequestProtocol receive = new RequestProtocol();
        receive.deserialize(output, output.readInt());
        Assert.assertArrayEquals(send.body(), receive.body());
        Assert.assertTrue(receive.compressType() == CompressType.NON_COMPRESS);
        Assert.assertFalse(receive.heartbeat());
        Assert.assertTrue(receive.langType() == LangType.JAVA);
        Assert.assertFalse(receive.oneway());
        Assert.assertTrue(1 == receive.opcode());
        Assert.assertTrue(1 == receive.packetId());
        Assert.assertTrue(receive.serializeType() == SerializeType.NON_SERIALIZE);
        
        
        output.clear();
        send.body(new byte[]{-1, -1, -1, -1});
        send.heartbeat(true);
        send.oneway(true);
        send.langType(LangType.CPP);
        send.serializeType(SerializeType.PROTOBUF_SERIALIZE);
        send.compressType(CompressType.LZ4_COMPRESS);
        send.opcode((short)100);
        send.packetId(1000);
        send.serialize(output);
        
        Assert.assertTrue(output.readShort() == RemotingConstants.SAILFISH_MAGIC);
        receive = new RequestProtocol();
        receive.deserialize(output, output.readInt());
        Assert.assertArrayEquals(send.body(), receive.body());
        Assert.assertTrue(receive.compressType() == CompressType.LZ4_COMPRESS);
        Assert.assertTrue(receive.heartbeat());
        Assert.assertTrue(receive.langType() == LangType.CPP);
        Assert.assertTrue(receive.oneway());
        Assert.assertTrue(100 == receive.opcode());
        Assert.assertTrue(1000 == receive.packetId());
        Assert.assertTrue(receive.serializeType() == SerializeType.PROTOBUF_SERIALIZE);
    }
    
    @Test
    public void testResponseProtocol() throws SailfishException{
        ResponseProtocol send = new ResponseProtocol();
        send.body(new byte[]{1,2,3,4});
        send.compressType(CompressType.GZIP_COMPRESS);
        send.heartbeat(false);
        send.packetId(1);
        send.result((byte)0);
        send.serializeType(SerializeType.JDK_SERIALIZE);

        ByteBuf output = ByteBufAllocator.DEFAULT.buffer(128);
        send.serialize(output);
        
        ResponseProtocol receive = new ResponseProtocol();
        Assert.assertTrue(output.readShort() == RemotingConstants.SAILFISH_MAGIC);
        receive.deserialize(output, output.readInt());
        Assert.assertArrayEquals(send.body(), receive.body());
        Assert.assertTrue(send.compressType() == CompressType.GZIP_COMPRESS);
        Assert.assertTrue(send.serializeType() == SerializeType.JDK_SERIALIZE);
        Assert.assertFalse(receive.heartbeat());
        Assert.assertTrue(1 == receive.packetId());
        Assert.assertTrue(0 == receive.result());
        
        
        output.clear();
        send.heartbeat(true);
        send.serialize(output);
        
        Assert.assertTrue(output.readShort() == RemotingConstants.SAILFISH_MAGIC);
        receive = new ResponseProtocol();
        receive.deserialize(output, output.readInt());
        Assert.assertTrue(receive.heartbeat());
    }
}

