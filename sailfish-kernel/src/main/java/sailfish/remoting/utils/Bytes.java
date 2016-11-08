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
package sailfish.remoting.utils;

/**
 * 
 * @author spccold
 * @version $Id: Bytes.java, v 0.1 2016年11月8日 下午7:44:46 jileng Exp $
 */
public class Bytes {
    public static int bytes2int(byte[] bytes) {
        if (null == bytes || bytes.length != 4) {
            throw new IllegalArgumentException();
        }
        return bytes[0] << 24 | bytes[1] << 16 | bytes[2] << 8 | bytes[3] << 0;
    }

    //high byte first
    public static byte[] int2bytes(int integer) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (integer >> 24 & 0xFF);
        bytes[1] = (byte) (integer >> 16 & 0xFF);
        bytes[2] = (byte) (integer >> 8 & 0xFF);
        bytes[3] = (byte) (integer >> 0 & 0xFF);
        return bytes;
    }
}
