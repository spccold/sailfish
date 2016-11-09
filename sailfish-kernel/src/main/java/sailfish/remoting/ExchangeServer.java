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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import sailfish.remoting.codec.RemotingDecoder;
import sailfish.remoting.codec.RemotingEncoder;
import sailfish.remoting.configuration.ExchangeServerConfig;
import sailfish.remoting.constants.ChannelAttrKeys;
import sailfish.remoting.exceptions.SailfishException;
import sailfish.remoting.handler.ChannelEventsHandler;
import sailfish.remoting.handler.MsgHandler;
import sailfish.remoting.handler.ShareableSimpleChannelInboundHandler;
import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.utils.ParameterChecker;

/**
 * 
 * @author spccold
 * @version $Id: ExchangeServer.java, v 0.1 2016年10月26日 下午3:52:19 jileng Exp $
 */
public class ExchangeServer implements Endpoint{
    private volatile boolean isClosed = false;
    private ExchangeServerConfig config;
    private MsgHandler<Protocol> handler;
    private ServerBootstrap boot;
    public ExchangeServer(ExchangeServerConfig config, MsgHandler<Protocol> handler){
        this.config = ParameterChecker.checkNotNull(config, "ExchangeServerConfig");
        this.handler = ParameterChecker.checkNotNull(handler, "handler");
    }
    
    public void start() throws SailfishException{
        ServerBootstrap boot = newServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup(config.bossThreads(), new DefaultThreadFactory(config.bossThreadName()));
        NioEventLoopGroup io = new NioEventLoopGroup(config.iothreads(), new DefaultThreadFactory(config.iothreadName()));
        final EventExecutorGroup executor = new DefaultEventExecutorGroup(config.codecThreads(), new DefaultThreadFactory(config.codecThreadName()));
        boot.group(boss, io);
        boot.localAddress(config.address().host(), config.address().port());
        boot.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                ch.attr(ChannelAttrKeys.maxIdleTimeout).set(config.maxIdleTimeout());
                pipeline.addLast(executor, new RemotingDecoder());
                pipeline.addLast(executor, new RemotingEncoder());
                pipeline.addLast(executor, "idleStateHandler", new IdleStateHandler(config.idleTimeout(), 0, 0));
                pipeline.addLast(executor, new ChannelEventsHandler(false));
                pipeline.addLast(executor, new ShareableSimpleChannelInboundHandler(handler, false));
            }
        });
        try{
            boot.bind().syncUninterruptibly();
        }catch(Throwable cause){
            throw new SailfishException(cause);
        }
        this.boot = boot;
    }
    
    private ServerBootstrap newServerBootstrap(){
        ServerBootstrap serverBoot = new ServerBootstrap();
        serverBoot.channel(NioServerSocketChannel.class);
        // connections wait for accept
        serverBoot.option(ChannelOption.SO_BACKLOG, 1024);
        serverBoot.option(ChannelOption.SO_REUSEADDR, true);
        //replace by heart beat
        serverBoot.option(ChannelOption.SO_KEEPALIVE, false);
        serverBoot.childOption(ChannelOption.TCP_NODELAY, true);
        return serverBoot;
    }

    @Override
    public void close(){
        close(0);
    }

    @Override
    public void close(int timeout){
        synchronized (this) {
            if(isClosed)
                return;
            if(null != boot){
                try{
                    boot.group().shutdownGracefully().await(timeout);
                    boot.childGroup().shutdownGracefully().await(timeout);;
                }catch(InterruptedException cause){
                    //do nothing
                }
            }
        }
    }

    @Override
    public boolean isClosed() {
        return this.isClosed;
    }
}
