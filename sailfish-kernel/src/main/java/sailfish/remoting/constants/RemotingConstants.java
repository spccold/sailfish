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
package sailfish.remoting.constants;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * <a href="http://www.importnew.com/14410.html">different compression algorithm in java platform</a>
 * 
 * @author spccold
 * @version $Id: RemotingConstants.java, v 0.1 2016年10月9日 下午9:58:16 jileng Exp $
 */
public interface RemotingConstants {
    //压缩阀值, KB
    int     COMPRESS_THRESHOLD      = 4 * 1024;
    //sailfish binary protocol magic
    short   SAILFISH_MAGIC          = ByteBuffer.wrap("SH".getBytes()).getShort();
    Charset DEFAULT_CHARSET         = Charset.forName("UTF-8");

    //max frame size, 8MB
    int     DEFAULT_PAYLOAD         = 8 * 1024 * 1024;

    //milliseconds
    int     DEFAULT_CONNECT_TIMEOUT = 2000;

    //result
    byte    RESULT_SUCCESS          = 0;
    byte    RESULT_FAIL             = 1;
}
