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

import org.junit.Assert;
import org.junit.Test;

import sailfish.remoting.utils.Bytes;

/**
 * 
 * @author spccold
 * @version $Id: BytesTest.java, v 0.1 2016年11月8日 下午7:53:57 jileng Exp $
 */
public class BytesTest {
    
    @Test
    public void test() {
        //test positive
        int integer = 1;
        Assert.assertTrue(integer == Bytes.bytes2int(Bytes.int2bytes(integer)));
        
        //test negative
        integer = -1;
        Assert.assertTrue(integer == Bytes.bytes2int(Bytes.int2bytes(integer)));
        
        //test zero
        integer = 0;
        Assert.assertTrue(integer == Bytes.bytes2int(Bytes.int2bytes(integer)));
    }
}
