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

/**
 * 
 * @author spccold
 * @version $Id: DefaultProtocolHeader.java, v 0.1 2016年10月9日 下午9:53:22 jileng Exp $
 */
public class DefaultProtocolHeader implements ProtocolHeader{
    //protocol version
    private static final byte VERSION = 1;
    //是否oneway调用
    //序列化方式
    //是否发生压缩
    //压缩方式
    //language type(c++,java)
    
    @Override
    public void serialize(DataOutput output) throws IOException {
        
    }

    @Override
    public void deserialize(DataInput input) throws IOException {
        
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

}