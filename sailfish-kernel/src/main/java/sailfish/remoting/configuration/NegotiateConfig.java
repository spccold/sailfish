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
package sailfish.remoting.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import sailfish.remoting.channel.ChannelType;
import sailfish.remoting.constants.Opcode;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.utils.ArrayUtils;

/**
 *
 * @author spccold
 * @version $Id: NegotiateConfig.java, v 0.1 2016年11月22日 下午2:58:33 spccold Exp $
 */
public class NegotiateConfig {
	private byte idleTimeout;
	private byte maxIdleTimeout;

	private final UUID uuid;
	private final byte type;
	private final short connections;
	private final short writeConnections;
	private final boolean reversed;

	private short index;

	public NegotiateConfig(byte idleTimeout, byte maxIdleTimeout, UUID uuid, byte type, short connections,
			short writeConnections, short index, boolean reversed) {
		this.idleTimeout = idleTimeout;
		this.maxIdleTimeout = maxIdleTimeout;

		this.uuid = uuid;
		this.type = type;
		this.connections = connections;
		this.writeConnections = writeConnections;
		this.reversed = reversed;

		this.index = index;
	}

	public byte idleTimeout() {
		return this.idleTimeout;
	}

	public byte maxIdleTimeout() {
		return this.maxIdleTimeout;
	}

	public UUID uuid() {
		return this.uuid;
	}

	public byte type() {
		return this.type;
	}

	public short connections() {
		return this.connections;
	}

	public short writeConnections() {
		return this.writeConnections;
	}

	public NegotiateConfig index(short index) {
		this.index = index;
		return this;
	}

	public short index() {
		return this.index;
	}

	public boolean reversed() {
		return this.reversed;
	}

	public boolean isRead() {
		return ChannelType.read.code() == type;
	}

	public boolean isWrite() {
		return ChannelType.write.code() == type;
	}

	public boolean isReadWrite(){
		return ChannelType.readwrite.code() == type;
	}

	public void reverseIndex(){
		if(!this.reversed){
			return;
		}
		if(isReadWrite()){
			this.index = (short)ArrayUtils.reverseArrayIndex(this.connections, this.index);
		}else if(isRead()){
			int readConnections = this.connections - this.writeConnections;
			this.index = (short) ArrayUtils.reverseArrayIndex(readConnections, index);
		}else if(isWrite()){
			this.index = (short) ArrayUtils.reverseArrayIndex(this.writeConnections, index);
		}
	}
	
	public NegotiateConfig deepCopy() {
		return new NegotiateConfig(idleTimeout, maxIdleTimeout, uuid, type, connections, 
				writeConnections, index, reversed);
	}

	public RequestProtocol toNegotiateRequest() throws IOException {
		int size = 1 + 1 + 16 + 1 + 2 + 2 + 2 + 1;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
				DataOutputStream dos = new DataOutputStream(baos);) {
			dos.writeByte(this.idleTimeout);
			dos.writeByte(this.maxIdleTimeout);
			dos.writeLong(this.uuid.getMostSignificantBits());
			dos.writeLong(this.uuid.getLeastSignificantBits());
			dos.writeByte(this.type);
			dos.writeShort(this.connections);
			dos.writeShort(this.writeConnections);
			dos.writeShort(this.index);
			dos.writeBoolean(this.reversed);
			return RequestProtocol.newHeartbeat().opcode(Opcode.HEARTBEAT_WITH_NEGOTIATE).body(baos.toByteArray());
		}
	}

	public static NegotiateConfig fromNegotiate(byte[] negotiateData) throws IOException {
		try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(negotiateData))) {
			return new NegotiateConfig(dis.readByte(), dis.readByte(), new UUID(dis.readLong(), dis.readLong()),
					dis.readByte(), dis.readShort(), dis.readShort(), dis.readShort(), dis.readBoolean());
		}
	}
}
