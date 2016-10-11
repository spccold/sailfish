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
 * @version $Id: ProtocolCodecException.java, v 0.1 2016年10月11日 下午10:31:02 jileng Exp $
 */
public class ProtocolCodecException extends BaseException{

    /**  */
    private static final long serialVersionUID = 1L;

    public ProtocolCodecException(int errorCode) {
        super(errorCode);
    }

    public ProtocolCodecException(int errorCode, String message) {
        super(errorCode, message);
    }

    public ProtocolCodecException(int errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ProtocolCodecException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
