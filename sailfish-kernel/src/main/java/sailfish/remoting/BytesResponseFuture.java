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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import sailfish.common.ResponseFuture;

/**
 * 
 * @author spccold
 * @version $Id: BytesResponseFuture.java, v 0.1 2016年10月4日 下午3:57:32 jileng Exp $
 */
public class BytesResponseFuture implements ResponseFuture<byte[]>{
    private byte[] data;
    private long packageId;
    private volatile boolean successed;
    private volatile boolean done;
    private volatile boolean canceled;

    public BytesResponseFuture(long packageId) {
        this.packageId = packageId;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (this) {
            this.done = true;
            this.canceled = true;
            Tracer.erase(packageId);
            notifyAll();
            return true;
        }
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public byte[] get() throws InterruptedException, ExecutionException {
        try{
            synchronized (this) {
                if(this.done){
                    return data;
                }
                while(!successed){
                    wait();
                }
                return data;
            }
        }catch(InterruptedException e){
            throw e;
        }finally{
            this.done = true;
        }
    }

    @Override
    public byte[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try{
            synchronized (this) {
                if(this.done){
                    return data;
                }
                
                long timeToSleep = unit.toMillis(timeout);
                long deadline = System.currentTimeMillis() + timeToSleep;
                while(!successed && timeToSleep > 0){
                    wait(unit.toMillis(timeToSleep));
                    timeToSleep = deadline - System.currentTimeMillis();
                }
                if(successed){
                    return data;
                }
                String msg = String.format("wait response for packageId[%d] timeout", packageId);
                throw new TimeoutException(msg);
            }
        }catch(InterruptedException | TimeoutException e){
            throw e;
        }finally{
            this.done = true;
        }
    }

    @Override
    public void trySuccess(byte[] data) {
        synchronized (this) {
            successed = true;
            this.data = data;
            this.done = true;
            notifyAll();
        }
    }
}
