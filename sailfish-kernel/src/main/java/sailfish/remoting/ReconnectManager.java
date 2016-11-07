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

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sailfish.remoting.channel.SimpleExchangeChannel;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.exceptions.SailfishException;

/**
 * 
 * @author spccold
 * @version $Id: ReconnectManager.java, v 0.1 2016年11月7日 下午4:26:22 jileng Exp $
 */
public class ReconnectManager {
    private static final Logger logger = LoggerFactory.getLogger(ReconnectManager.class);
    public static final ReconnectManager INSTANCE = new ReconnectManager();
    private final ScheduledThreadPoolExecutor reconnectExecutor;
    private ReconnectManager(){
        reconnectExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread();
                t.setName("sailfish-ReconnectManager");
                t.setDaemon(true);
                return t;
            }
        });
        reconnectExecutor.setKeepAliveTime(1, TimeUnit.MINUTES);
    }
    
    public void addReconnectTask(SimpleExchangeChannel reconnectedChannel){
        reconnectExecutor.schedule(new ReconnectTask(reconnectedChannel), 0, TimeUnit.MILLISECONDS);
    }
    
    private class ReconnectTask implements Runnable{
        private final SimpleExchangeChannel reconnectedChannel;
        private final ExchangeClientConfig clientConfig;
        public ReconnectTask(SimpleExchangeChannel reconnectedChannel) {
            this.reconnectedChannel = reconnectedChannel;
            this.clientConfig = reconnectedChannel.getConfig();
        }

        @Override
        public void run() {
            try{
                reconnectedChannel.reset(reconnectedChannel.doConnect(this.clientConfig));
            }catch(Throwable cause){
                String msg = "reconnect to remoteAddress[%s] fail";
                logger.error(String.format(msg, clientConfig.address().toString()), cause);
                if(cause instanceof SailfishException){
                    reconnectExecutor.schedule(this, this.clientConfig.reconnectInterval(), TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
