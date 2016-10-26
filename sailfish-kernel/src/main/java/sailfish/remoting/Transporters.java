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

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import sailfish.remoting.codec.RemotingDecoder;
import sailfish.remoting.codec.RemotingEncoder;
import sailfish.remoting.protocol.Protocol;

/**
 * 
 * @author spccold
 * @version $Id: Transporters.java, v 0.1 2016年10月9日 下午9:03:46 jileng Exp $
 */
public class Transporters {
    private static final ConcurrentMap<String /**remote address(ip:prot)*/, Channel> channels = new ConcurrentHashMap<>();
    private static Bootstrap bootstrap;
    private static ServerBootstrap serverBootstrap;
    
    public static Channel connect(RemotingConfig config, MsgHandler<Protocol> handler){
        ensureBootstrap(config,handler);
        NettyChannel nettyChannel = new NettyChannel(config.getConnections());
        nettyChannel.addChannel(bootstrap.connect(config.getRemoteAddress()).awaitUninterruptibly().channel());
        channels.putIfAbsent(config.getRemoteAddress().toString(), nettyChannel);
        return nettyChannel;
    }
    
    public static void bind(InetSocketAddress localAddress, MsgHandler<Protocol> handler){
        ensureServerBootstrap(localAddress, handler);
        serverBootstrap.bind();
    }
    
    private static Object serverBootstrapLock = new Object();
    private static void ensureServerBootstrap(InetSocketAddress localAddress, final MsgHandler<Protocol> handler){
        if(null != serverBootstrap){
            return;
        }
        synchronized (serverBootstrapLock) {
            if(null != serverBootstrap){
                return;
            }
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 2048);
            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()));
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(localAddress);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new RemotingEncoder());
                    pipeline.addLast(new RemotingDecoder());
                    //need IdleHandler in future
                    pipeline.addLast(new SimpleChannelInboundHandler<Protocol>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, Protocol msg) throws Exception {
                            handler.handle(ctx, msg);
                        }
                    });
                }
            });
        }
    }
    private static Object bootstrapLock = new Object();
    private static void ensureBootstrap(final RemotingConfig config, final MsgHandler<Protocol> handler){
        if(null != bootstrap){
            return;
        }
        synchronized (bootstrapLock) {
            if(null != bootstrap){
                return;
            }
            bootstrap = new Bootstrap();
            NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(config.getIoThreads(), 
                new DefaultThreadFactory(config.getIoThreadName()));
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout());
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>(){
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new RemotingEncoder());
                    pipeline.addLast(new RemotingDecoder());
                    //need IdleHandler in future
                    pipeline.addLast(new SimpleChannelInboundHandler<Protocol>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, Protocol msg) throws Exception {
                            handler.handle(ctx, msg);
                        }
                    });
                }
            });
        }
    }
}
