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

import sailfish.remoting.channel.ExchangeChannel;

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
    
    public void addReconnectTask(ExchangeChannel reconnectedChannel, int reconnectInterval){
        reconnectExecutor.schedule(new ReconnectTask(reconnectedChannel, reconnectInterval), 0, TimeUnit.MILLISECONDS);
    }
    
    private class ReconnectTask implements Runnable{
        private final ExchangeChannel reconnectedChannel;
        private final int reconnectInterval;
        public ReconnectTask(ExchangeChannel reconnectedChannel, int reconnectInterval) {
            this.reconnectedChannel = reconnectedChannel;
            this.reconnectInterval = reconnectInterval;
        }

        @Override
        public void run() {
            if(reconnectedChannel.isClosed()){
                return;
            }
            try{
                reconnectedChannel.update(reconnectedChannel.doConnect());
            }catch(Throwable cause){
                String msg = "reconnect to remoteAddress[%s] fail";
                logger.error(String.format(msg, reconnectedChannel.remoteAdress().toString(), cause));
                //reconnect next time
                ReconnectManager.this.reconnectExecutor.schedule(this, reconnectInterval, TimeUnit.MILLISECONDS);
            }
        }
    }
}
