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
package sailfish.remoting.protocol;

import io.netty.buffer.ByteBuf;
import sailfish.remoting.RemotingConstants;
import sailfish.remoting.exceptions.SailfishException;

/**
 * 
 * @author spccold
 * @version $Id: RequestProtocol.java, v 0.1 2016年10月11日 下午8:44:48 jileng Exp $
 */
public class RequestProtocol implements Protocol {
    private static final int HEADER_LENGTH    = 17;
    private byte             direction        = RemotingConstants.DIRECTION_REQUEST;
    private boolean          oneway;
    private long             packetId;
    private byte             serializeType;
    private byte             compressType;
    private byte             langType;

    private byte[]           body;

    @Override
    public void serialize(ByteBuf output) throws SailfishException {
        try {
            //write package length(not contain current length field(4 bytes))
            output.writeInt(HEADER_LENGTH + bodyLength());
            // magic for package validation
            output.writeInt(RemotingConstants.SAILFISH_MAGIC);
            output.writeByte(this.direction);
            output.writeLong(this.packetId);
            output.writeBoolean(this.oneway);
            output.writeByte(this.serializeType);
            output.writeByte(this.compressType);
            output.writeByte(this.langType);
            //wirte body data
            if (null != this.body) {
                output.writeBytes(this.body);
            }
        } catch (Throwable cause) {
            throw new SailfishException(cause);
        }
    }

    @Override
    public void deserialize(ByteBuf input, int totalLength) throws SailfishException {
        try {
            this.packetId = input.readLong();
            this.oneway = input.readBoolean();
            this.serializeType = input.readByte();
            this.compressType = input.readByte();
            this.langType = input.readByte();
            //read body
            this.body = new byte[totalLength - HEADER_LENGTH];
            input.readBytes(this.body);
        } catch (Throwable cause) {
            throw new SailfishException(cause);
        }
    }

    public long getPacketId() {
        return packetId;
    }

    public void setPacketId(long packetId) {
        this.packetId = packetId;
    }

    public boolean isOneway() {
        return oneway;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }

    public byte getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(byte serializeType) {
        this.serializeType = serializeType;
    }

    public byte getCompressType() {
        return compressType;
    }

    public void setCompressType(byte compressType) {
        this.compressType = compressType;
    }

    public byte getLangType() {
        return langType;
    }

    public void setLangType(byte langType) {
        this.langType = langType;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    private int bodyLength() {
        if (null == body) {
            return 0;
        }
        return body.length;
    }

    @Override
    public boolean request() {
        return true;
    }
}
