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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sailfish.remoting.channel.SimpleExchangeChannel;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.protocol.ResponseProtocol;
import sailfish.remoting.utils.CollectionUtils;
import sailfish.remoting.utils.ParameterChecker;

/**
 * tcp communication tracer
 * 
 * @author spccold
 * @version $Id: Tracer.java, v 0.1 2016年10月26日 下午2:46:38 jileng Exp $
 */
public class Tracer {
    private static final Logger logger = LoggerFactory.getLogger(Tracer.class);
    private static final Object EMPTY_VALUE = new Object();
    private static final ConcurrentMap<Integer , TraceContext> TRACES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<SimpleExchangeChannel, ConcurrentMap<Integer, Object>> 
                                            SINGLE_CHANNEL_TRACES = new ConcurrentHashMap<>();
    
    public static Map<Integer, Object> popPendingRequests(SimpleExchangeChannel channel){
        return SINGLE_CHANNEL_TRACES.remove(channel);
    }

    public static Map<Integer, Object> peekPendingRequests(SimpleExchangeChannel channel){
        return SINGLE_CHANNEL_TRACES.get(channel);
    }
    
    public static void trace(SimpleExchangeChannel channel ,int packageId, ResponseFuture<byte[]> future){
        TRACES.putIfAbsent(packageId, new TraceContext(channel, future));
        
        ConcurrentMap<Integer, Object> packetIds = SINGLE_CHANNEL_TRACES.get(channel);
        if(null == packetIds){
            ConcurrentMap<Integer, Object> old = SINGLE_CHANNEL_TRACES.putIfAbsent(channel,
                packetIds = new ConcurrentHashMap<>());
            if(null != old){
                packetIds = old;
            }
        }
        packetIds.put(packageId, EMPTY_VALUE);
    }
    
    public static void erase(ResponseProtocol protocol){
        if(protocol.heartbeat()){
            return;
        }
        TraceContext traceContext = TRACES.remove(protocol.packetId());
        if(null == traceContext){
            logger.info("trace no exist for packageId[{}]", protocol.packetId());
            return;
        }
        traceContext.respFuture.putResponse(protocol.body(), protocol.result());
        ConcurrentMap<Integer, Object> packetIds = SINGLE_CHANNEL_TRACES.get(traceContext.channel);
        if(CollectionUtils.isNotEmpty(packetIds)){
            packetIds.remove(protocol.packetId());
        }
    }
    
    static class TraceContext{
        SimpleExchangeChannel channel;
        ResponseFuture<byte[]> respFuture;
        public TraceContext(SimpleExchangeChannel channel, ResponseFuture<byte[]> respFuture) {
            this.channel = ParameterChecker.checkNotNull(channel, "channel");
            this.respFuture = ParameterChecker.checkNotNull(respFuture, "respFuture");
        }
    }
}
