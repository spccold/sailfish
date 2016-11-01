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

import java.util.concurrent.TimeUnit;

import sailfish.remoting.exceptions.SailfishException;

/**
 * 
 * @author spccold
 * @version $Id: ResponseFuture.java, v 0.1 2016年10月4日 下午3:56:21 jileng Exp $
 */
public interface ResponseFuture<T>{
    void setResponse(T resp, int result);
    boolean isDone();
    void setCallback(ResponseCallback<T> callback, int timeout);
    T get() throws SailfishException, InterruptedException;
    T get(long timeout, TimeUnit unit) throws SailfishException,InterruptedException;
}
