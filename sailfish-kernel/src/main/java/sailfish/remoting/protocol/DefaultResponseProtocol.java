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

import sailfish.common.Constants;
import sailfish.exceptions.ProtocolCodecException;
import sailfish.remoting.RemotingConstants;
import sailfish.utils.StrUtils;

/**
 * 
 * @author spccold
 * @version $Id: DefaultResponseProtocol.java, v 0.1 2016年10月11日 下午9:48:43 jileng Exp $
 */
public class DefaultResponseProtocol implements Protocol{
    private static final int HEADER_LENGTH = 17;
    private static final int PROTOCOL_VERSION = 1;
    // 4 bytes
    private int magic = RemotingConstants.SAILFISH_MAGIC;
    // 8 bytes
    private long packageId;
    // 1 byte
    private byte result;
    // 4 bytes
    private int errorStackLength = 0;
    
    private String errorStack;
    private byte[] errorStackBytes;
    private byte[] body;
    
    @Override
    public void serialize(DataOutput output) throws ProtocolCodecException {
        try{
            //write total length
            output.writeInt(HEADER_LENGTH + errorStackLength + bodyLength());
            output.writeInt(magic);
            output.writeLong(packageId);
            output.writeByte(result);
            output.writeInt(errorStackLength);
            if(StrUtils.isNotBlank(errorStack)){
                output.write(errorStackBytes);
            }
            output.write(body);
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
            this.result = input.readByte();
            this.errorStackLength = input.readInt();
            if(this.errorStackLength > 0){
                errorStackBytes = new byte[errorStackLength];
                input.readFully(errorStackBytes);
                this.errorStack = new String(errorStackBytes, RemotingConstants.DEFAULT_CHARSET);
            }
            body = new byte[totalLength - HEADER_LENGTH - errorStackLength];
            input.readFully(body);
        }catch(IOException cause){
            throw new ProtocolCodecException(Constants.IO_EXCEPTION, cause);
        }
    }
    
    public long getPackageId() {
        return packageId;
    }

    public void setPackageId(long packageId) {
        this.packageId = packageId;
    }

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public String getErrorStack() {
        return errorStack;
    }

    public void setErrorStack(String errorStack) {
        this.errorStack = errorStack;
        if(StrUtils.isNotBlank(errorStack)){
            errorStackBytes = errorStack.getBytes(RemotingConstants.DEFAULT_CHARSET);
            this.errorStackLength = errorStackBytes.length;
        }
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
    
    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public int getErrorStackLength() {
        return errorStackLength;
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
}
