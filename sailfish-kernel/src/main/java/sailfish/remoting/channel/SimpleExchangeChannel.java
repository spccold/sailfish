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
package sailfish.remoting.channel;

import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import sailfish.remoting.NettyPlatformIndependent;
import sailfish.remoting.ReconnectManager;
import sailfish.remoting.RequestControl;
import sailfish.remoting.ResponseCallback;
import sailfish.remoting.Tracer;
import sailfish.remoting.codec.RemotingDecoder;
import sailfish.remoting.codec.RemotingEncoder;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.constants.RemotingConstants;
import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.BytesResponseFuture;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.handler.ChannelEventsHandler;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.handler.ShareableSimpleChannelInboundHandler;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;
import sailfish.remoting.utils.CollectionUtils;
import sailfish.remoting.utils.ParameterChecker;
import sailfish.remoting.utils.RemotingUtils;
import sailfish.remoting.utils.StrUtils;

/**
 * with only one connection and the connection need to be initialized immediately or lazily
 * 
 * @author spccold
 * @version $Id: SimpleExchangeChannel.java, v 0.1 2016年10月26日 下午9:08:24 jileng Exp $
 */
public class SimpleExchangeChannel extends AbstractExchangeChannel implements ExchangeChannel {
    private final ExchangeClientConfig clientConfig;
    private volatile Channel           nettyChannel;
    private volatile boolean           lazyWithOutInit;
    private volatile boolean           reconnectting;
    private volatile boolean           closed = false;

    public SimpleExchangeChannel(ExchangeClientConfig config) throws SailfishException {
        this.clientConfig = config.deepCopy();
        this.reconnectting = false;
        if ((!ChannelMode.readwrite.equals(clientConfig.mode()) && clientConfig.isLazyConnection())
            || (ChannelMode.readwrite.equals(clientConfig.mode()) && clientConfig.isWriteConnection()
                && clientConfig.isLazyConnection())) {
            this.lazyWithOutInit = true;
        } else {
            this.nettyChannel = doConnect(clientConfig);
            this.lazyWithOutInit = false;
        }
    }

    public ExchangeClientConfig getConfig() {
        return this.clientConfig;
    }

    public void reset(Channel newChannel) {
        synchronized (this) {
            this.reconnectting = false;
            if (this.isClosed()) {
                RemotingUtils.closeChannel(newChannel);
                return;
            }
            this.nettyChannel = newChannel;
        }
    }

    @Override
    public void oneway(byte[] data, RequestControl requestControl) throws SailfishException {
        channelStatusCheck();
        initChannel();
        RequestProtocol protocol = newRequest(requestControl);
        protocol.oneway(true);
        protocol.body(data);
        //TODO write or writeAndFlush?
        ChannelFuture future = nettyChannel.writeAndFlush(protocol);
        try {
            if (requestControl.sent()) {
                boolean ret = future.await(requestControl.timeout());
                if (!ret) {
                    future.cancel(true);
                    throw new SailfishException(ExceptionCode.TIMEOUT, "oneway request timeout");
                }
            }
        } catch (InterruptedException cause) {
            throw new SailfishException(ExceptionCode.INTERRUPTED, "interrupted exceptions");
        }
    }

    @Override
    public ResponseFuture<byte[]> request(byte[] data, RequestControl requestControl) throws SailfishException {
        return requestWithFuture(data, null, requestControl);
    }

    @Override
    public void request(byte[] data, ResponseCallback<byte[]> callback,
                        RequestControl requestControl) throws SailfishException {
        requestWithFuture(data, callback, requestControl);
    }

