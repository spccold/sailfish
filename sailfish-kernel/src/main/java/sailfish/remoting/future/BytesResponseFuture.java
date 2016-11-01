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
package sailfish.remoting.future;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import io.netty.util.CharsetUtil;
import sailfish.remoting.RemotingConstants;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.executor.SimpleExecutor;
import sailfish.remoting.utils.ParameterChecker;

/**
 * 
 * @author spccold
 * @version $Id: BytesResponseFuture.java, v 0.1 2016年10月4日 下午3:57:32 jileng Exp $
 */
public class BytesResponseFuture implements ResponseFuture<byte[]>{
    private static final Timer TIMER = new Timer("sailfish-callback-timeout-checker", true);
    private final long packageId;
    private volatile boolean done;
    private volatile boolean successed;
    private ResponseCallback<byte[]> callback;
    private TimerTask task;
    private byte[] data;
    
    public BytesResponseFuture(long packageId) {
        this.packageId = packageId;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public byte[] get() throws SailfishException, InterruptedException {
        synchronized (this) {
            while(!this.done){
                wait();
            }
        }
        if(!this.successed){
            throw new SailfishException(new String(data, CharsetUtil.UTF_8)).toRemoteException();
        }
        return data;
    }

    @Override
    public byte[] get(long timeout, TimeUnit unit) throws SailfishException ,InterruptedException {
        ParameterChecker.checkPositive(timeout, "timeout");
        synchronized (this) {
            long timeToSleep = unit.toMillis(timeout);
            long deadline = System.currentTimeMillis() + timeToSleep;
            while(!this.done && timeToSleep > 0){
                wait(timeToSleep);
                timeToSleep = deadline - System.currentTimeMillis();
            }
        }
        if(!this.done){
            String msg = String.format("wait response for packageId[%d] timeout", packageId);
            throw new SailfishException(ExceptionCode.TIMEOUT, msg);
        }
        if(!this.successed){
            throw new SailfishException(new String(data, CharsetUtil.UTF_8)).toRemoteException();
        }
        return data;
    }

    @Override
    public void putResponse(byte[] data, byte result) {
        synchronized (this) {
            this.done = true;
            this.data = data;
            switch(result){
                case RemotingConstants.RESULT_SUCCESS:
                    this.successed = true;
                    break;
                case RemotingConstants.RESULT_FAIL:
                    this.successed = false;
                    break;
            }
            notifyAll();
        }
        
        if(null != this.task){
            this.task.cancel();
        }
        
        if(null == this.callback){
            return;
        }
        executeCallbackTask();
    }
    
    @Override
    public void setCallback(final ResponseCallback<byte[]> callback, final int timeout) {
        this.callback = callback;
        this.task = new CallbackCheckTask();
        TIMER.schedule(task, timeout);
    }
    
    private class CallbackTask implements Runnable{
        @Override
        public void run() {
            if(!BytesResponseFuture.this.done){
                String msg = String.format("wait response for packageId[%d] timeout", BytesResponseFuture.this.packageId);
                BytesResponseFuture.this.callback.handleException(new SailfishException(ExceptionCode.TIMEOUT, msg));
                return;
            }
            if(BytesResponseFuture.this.successed){
                BytesResponseFuture.this.callback.handleResponse(BytesResponseFuture.this.data);
                return;
            }
            BytesResponseFuture.this.callback.handleException(new SailfishException(
                new String(BytesResponseFuture.this.data, CharsetUtil.UTF_8)).toRemoteException());
        }
    }
    
    private class CallbackCheckTask extends TimerTask{
        @Override
        public void run() {
            synchronized (BytesResponseFuture.this) {
                if(BytesResponseFuture.this.done){
                    return;
                }
            }
            executeCallbackTask();
        }
    }
    
    private void executeCallbackTask(){
        Executor executor = null != this.callback.getExecutor() ? 
            this.callback.getExecutor() : SimpleExecutor.instance();
        executor.execute(new CallbackTask());
    }
}
