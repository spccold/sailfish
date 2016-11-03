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
package org.netty.demos.events;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * <pre>
 * run1(close)------ 正常与server建立连接, 然后client主动close channel, 发生的事件如下:
 *        client--- handlerAdded->channelRegistered->connect->channelActive->read->
 *                  close->channelInactive->channelUnregistered->handlerRemoved
 *        server--- handlerAdded->channelRegistered->channelActive->read->channelReadComplete
 *                  ->read->channelInactive->channelUnregistered->handlerRemoved
 * run1(disconnect)- 正常与server建立连接, 然后client主动disconnect channel, 发生的事件如下:
 *        client--- handlerAdded->channelRegistered->connect->channelActive->read->
 *                  close->channelInactive->channelUnregistered->handlerRemoved
 *        server--- handlerAdded->channelRegistered->channelActive->read->channelReadComplete
 *                  ->read->channelInactive->channelUnregistered->handlerRemoved
 *                  
 * run2(close)------ 正常与server建立连接, 然后server主动close channel, 发生的事件如下:
 *        client--- handlerAdded->channelRegistered->connect->channelActive->read->
 *                  channelReadComplete->read->channelInactive->channelUnregistered->handlerRemoved
 *        server--- handlerAdded->channelRegistered->channelActive->read->close->
 *                  channelInactive->channelUnregistered->handlerRemoved
 * run3(disconnect)- 正常与server建立连接, 然后server主动disconnect channel, 发生的事件如下:
 *        client--- handlerAdded->channelRegistered->connect->channelActive->read->
 *                  channelReadComplete->read->channelInactive->channelUnregistered->handlerRemoved
 *        server--- handlerAdded->channelRegistered->channelActive->read->close->
 *                  channelInactive->channelUnregistered->handlerRemoved
 *                  
 * run3(close)------ 正常与server建立连接, 然后server主动close accept channel, 发生的事件如下:
 *        client--- handlerAdded->channelRegistered->connect->channelActive->read->
 *                  channelInactive->channelUnregistered->handlerRemoved
 *        server--- handlerAdded->channelRegistered->channelActive->read->
 *                  channelInactive->channelUnregistered->handlerRemoved
 * run3(disconnect)- 正常与server建立连接, 然后server主动disconnect accept channel, 发生的事件如下:
 *        client--- handlerAdded->channelRegistered->connect->channelActive->read->
 *                  channelInactive->channelUnregistered->handlerRemoved
 *        server--- handlerAdded->channelRegistered->channelActive->read->
 *                  channelInactive->channelUnregistered->handlerRemoved
 * </pre>
 * 
 * 可以发现相同场景下, 执行close与执行disconnect具有相同的事件(包含顺序)
 * @author spccold
 * @version $Id: EventsWhenInvokeCloseOrDisconnectOnChannel.java, v 0.1 2016年11月3日 下午5:13:49 jileng Exp $
 */
public class EventsWhenInvokeCloseOrDisconnectOnChannel {
    private static final Logger  logger        = LoggerFactory.getLogger(EventsWhenInvokeCloseOrDisconnectOnChannel.class);

    private static final String  HOST          = "127.0.0.1";
    private static final int     PORT          = 13141;

    private static final boolean showClientLog = true;
    private static final boolean showServerLog = true;

    public static void main(String[] args) throws Exception {
        //run1(true);
        //run1(false);
        //run2(true);
        //run2(false);
        //run3(true);
        //run3(false);
    }

    public static void run1(boolean close) {
        ServerBootstrap serverBootstrap = newServerAndStart1();
        Bootstrap bootstrap = newClientAndInvoke1(close);
        bootstrap.group().shutdownGracefully();
        serverBootstrap.group().shutdownGracefully();
        serverBootstrap.childGroup().shutdownGracefully();
    }

