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
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.utils.StrUtils;

/**
 * sailfish binary response protocol
 * <pre>
 * 1-- magic(2 bytes)
 * 2-- total length(header length + body length, 4 bytes)
 * 3-- header (6 bytes)
 *    3.1-- direction + heartbeat response or normal response + serializeType (1 byte)
 *          --response(0)(eighth high-order bit)
 *          --heartbeat response(1)/normal response(0)(sixth high-order bit)
 *          --serializeType([0~31])(five low-order bits)
 *    3.2-- packetId (4 bytes)
 *    3.3-- result + compressType  (1 byte)
 *          --result(four high-order bits)
 *          --compressType(four low-order bits)
 * 4-- body ((total length - header length) bytes)
 * </pre>
 * 
 * @author spccold
 * @version $Id: ResponseProtocol.java, v 0.1 2016年10月11日 下午8:44:48 jileng Exp $
 */
public class ResponseProtocol implements Protocol{
    private static final int HEADER_LENGTH = 6;
    private static final int RESPONSE_FLAG = 0;
    private static final int HEARTBEAT_FLAG = 0x20;
        
    //response direction
    private boolean heartbeat;
    private byte serializeType;
    
    private int packetId;
    
    private byte result;
    private byte compressType;

    private byte[] body;
    
    @Override
    public void serialize(ByteBuf output) throws SailfishException {
        try{
            //write magic first
            output.writeShort(RemotingConstants.SAILFISH_MAGIC);
            //write package length(not contain current length field(4 bytes))
            if(this.heartbeat){
                output.writeInt(1);
            }else{
                output.writeInt(HEADER_LENGTH + bodyLength());
            }

            byte compactByte = (byte)RESPONSE_FLAG; 
            if(heartbeat){
                compactByte = (byte)(compactByte | HEARTBEAT_FLAG);
                output.writeByte(compactByte);
                return;
            }
            output.writeByte(compactByte | serializeType);
            
            output.writeInt(packetId);
            output.writeByte(result << 4 | compressType);
            
            if(bodyLength() != 0){
                output.writeBytes(body);
            }
        }catch(Throwable cause){
            throw new SailfishException(cause);
        }
    }

    @Override
    public void deserialize(ByteBuf input, int totalLength) throws SailfishException {
        try{
            byte compactByte = input.readByte();
            this.heartbeat = ((compactByte & HEARTBEAT_FLAG) != 0);
            if(this.heartbeat){
                return;
            }
            this.serializeType = (byte)(compactByte & 0x1F);
            
            this.packetId = input.readInt(); 
            
            byte tmp = input.readByte();

            this.result = (byte)(tmp >> 4 & 0xF);
            this.compressType = (byte)(tmp >> 0 & 0xF);
            
            //read body
            int bodyLength = totalLength - HEADER_LENGTH;
            if(bodyLength > 0){
                this.body = new byte[bodyLength];
                input.readBytes(this.body);
            }
        }catch(Throwable cause){
            throw new SailfishException(cause);
        }
    }

    public void heartbeat(boolean heartbeat) {
        this.heartbeat = heartbeat;
    }

    public byte serializeType() {
        return serializeType;
    }

    public void serializeType(byte serializeType) {
        this.serializeType = ProtocolParameterChecker.checkSerializeType(serializeType);
    }

    public int packetId() {
        return packetId;
    }

    public void packetId(int packetId) {
        this.packetId = packetId;
    }

    public byte result() {
        return result;
    }

    public void result(byte result) {
        this.result = ProtocolParameterChecker.checkResult(result);
    }

    public byte compressType() {
        return compressType;
    }

    public void compressType(byte compressType) {
        this.compressType = ProtocolParameterChecker.checkCompressType(compressType);
    }

    public byte[] body() {
        return body;
    }

    public void body(byte[] body) {
        this.body = body;
    }

    public void errorStack(String errorStack) {
        if(StrUtils.isNotBlank(errorStack)){
            this.body = errorStack.getBytes(RemotingConstants.DEFAULT_CHARSET);
        }
    }
   
    private int bodyLength(){
        if(null == body){
            return 0;
        }
        return body.length;
    }

    @Override
    public boolean request() {
        return false;
    }
    
    public boolean heartbeat() {
        return this.heartbeat;
    }
    
    public static ResponseProtocol newHeartbeat(){
        ResponseProtocol heartbeat = new ResponseProtocol();
        heartbeat.heartbeat(true);
        return heartbeat;
    }
}
