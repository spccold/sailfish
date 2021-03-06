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
 * @version $Id: ArrayUtils.java, v 0.1 2016年11月10日 下午5:34:03 jileng Exp $
 */

public class ArrayUtils {

    /**
     * <p>
     * Checks if an array of Objects is empty or {@code null}.
     * </p>
     *
     * @param array
     *            the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 2.1
     */
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <p>
     * Checks if an array of Objects is not empty or not {@code null}.
     * </p>
     *
     * @param <T>
     *            the component type of the array
     * @param array
     *            the array to test
     * @return {@code true} if the array is not empty or not {@code null}
     * @since 2.5
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }
    
    /**
     * <p>
     * Checks if an array of primitive bytes is empty or {@code null}.
     * </p>
     *
     * @param array
     *            the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 2.1
     */
    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }
    
    /**
     * reverse element array index
     * 
     * @param length  array length
     * @param index   the element index
     * @return
     */
    public static int reverseArrayIndex(int length, int index){
    	return length - index - 1;
    }
}