    public ResponseFuture<byte[]> requestWithFuture(byte[] data, ResponseCallback<byte[]> callback,
                                                    RequestControl requestControl) throws SailfishException {
        channelStatusCheck();
        initChannel();
        final RequestProtocol protocol = newRequest(requestControl);
        protocol.oneway(false);
        protocol.body(data);

        ResponseFuture<byte[]> respFuture = new BytesResponseFuture(protocol.packetId());
        respFuture.setCallback(callback, requestControl.timeout());
        //trace before write
        Tracer.trace(this, protocol.packetId(), respFuture);
        ChannelFuture future = nettyChannel.writeAndFlush(protocol).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    String errorMsg = "write fail!";
                    if (null != future.cause()) {
                        errorMsg = StrUtils.exception2String(future.cause());
                    }
                    //FIXME maybe need more concrete error, like WriteOverFlowException or some other special exceptions
                    Tracer.erase(ResponseProtocol.newErrorResponse(protocol.packetId(), errorMsg,
                        RemotingConstants.RESULT_FAIL));
                }
            }
        });
        try {
            if (requestControl.sent()) {
                boolean ret = future.await(requestControl.timeout());
                if (!ret) {
                    future.cancel(true);
                    throw new SailfishException(ExceptionCode.TIMEOUT, "oneway request timeout");
                }
            }
        } catch (InterruptedException cause) {
            throw new SailfishException(ExceptionCode.INTERRUPTED, "interrupted exceptions");
        }
        return respFuture;
    }

    @Override
    public void close() {
        close(0);
    }

    @Override
    public void close(int timeout) {
        ParameterChecker.checkNotNegative(timeout, "timeout");
        if (this.isClosed()) {
            return;
        }
        synchronized (this) {
            if (this.isClosed()) {
                return;
            }
            this.closed = true;
            //deal unfinished requests, response with channel closed exception
            long start = System.currentTimeMillis();
            while (CollectionUtils.isNotEmpty(Tracer.peekPendingRequests(this))
                   && (System.currentTimeMillis() - start < timeout)) {
                LockSupport.parkNanos(1000 * 1000 * 10L);
            }
            Map<Integer, Object> pendingRequests = Tracer.popPendingRequests(this);
            if (CollectionUtils.isEmpty(pendingRequests)) {
                return;
            }
            for (Integer packetId : pendingRequests.keySet()) {
                Tracer.erase(ResponseProtocol.newErrorResponse(packetId,
                    "unfinished request because of channel:" + nettyChannel.toString() + " be closed",
                    RemotingConstants.RESULT_FAIL));
            }
            RemotingUtils.closeChannel(nettyChannel);
        }
    }

    @Override
    public Channel doConnect(final ExchangeClientConfig config) throws SailfishException {
        MsgHandler<Protocol> handler = new MsgHandler<Protocol>() {
            @Override
            public void handle(ChannelHandlerContext context, Protocol msg) {
                if (msg.request()) {
                    //TODO
                } else {
                    Tracer.erase((ResponseProtocol) msg);
                }
            }
        };
        Bootstrap boot = configureBoostrap(config, handler);
        try {
            return boot.connect().syncUninterruptibly().channel();
        } catch (Throwable cause) {
            throw new SailfishException(cause);
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public boolean isAvailable() {
        if (isClosed()) {
            return false;
        }
        if (this.lazyWithOutInit) {
            return true;
        }
        boolean isAvailable = false;
        if (!reconnectting && !(isAvailable = available())) {
            synchronized (this) {
                if (!reconnectting && !(isAvailable = available())) {
                    //add reconnect task
                    ReconnectManager.INSTANCE.addReconnectTask(this);
                    this.reconnectting = true;
                }
            }
        }
        return isAvailable;
    }

    private boolean available() {
        return null != this.nettyChannel && this.nettyChannel.isOpen() && this.nettyChannel.isActive();
    }

    private void channelStatusCheck() throws SailfishException {
        if (isClosed()) {
            throw new SailfishException(ExceptionCode.INVOKE_ON_CLOSED_CHANNEL,
                "current channel closed already, can't invoke anymore");
        }
    }

    private void initChannel() throws SailfishException {
        if (!clientConfig.isLazyConnection()) {
            return;
        }
        if (null != nettyChannel) {
            return;
        }
        synchronized (this) {
            if (null != nettyChannel) {
                return;
            }
            this.nettyChannel = doConnect(this.clientConfig);
            this.lazyWithOutInit = false;
        }
    }

    private Bootstrap configureBoostrap(final ExchangeClientConfig config, final MsgHandler<Protocol> handler) {
        Bootstrap boot = newBootstrap();
        boot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.connectTimeout());
        if (config.mode() == ChannelMode.simple && null != config.localAddress()) {
            boot.localAddress(config.localAddress().host(), config.localAddress().port());
        }
        boot.remoteAddress(config.address().host(), config.address().port());
        EventLoopGroup eventLoopGroup = NettyPlatformIndependent.newEventLoopGroup(config.iothreads(),
            new DefaultThreadFactory(config.iothreadName()));
        boot.group(eventLoopGroup);

        final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(config.codecThreads(),
            new DefaultThreadFactory(config.codecThreadName()));
        boot.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                ch.attr(ChannelAttrKeys.idleTimeout).set(config.idleTimeout());
                ch.attr(ChannelAttrKeys.maxIdleTimeout).set(config.maxIdleTimeout());
                if (ChannelMode.readwrite.equals(config.mode())) {
                    ch.attr(ChannelAttrKeys.writeChannel).set(config.isWriteConnection());
                    ch.attr(ChannelAttrKeys.channelIndex).set(config.channelIndex());
                    ch.attr(ChannelAttrKeys.uuid).set(config.uuid());
                }
                //TODO should increase ioRatio when every ChannelHandler bind to executorGroup?
                pipeline.addLast(executorGroup, new RemotingEncoder());
                pipeline.addLast(executorGroup, new RemotingDecoder());
                pipeline.addLast(executorGroup, new IdleStateHandler(config.idleTimeout(), 0, 0));
                pipeline.addLast(executorGroup, new ChannelEventsHandler(true));
                pipeline.addLast(executorGroup, new ShareableSimpleChannelInboundHandler(handler, true));
            }
        });
        return boot;
    }
}