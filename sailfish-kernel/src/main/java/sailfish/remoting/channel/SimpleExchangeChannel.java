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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import sailfish.remoting.RequestControl;
import sailfish.remoting.Tracer;
import sailfish.remoting.codec.RemotingDecoder;
import sailfish.remoting.codec.RemotingEncoder;
import sailfish.remoting.configuration.ExchangeClientConfig;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.future.BytesResponseFuture;
import sailfish.remoting.future.ResponseFuture;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.handler.ShareableSimpleChannelInboundHandler;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.protocol.RequestProtocol;
import sailfish.remoting.protocol.ResponseProtocol;
import sailfish.remoting.utils.RemotingUtils;

/**
 * with only one connection and the connection need to be initialized immediately
 * 
 * @author spccold
 * @version $Id: SimpleExchangeChannel.java, v 0.1 2016年10月26日 下午9:08:24 jileng Exp $
 */
public class SimpleExchangeChannel extends AbstractExchangeChannel implements ExchangeChannel{
    private Channel nettyChannel;

    public SimpleExchangeChannel(ExchangeClientConfig config) throws SailfishException{
        this.nettyChannel = doConnect(config);
    }

    @Override
    public void oneway(byte[] data, RequestControl requestControl) {
        RequestProtocol protocol = newRequest(requestControl);
        protocol.oneway(true);
        protocol.body(data);
        //FIXME wait write success?
        nettyChannel.writeAndFlush(protocol);
    }

    @Override
    public ResponseFuture<byte[]> request(byte[] data, RequestControl requestControl) {
        RequestProtocol protocol = newRequest(requestControl);
        protocol.oneway(false);
        protocol.body(data);
        nettyChannel.writeAndFlush(protocol);
        ResponseFuture<byte[]> future = new BytesResponseFuture(protocol.packetId());
        Tracer.trace(protocol.packetId(), future);
        return future;
    }

    @Override
    public void close() throws InterruptedException{
        RemotingUtils.closeChannel(nettyChannel);
    }

    @Override
    public void close(int timeout) throws InterruptedException{
        RemotingUtils.closeChannel(nettyChannel);
    }

    @Override
    protected Channel doConnect(final ExchangeClientConfig config) throws SailfishException{
        MsgHandler<Protocol> handler= new MsgHandler<Protocol>() {
            @Override
            public void handle(ChannelHandlerContext context, Protocol msg) {
                if(msg.request()){
                    //TODO
                }else{
                    Tracer.erase((ResponseProtocol)msg);
                }
            }
        };
        Bootstrap boot = configureBoostrap(config, handler);
        try{
            return boot.connect().syncUninterruptibly().channel();
        }catch(Throwable cause){
            throw new SailfishException(cause);
        }
    }
    
    private Bootstrap configureBoostrap(final ExchangeClientConfig config, final MsgHandler<Protocol> handler){
        Bootstrap boot = newBootstrap();
        boot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.Connecttimeout());
        if(null != config.localAddress()){
            boot.localAddress(config.localAddress().host(), config.localAddress().port());
        }
        boot.remoteAddress(config.address().host(), config.address().port());
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(config.iothreads(), new DefaultThreadFactory(config.iothreadName()));
        boot.group(eventLoopGroup);

        final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(config.codecThreads(),
            new DefaultThreadFactory(config.codecThreadName()));
        boot.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                ch.attr(ChannelAttrKeys.idleTimeout).set(config.idleTimeout());
                ch.attr(ChannelAttrKeys.maxIdleTimeout).set(config.maxIdleTimeout());
                pipeline.addLast(executorGroup, new RemotingEncoder());
                pipeline.addLast(executorGroup, new RemotingDecoder());
                pipeline.addLast(executorGroup, new IdleStateHandler(config.idleTimeout(), 0, 0));
                pipeline.addLast(executorGroup, new ShareableSimpleChannelInboundHandler(handler));
            }
        });
        return boot;
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
