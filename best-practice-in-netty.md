# best practice in netty
* writeAndFlush不要一直调用， 是否可以通过调用write，并且在适当的时间flush，因为每次系统flush都是一次系统调用，如果可以的话write的调用次数也应该减少，因为它会经过整个pipeline
* 如果你不是很关注write的结果，可以使用channel.voidPromise(),可以减少对象的创建
* 一直写对于处理能力较弱的接受者来说，可能会引起OutMemoryError，关注channel.isWritable()和channelhandler中的cahnnelWritabilityChanged()将会很有帮助，channel.bytesBeforeUnwritable和channel.bytesBeforeWritable()同样值得关注
* 关注write_buffer_high_water_mark和write_buffer_low_water_mark的配置， 例如high:32kb(default 64kb), low:8kb(default 32kb)
* 可以通过channelpipeline 触发custome events (pipeline.fireUserEventTriggered(MyCustomEvent)), 可以在DuplexChannelHandler中处理相应的事件
* use pooledBytebuffer as default channel options(since netty 4.1.x)	
* only use heap buffres if need to operate on byte[] in channeloutboundhandler! (perfer to use direct buffers always), take this as rule of thumb
* ByteBuf.forEanchByte()(via ByteBufProcessor) fast than normal find via range check, because
	* can eliminate range checks
	* can be created and shared
	* easier to inline by the JIT 
	* use it whenever you need to find some pattern in a ByteBuf
* other buffer tips
	* alloc() over Unpooled
	* slice(), duplicate() over copy
	* bulk operation over loops
* never block the EventLoop
	* Thread.sleep()
	* CountDownLatch.await() or any other blocking operation from java.util.concurrent
	* long-lived computationally in intensive operations
	* Blocking operations that might take a while(e.g. DB query)
* Schedule and execute tasks via EventLoop this reduces the needed Threads and also makes it's Thread-safe(notice: the task should be lightweight)
* Re-use EventLoopGroup if you can
* proxy like application which reduce context-switching to minimum by share the same EventLoop between both channels, like
		
		public class ProxyHandler extends ChannelInboundHandlerAdapter{
					public void channelActive(ChannelHandlerContext ctx){
						final channel inboundchannel = ctx.channel();
						Bootstrap  b = new Bootstrap();
						b.group(inboundchannel.eventLoop());
						...
						ChannelFuture f = b.connect(remoteHost, remotePort);
						...
					}
	} 
* always combine operations if possible when act on channel from outside the eventloop to reduce overhead of wakeups and object creation!

		//Not recommended, will create two Runnable Object
		channel.write(msg1); 
		channel.writeAndFlush(msg3);
		//Combine for the WIN!
		channel.eventLoop().execute(new Runnable(){
		   public void tun(){
			  	channel.write(msg1); 
			    channel.writeAndFlush(msg3);
	    }});	
* perfer to use ChannelHandlerContext to writeAndFlush than Channel, because shortest path as possible to get maximal performance
* Share ChannelHandlers if stateless(@ChannelHandler.Shareable)
* Remove ChannelHandler once not needed anymore, this keep the channelPipeline as short as possible and so eliminate overhead of traversing as much as possible(e.g. UniPortHandler)
* channel auto-read option? i can't understand
* epoll for linux 
	* 	Less gc as NIO
	* 	Less synchronization
	* 	Edge-Triggered!
	* 	Supports TCP_CORK (with channel option) 