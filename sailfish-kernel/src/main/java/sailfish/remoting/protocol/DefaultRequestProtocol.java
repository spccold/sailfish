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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import sailfish.common.Constants;
import sailfish.exceptions.ProtocolCodecException;
import sailfish.remoting.RemotingConstants;

/**
 * 
 * @author spccold
 * @version $Id: DefaultRequestProtocol.java, v 0.1 2016年10月11日 下午8:44:48 jileng Exp $
 */
public class DefaultRequestProtocol implements Protocol{
    private static final int HEADER_LENGTH = 17;
    private static final int PROTOCOL_VERSION = 1;
    //magic number(4 bytes)
    private int magic = RemotingConstants.SAILFISH_MAGIC;
    //packageId (8 bytes)
    private long packageId;
    //是否oneway调用 (1 byte)
    private boolean oneway;
    //序列化方式 (1 byte)
    private byte serializeType;
    //是否发生压缩 (1 byte)
    private boolean compressed;
    //压缩方式 (1 byte)
    private byte compressType;
    //language type(c++,java) (1 byte)
    private byte langType;
    
    //body
    private byte[] body;
    
    @Override
    public void serialize(DataOutput output) throws ProtocolCodecException {
        try{
            //write package length(not contain current length field(4 bytes))
            output.writeInt(HEADER_LENGTH + bodyLength());
            // magic for package validation
            output.writeInt(this.magic);
            output.writeLong(this.packageId);
            output.writeBoolean(this.oneway);
            output.writeByte(this.serializeType);
            output.writeBoolean(this.compressed);
            output.writeByte(this.compressType);
            output.writeByte(this.langType);
            //wirte body data
            if(null != this.body){
                output.write(this.body);
            }
        }catch(IOException cause){
            throw new ProtocolCodecException(Constants.IO_EXCEPTION, cause);
        }
    }

    @Override
    public void deserialize(DataInput input) throws ProtocolCodecException {
        try{
            int totalLength = input.readInt();
            int magic = input.readInt();
            if(magic != this.magic){
                throw new ProtocolCodecException(Constants.BAD_PACKAGE, 
                    "bad package, expected magic:"+this.magic+"but actual:"+magic+", current channel will be closed!");
            }
            this.packageId = input.readLong();
            this.oneway = input.readBoolean();
            this.serializeType = input.readByte();
            this.compressed = input.readBoolean();
            this.compressType = input.readByte();
            this.langType = input.readByte();
            //read body
            this.body = new byte[totalLength - HEADER_LENGTH];
            input.readFully(this.body);
        }catch(IOException cause){
            throw new ProtocolCodecException(Constants.IO_EXCEPTION, cause);
        }
    }
    
    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public long getPackageId() {
        return packageId;
    }

    public void setPackageId(long packageId) {
        this.packageId = packageId;
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

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
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

    private int bodyLength(){
        if(null == body){
            return 0;
        }
        return body.length;
    }
    
    @Override
    public byte getVersion() {
        return PROTOCOL_VERSION;
    }

    @Override
    public String toString() {
        return "DefaultRequestProtocol [magic=" + magic + ", packageId=" + packageId + ", oneway=" + oneway
               + ", serializeType=" + serializeType + ", compressed=" + compressed + ", compressType=" + compressType
               + ", langType=" + langType + ", body=" + Arrays.toString(body) + "]";
    }
}
