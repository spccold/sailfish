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
package sailfish.remoting.handler;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;

/**
 * XXX
 * @author spccold
 * @version $Id: ChannelHandlerContexts.java, v 0.1 2016年11月9日 下午5:17:33 jileng Exp $
 */
public class ChannelHandlerContexts implements ChannelHandlerContext {
    private int                     writeConns        = 0;
    private int                     readConns         = 0;
    private ChannelHandlerContext[] writeContexts;
    private ChannelHandlerContext[] readContexts;
    private ChannelHandlerContext[] deadWriteContexts;
    private ChannelHandlerContext[] deadReadContexts;
    private final AtomicInteger     writeContextIndex = new AtomicInteger(0);
    private final AtomicInteger     readContextIndex  = new AtomicInteger(0);

    public void addWriteChannelHandlerContext(ChannelHandlerContext writeContext, int index) {
        synchronized (this) {
            this.writeConns++;
            if (null == this.writeContexts) {
                this.writeContexts = new ChannelHandlerContext[index + 1];
            } else if (this.writeContexts.length < index + 1) {
                ChannelHandlerContext[] newWriteContexts = new ChannelHandlerContext[index + 1];
                System.arraycopy(this.writeContexts, 0, newWriteContexts, 0, this.writeContexts.length);
                this.writeContexts = newWriteContexts;
            }
            this.writeContexts[index] = writeContext;
            this.deadWriteContexts = new ChannelHandlerContext[this.writeConns];
        }
    }

    public void addReadChannelHandlerContext(ChannelHandlerContext readContext, int index) {
        synchronized (this) {
            this.readConns++;
            if (null == this.readContexts) {
                this.readContexts = new ChannelHandlerContext[index + 1];
            } else if (this.readContexts.length < index + 1) {
                ChannelHandlerContext[] newReadContexts = new ChannelHandlerContext[index + 1];
                System.arraycopy(this.readContexts, 0, newReadContexts, 0, this.readContexts.length);
                this.readContexts = newReadContexts;
            }
            this.readContexts[index] = readContext;
            this.deadReadContexts = new ChannelHandlerContext[this.readConns];
        }
    }

    //lock free
    private ChannelHandlerContext next() {
        int arrayIndex = 0;
        //select write context first 
        int currentIndex = writeContextIndex.getAndIncrement();
        for (int i = 0; i < this.writeConns; i++) {
            ChannelHandlerContext currentContext = writeContexts[arrayIndex = Math
                .abs((currentIndex++) % this.writeConns)];
            if (available(currentContext)) {
                if (null != deadWriteContexts[arrayIndex]) {
                    deadWriteContexts[arrayIndex] = null;
                }
                return currentContext;
            }
            deadWriteContexts[arrayIndex] = currentContext;
        }

        //if all write context unavailable, try to select read context
        arrayIndex = 0;
        currentIndex = readContextIndex.getAndIncrement();
        for (int i = 0; i < this.readConns; i++) {
            ChannelHandlerContext currentContext = readContexts[arrayIndex = Math
                .abs((currentIndex++) % this.readConns)];
            if (available(currentContext)) {
                if (null != deadReadContexts[arrayIndex]) {
                    deadReadContexts[arrayIndex] = null;
                }
                return currentContext;
            }
            deadReadContexts[arrayIndex] = currentContext;
        }
        //TODO
        throw new RuntimeException(
            new SailfishException(ExceptionCode.EXCHANGER_NOT_AVAILABLE, "exchanger is not available!"));
    }

    private boolean available(ChannelHandlerContext context) {
        if (null == context) {
            return false;
        }
        Channel channel = context.channel();
        return (null != channel && channel.isOpen() && channel.isActive());
    }

    /********************************implementation from ChannelHandlerContext****************************************/
    @SuppressWarnings("deprecation")
    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return next().attr(key);
    }

    @Override
    public Channel channel() {
        return next().channel();
    }

    @Override
    public EventExecutor executor() {
        return next().executor();
    }

    @Override
    public String name() {
        return next().name();
    }

    @Override
    public ChannelHandler handler() {
        return next().handler();
    }

    @Override
    public boolean isRemoved() {
        return next().isRemoved();
    }

    @Override
    public ChannelHandlerContext fireChannelRegistered() {
        return next().fireChannelRegistered();
    }

    @Override
    public ChannelHandlerContext fireChannelUnregistered() {
        return next().fireChannelUnregistered();
    }

    @Override
    public ChannelHandlerContext fireChannelActive() {
        return next().fireChannelActive();
    }

    @Override
    public ChannelHandlerContext fireChannelInactive() {
        return next().fireChannelInactive();
    }

    @Override
    public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
        return next().fireExceptionCaught(cause);
    }

    @Override
    public ChannelHandlerContext fireUserEventTriggered(Object event) {
        return next().fireUserEventTriggered(event);
    }

    @Override
    public ChannelHandlerContext fireChannelRead(Object msg) {
        return next().fireChannelRead(msg);
    }

    @Override
    public ChannelHandlerContext fireChannelReadComplete() {
        return next().fireChannelReadComplete();
    }

    @Override
    public ChannelHandlerContext fireChannelWritabilityChanged() {
        return next().fireChannelWritabilityChanged();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return next().bind(localAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return next().connect(remoteAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return next().connect(remoteAddress, localAddress);
    }

    @Override
    public ChannelFuture disconnect() {
        return next().disconnect();
    }

    @Override
    public ChannelFuture close() {
        return next().close();
    }

    @Override
    public ChannelFuture deregister() {
        return next().deregister();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return next().bind(localAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return next().connect(remoteAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return next().connect(remoteAddress, localAddress, promise);
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        return next().disconnect(promise);
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        return next().close(promise);
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise) {
        return next().deregister(promise);
    }

    @Override
    public ChannelHandlerContext read() {
        return next().read();
    }

    @Override
    public ChannelFuture write(Object msg) {
        return next().write(msg);
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return next().write(msg, promise);
    }

    @Override
    public ChannelHandlerContext flush() {
        return next().flush();
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return next().writeAndFlush(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return next().writeAndFlush(msg);
    }

    @Override
    public ChannelPipeline pipeline() {
        return next().pipeline();
    }

    @Override
    public ByteBufAllocator alloc() {
        return next().alloc();
    }

    @Override
    public ChannelPromise newPromise() {
        return next().newPromise();
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return next().newProgressivePromise();
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return next().newSucceededFuture();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        return next().newFailedFuture(cause);
    }

    @Override
    public ChannelPromise voidPromise() {
        return next().voidPromise();
    }

    @SuppressWarnings("deprecation")
    @Override
    public <T> boolean hasAttr(AttributeKey<T> arg0) {
        return next().hasAttr(arg0);
    }
}
