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

/**
 * 
 * @author spccold
 * @version $Id: ProtocolParameterChecker.java, v 0.1 2016年11月2日 下午7:29:18 jileng Exp $
 */
public class ProtocolParameterChecker {
    public static byte checkSerializeType(byte serializeType){
        if(serializeType < 0 || serializeType > 0x1F){
            throw new IllegalArgumentException(
                "serializeType: " + serializeType + " (expected: 0 <= serializeType <= 0x1F)");
        }
        return serializeType;
    }
    
    public static byte checkCompressType(byte compressType){
        if(compressType < 0 || compressType > 0xF){
            throw new IllegalArgumentException(
                "compressType: " + compressType + " (expected: 0 <= compressType <= 0xF)");
        }
        return compressType;
    }
    
    public static byte checkLangType(byte langType){
        if(langType < 0 || langType > 0xF){
            throw new IllegalArgumentException(
                "langType: " + langType + " (expected: 0 <= langType <= 0xF)");
        }
        return langType;
    }

    public static byte checkResult(byte result){
        if(result < 0 || result > 0xF){
            throw new IllegalArgumentException(
                "result: " + result + " (expected: 0 <= result <= 0xF)");
        }
        return result;
    }
}
