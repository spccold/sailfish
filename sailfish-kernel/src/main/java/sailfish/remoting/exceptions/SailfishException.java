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
package sailfish.remoting.exceptions;

import sailfish.remoting.utils.StrUtils;

/**
 * 
 * @author spccold
 * @version $Id: RemotingException.java, v 0.1 2016年10月27日 下午3:44:04 jileng Exp $
 */
public class SailfishException extends Exception {

    /**  */
    private static final long serialVersionUID = 1L;
    private ExceptionCode     errorCode;

    public SailfishException(String message) {
        super(message);
    }
    
    public SailfishException(Throwable cause){
        super(cause);
    }

    public SailfishException(ExceptionCode errorCode, String message) {
        super(message(errorCode, message));
        this.errorCode = errorCode;
    }

    public SailfishException(String message, Throwable cause) {
        super(message, cause);
    }

    public SailfishException(ExceptionCode errorCode, String message, Throwable cause) {
        super(message(errorCode, message), cause);
        this.errorCode = errorCode;
    }

    private static String message(ExceptionCode errorCode, String message) {
        if(null == errorCode){
            errorCode = ExceptionCode.DEFAULT;
        }
        String prefix = "[errorCode:" + errorCode.toString() + "]";
        return StrUtils.isBlank(message) ? prefix : prefix + ", "+ message;
    }

    public ExceptionCode code() {
        return errorCode;
    }

    public RemoteSailfishException toRemoteException() {
        return new RemoteSailfishException(errorCode, getMessage(), getCause());
    }
    
    /**
     * exception for remote peer
     */
    class RemoteSailfishException extends SailfishException{
        private static final long serialVersionUID = 1L;
        public RemoteSailfishException(ExceptionCode errorCode, String message, Throwable cause) {
            super(errorCode, message, cause);
        }
    }
}
