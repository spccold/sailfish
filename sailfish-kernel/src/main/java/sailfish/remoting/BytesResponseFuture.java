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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.netty.util.CharsetUtil;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;

/**
 * 
 * @author spccold
 * @version $Id: BytesResponseFuture.java, v 0.1 2016年10月4日 下午3:57:32 jileng Exp $
 */
public class BytesResponseFuture implements ResponseFuture<byte[]>{
    private static final Timer TIMER = new Timer("sailfish-callback-timeout-checker", true);
    private byte[] data;
    private long packageId;
    private volatile boolean done;
    private volatile boolean successed;
    private ResponseCallback<byte[]> callback;
    private TimerTask task;
    public BytesResponseFuture(long packageId) {
        this.packageId = packageId;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public byte[] get() throws SailfishException, InterruptedException {
        try{
            synchronized (this) {
                if(this.done && this.successed){
                    return data;
                }
                while(!this.done){
                    wait();
                }
                if(!this.successed){
                    throw new SailfishException(new String(data,CharsetUtil.UTF_8)).toRemoteException();
                }
                return data;
            }
        }catch(SailfishException | InterruptedException cause){
            throw cause;
        }finally{
            this.done = true;
        }
    }

    @Override
    public byte[] get(long timeout, TimeUnit unit) throws SailfishException, TimeoutException ,InterruptedException {
        try{
            synchronized (this) {
                if(this.done && this.successed){
                    return data;
                }
                
                long timeToSleep = unit.toMillis(timeout);
                long deadline = System.currentTimeMillis() + timeToSleep;
                while(!this.done && timeToSleep > 0){
                    wait(unit.toMillis(timeToSleep));
                    timeToSleep = deadline - System.currentTimeMillis();
                }
                if(this.done && this.successed){
                    return data;
                }
                if(!this.done){
                    String msg = String.format("wait response for packageId[%d] timeout", packageId);
                    throw new TimeoutException(msg);
                }
                if(!this.successed){
                    throw new SailfishException(new String(data, CharsetUtil.UTF_8)).toRemoteException();
                }

                return data;
            }
        }catch(SailfishException | TimeoutException | InterruptedException cause){
            throw cause;
        }finally{
            this.done = true;
        }
    }

    @Override
    public void setResponse(byte[] data, int result) {
        synchronized (this) {
            this.done = true;
            this.data = data;
            if(0 == result){
                this.successed = true;
                if(null != this.callback && null != this.task){
                    this.callback.handleResponse(data);
                    this.task.cancel();
                }
            }else{
                this.successed = false;
                if(null != this.callback && null != this.task){
                    this.callback.handleException(new SailfishException(new String(this.data, CharsetUtil.UTF_8)).toRemoteException());
                    this.task.cancel();
                }
            }
            notifyAll();
        }
    }
    
    @Override
    public void setCallback(final ResponseCallback<byte[]> callback, final int timeout) {
        this.callback = callback;
        this.task = new TimerTask() {
            @Override
            public void run() {
                synchronized (BytesResponseFuture.this) {
                    if(BytesResponseFuture.this.done){
                        return;
                    }
                    callback.handleException(new SailfishException(ExceptionCode.TIMEOUT, 
                        "wait for response timeout, packageId: "+packageId));
                }
            }
        };
        TIMER.schedule(task, timeout);
    }
}
