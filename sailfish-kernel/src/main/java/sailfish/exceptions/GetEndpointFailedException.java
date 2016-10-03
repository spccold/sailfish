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
package sailfish.exceptions;

/**
 * 
 * @author spccold
 * @version $Id: GetEndpointFailedException.java, v 0.1 2016年10月3日 下午3:57:57 jileng Exp $
 */
public class GetEndpointFailedException extends Exception{

    /**  */
    private static final long serialVersionUID = 1L;

    public GetEndpointFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public GetEndpointFailedException(String message) {
        super(message);
    }

    public GetEndpointFailedException(Throwable cause) {
        super(cause);
    }
}
