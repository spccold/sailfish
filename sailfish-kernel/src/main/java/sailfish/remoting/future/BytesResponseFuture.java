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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import io.netty.util.CharsetUtil;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.Tracer;
import sailfish.remoting.constants.RemotingConstants;
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
    private final int packetId;
    private volatile boolean done;
    private volatile boolean successed;
    private ResponseCallback<byte[]> callback;
    private TimerTask task;
    private byte[] data;
    
    private volatile SailfishException cause;
    private final Tracer tracer;
    public BytesResponseFuture(int packetId, Tracer tracer) {
        this.packetId = packetId;
        this.tracer = tracer;
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
        	if(null == cause){
        		this.cause = new SailfishException(new String(data, CharsetUtil.UTF_8)).toRemoteException();
        	}
            throw cause;
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
        	this.done = true;
        	removeTrace();
        	String msg = String.format("wait response for packetId[%d] timeout", packetId);
            this.cause = new SailfishException(ExceptionCode.RESPONSE_TIMEOUT, msg);
            throw cause;
        }
        if(!this.successed){
        	if(null == cause){
        		this.cause = new SailfishException(new String(data, CharsetUtil.UTF_8)).toRemoteException();
        	}
            throw cause;
        }
        return data;
    }

    @Override
    public void putResponse(byte[] data, byte result, SailfishException cause) {
        synchronized (this) {
            this.done = true;
            this.cause = cause;
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
        if(null == callback || this.done){
            return;
        }
        this.callback = callback;
        this.task = new CallbackCheckTask();
        TIMER.schedule(task, timeout);
    }
    
    private class CallbackTask implements Runnable{
        @Override
        public void run() {
            if(!BytesResponseFuture.this.done){
                BytesResponseFuture.this.done = true;
                removeTrace();
                String msg = String.format("wait response for packetId[%d] timeout", BytesResponseFuture.this.packetId);
                BytesResponseFuture.this.cause = new SailfishException(ExceptionCode.RESPONSE_TIMEOUT, msg);
                BytesResponseFuture.this.callback.handleException(BytesResponseFuture.this.cause);
                return;
            }
            if(BytesResponseFuture.this.successed){
                BytesResponseFuture.this.callback.handleResponse(BytesResponseFuture.this.data);
                return;
            }
            if(null == BytesResponseFuture.this.cause){
            	BytesResponseFuture.this.cause = new SailfishException(
                        new String(BytesResponseFuture.this.data, CharsetUtil.UTF_8)).toRemoteException();
            }
            BytesResponseFuture.this.callback.handleException(BytesResponseFuture.this.cause);
        }
    }
    
    private class CallbackCheckTask extends TimerTask{
        @Override
        public void run() {
        	if(BytesResponseFuture.this.done){
        		return;
        	}
            executeCallbackTask();
        }
    }
    
    private void executeCallbackTask(){
        Executor executor = null != this.callback.getExecutor() ? 
            this.callback.getExecutor() : SimpleExecutor.INSTANCE;
        try{
        	executor.execute(new CallbackTask());
        }catch(RejectedExecutionException cause){
        	SimpleExecutor.INSTANCE.execute(new CallbackTask());
        }
    }
    
    private void removeTrace(){
    	this.tracer.remove(packetId);
    }
}
