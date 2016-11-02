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
 * <a href="http://www.importnew.com/14410.html">different compression algorithm in java platform</a>
 * 
 * @author spccold
 * @version $Id: RemotingConstants.java, v 0.1 2016年10月9日 下午9:58:16 jileng Exp $
 */
public class RemotingConstants {
    //压缩阀值, KB
    public static final int     COMPRESS_THRESHOLD      = 4 * 1024;
    //sailfish binary protocol magic
    public static final short   SAILFISH_MAGIC          = ByteBuffer.wrap("SH".getBytes()).getShort();
    public static final Charset DEFAULT_CHARSET         = Charset.forName("UTF-8");

    //max frame size, 8MB
    public static final int     DEFAULT_PAYLOAD         = 8 * 1024 * 1024;

    //milliseconds
    public static final int     DEFAULT_CONNECT_TIMEOUT = 2000;

    //result
    public static final byte    RESULT_SUCCESS          = 0;
    public static final byte    RESULT_FAIL             = 1;

    /** serializeType */
    //pure bytes, no need any serialize and deserialize
    public static final byte    NON_SERIALIZE           = -1;
    //java platform
    public static final byte    JDK_SERIALIZE           = 0;
    //java platform with high performance
    public static final byte    KRYO_SERIALIZE          = 1;
    public static final byte    FST_SERIALIZE           = 2;
    //cross-platform
    public static final byte    JSON_SERIALIZE          = 3;
    //cross-platform with high performance
    public static final byte    HESSIAN_SERIALIZE       = 4;
    public static final byte    AVRO_SERIALIZE          = 5;
    public static final byte    THRIFT_SERIALIZE        = 6;
    public static final byte    PROTOBUF_SERIALIZE      = 7;
    public static final byte    FLATBUFFER_SERIALIZE    = 8;

    //TODO compressType with multiple mode, needs to perfect in future
    public static final byte    NON_COMPRESS            = -1;
    public static final byte    LZ4_COMPRESS            = 1;
    public static final byte    GZIP_COMPRESS           = 2;
    public static final byte    DEFLATE_COMPRESS        = 3;
    public static final byte    SNAPPY_COMPRESS         = 4;

    //langType
    public static final byte    JAVA                    = 1;
    public static final byte    C                       = 2;
    public static final byte    CPP                     = 3;
    public static final byte    GO                      = 4;
}
