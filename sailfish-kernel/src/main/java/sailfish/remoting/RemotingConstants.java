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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 
 * @author spccold
 * @version $Id: RemotingConstants.java, v 0.1 2016年10月9日 下午9:58:16 jileng Exp $
 */
public class RemotingConstants {
    //压缩阀值, KB
    public static final int     COMPRESS_THRESHOLD = 4 * 1024;
    //sailfish binary protocol magic
    public static final int     SAILFISH_MAGIC     = ByteBuffer.wrap("SASH".getBytes()).getInt();

    public static final Charset DEFAULT_CHARSET    = Charset.forName("UTF-8");

    //max frame size, 8MB
    public static final int     DEFAULT_PAYLOAD    = 8 * 1024 * 1024;
    
    public static final byte    DIRECTION_REQUEST  = 1;
    public static final byte    DIRECTION_RESPONSE = 2;
    
    //ms
    public static final int DEFAULT_CONNECT_TIMEOUT = 2000;
}