    public static Bootstrap newClientAndInvoke1(boolean close) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1));
        bootstrap.remoteAddress(HOST, PORT);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                    new ChannelEventsHandler("-----------------------client-----------------------", showClientLog));
            }
        });
        Channel channel = bootstrap.connect().syncUninterruptibly().channel();
        logger.error("connect to " + channel.remoteAddress().toString() + " success!");
        if(close){
            logger.error("client begin close channel......");
            channel.close().syncUninterruptibly();
            logger.error("client close channel success......");
        }else{
            logger.error("client begin disconnect channel......");
            channel.disconnect().syncUninterruptibly();
            logger.error("client disconnect channel success......");
        }
        return bootstrap;
    }

    public static ServerBootstrap newServerAndStart1() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(1));
        serverBootstrap.localAddress(HOST, PORT);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                    new ChannelEventsHandler("-----------------------server-----------------------", showServerLog));
            }
        });
        serverBootstrap.bind();
        return serverBootstrap;
    }

    public static void run2(boolean close) throws InterruptedException {
        final AtomicReference<Channel> channelReference = new AtomicReference<Channel>(null);
        CountDownLatch latch = new CountDownLatch(1);
        ServerBootstrap serverBootstrap = newServerAndStart2(latch, channelReference);
        Bootstrap bootstrap = newClientAndInvoke2(latch, channelReference, close);
        bootstrap.group().shutdownGracefully();
        serverBootstrap.group().shutdownGracefully();
        serverBootstrap.childGroup().shutdownGracefully();
    }

    public static Bootstrap newClientAndInvoke2(final CountDownLatch latch,
                                                final AtomicReference<Channel> channelReference, boolean close) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1));
        bootstrap.remoteAddress(HOST, PORT);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                    new ChannelEventsHandler("-----------------------client-----------------------", showClientLog));
            }
        });
        Channel channel = bootstrap.connect().syncUninterruptibly().channel();
        logger.error("connect to " + channel.remoteAddress().toString() + " success!");

        latch.await();
        if(close){
            logger.error("server begin close channel......");
            channelReference.get().close().syncUninterruptibly();
            logger.error("server close channel success......");
        }else{
            logger.error("server begin disconnect channel......");
            channelReference.get().disconnect().syncUninterruptibly();
            logger.error("server disconnect channel success......"); 
        }
        return bootstrap;
    }

    public static ServerBootstrap newServerAndStart2(final CountDownLatch latch,
                                                     final AtomicReference<Channel> channelReference) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(1));
        serverBootstrap.localAddress(HOST, PORT);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                    new ChannelEventsHandler("-----------------------server-----------------------", showServerLog));
                channelReference.set(ch);
                latch.countDown();
            }
        });
        serverBootstrap.bind();
        return serverBootstrap;
    }
    
    public static void run3(boolean close) throws InterruptedException{
        final AtomicReference<Channel> channelReference = new AtomicReference<Channel>(null);
        ServerBootstrap serverBootstrap = newServerAndStart3(channelReference);
        Bootstrap bootstrap = newClientAndInvoke3(channelReference, close);
        bootstrap.group().shutdownGracefully();
        serverBootstrap.group().shutdownGracefully();
        serverBootstrap.childGroup().shutdownGracefully();
    }
    
    public static Bootstrap newClientAndInvoke3(final AtomicReference<Channel> channelReference, boolean close) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1));
        bootstrap.remoteAddress(HOST, PORT);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                    new ChannelEventsHandler("-----------------------client-----------------------", showClientLog));
            }
        });
        Channel channel = bootstrap.connect().syncUninterruptibly().channel();
        logger.error("connect to " + channel.remoteAddress().toString() + " success!");

        if(close){
            logger.error("server begin close accept channel......");
            channelReference.get().close().syncUninterruptibly();
            logger.error("server close accept channel success......");
        }else{
            logger.error("server begin disconnect accept channel......");
            channelReference.get().disconnect().syncUninterruptibly();
            logger.error("server close disconnect channel success......");
        }
        return bootstrap;
    }

    public static ServerBootstrap newServerAndStart3(final AtomicReference<Channel> channelReference) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(1));
        serverBootstrap.localAddress(HOST, PORT);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                    new ChannelEventsHandler("-----------------------server-----------------------", showServerLog));
            }
        });
        channelReference.set(serverBootstrap.bind().syncUninterruptibly().channel());
        return serverBootstrap;
    }
    
}
