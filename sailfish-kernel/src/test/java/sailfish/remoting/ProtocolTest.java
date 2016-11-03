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
        send.setBody(new byte[]{1,2,3,4});
        send.setCompressType(CompressType.NON_COMPRESS);
        send.setHeartbeat(false);
        send.setLangType(LangType.JAVA);
        send.setOneway(false);
        send.setOpcode((short)1);
        send.setPacketId(1);
        send.setSerializeType(SerializeType.NON_SERIALIZE);
        
        ByteBuf output = ByteBufAllocator.DEFAULT.buffer(128);
        send.serialize(output);
        
        Assert.assertTrue(output.readShort() == RemotingConstants.SAILFISH_MAGIC);
        RequestProtocol receive = new RequestProtocol();
        receive.deserialize(output, output.readInt());
        Assert.assertArrayEquals(send.getBody(), receive.getBody());
        Assert.assertTrue(receive.getCompressType() == CompressType.NON_COMPRESS);
        Assert.assertFalse(receive.isHeartbeat());
        Assert.assertTrue(receive.getLangType() == LangType.JAVA);
        Assert.assertFalse(receive.isOneway());
        Assert.assertTrue(1 == receive.getOpcode());
        Assert.assertTrue(1 == receive.getPacketId());
        Assert.assertTrue(receive.getSerializeType() == SerializeType.NON_SERIALIZE);
        
        
        output.clear();
        send.setBody(new byte[]{-1, -1, -1, -1});
        send.setHeartbeat(true);
        send.setOneway(true);
        send.setLangType(LangType.CPP);
        send.setSerializeType(SerializeType.PROTOBUF_SERIALIZE);
        send.setCompressType(CompressType.LZ4_COMPRESS);
        send.setOpcode((short)100);
        send.setPacketId(1000);
        send.serialize(output);
        
        Assert.assertTrue(output.readShort() == RemotingConstants.SAILFISH_MAGIC);
        receive = new RequestProtocol();
        receive.deserialize(output, output.readInt());
        Assert.assertArrayEquals(send.getBody(), receive.getBody());
        Assert.assertTrue(receive.getCompressType() == CompressType.LZ4_COMPRESS);
        Assert.assertTrue(receive.isHeartbeat());
        Assert.assertTrue(receive.getLangType() == LangType.CPP);
        Assert.assertTrue(receive.isOneway());
        Assert.assertTrue(100 == receive.getOpcode());
        Assert.assertTrue(1000 == receive.getPacketId());
        Assert.assertTrue(receive.getSerializeType() == SerializeType.PROTOBUF_SERIALIZE);
    }
    
    @Test
    public void testResponseProtocol() throws SailfishException{
        ResponseProtocol send = new ResponseProtocol();
        send.setBody(new byte[]{1,2,3,4});
        send.setCompressType(CompressType.GZIP_COMPRESS);
        send.setHeartbeat(false);
        send.setPacketId(1);
        send.setResult((byte)0);
        send.setSerializeType(SerializeType.JDK_SERIALIZE);

        ByteBuf output = ByteBufAllocator.DEFAULT.buffer(128);
        send.serialize(output);
        
        ResponseProtocol receive = new ResponseProtocol();
        Assert.assertTrue(output.readShort() == RemotingConstants.SAILFISH_MAGIC);
        receive.deserialize(output, output.readInt());
        Assert.assertArrayEquals(send.getBody(), receive.getBody());
        Assert.assertTrue(send.getCompressType() == CompressType.GZIP_COMPRESS);
        Assert.assertTrue(send.getSerializeType() == SerializeType.JDK_SERIALIZE);
        Assert.assertFalse(receive.isHeartbeat());
        Assert.assertTrue(1 == receive.getPacketId());
        Assert.assertTrue(0 == receive.getResult());
        
        
        output.clear();
        send.setHeartbeat(true);
        send.serialize(output);
        
        Assert.assertTrue(output.readShort() == RemotingConstants.SAILFISH_MAGIC);
        receive = new ResponseProtocol();
        receive.deserialize(output, output.readInt());
        Assert.assertTrue(receive.isHeartbeat());
    }
}

