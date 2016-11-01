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

import java.util.concurrent.Executor;

/**
 * 
 * @author spccold
 * @version $Id: ResponseCallback.java, v 0.1 2016年10月31日 上午10:09:15 jileng Exp $
 */
public interface ResponseCallback<T> {
    Executor getExecutor();
    
    void handleResponse(T resp);
    void handleException(Exception cause);
}   
