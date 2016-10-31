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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sailfish.remoting.protocol.Protocol;

/**
 * 
 * @author spccold
 * @version $Id: Tracer.java, v 0.1 2016年10月26日 下午2:46:38 jileng Exp $
 */
public class Tracer {
    private static final Logger logger = LoggerFactory.getLogger(Tracer.class);
    
    private static final ConcurrentMap<Long /** packageId */, ResponseFuture<byte[]>> TRACES = new ConcurrentHashMap<>();
    
    public static void trace(long packageId, ResponseFuture<byte[]> future){
        TRACES.putIfAbsent(packageId, future);
    }
    
    public static void erase(long packageId, Protocol protocol){
        ResponseFuture<byte[]> future = TRACES.get(packageId);
        if(null == future){
            logger.info("future no exist for packageId[{}]", packageId);
            return;
        }
        future.trySuccess(protocol.body());
    }
}
