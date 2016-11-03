# something about ChannelPipeline
		
		------HeadContext(OutBound)--------------------------|
		|								 |              fireChannelRead         
		|							     |		     
		------  ChannelHandler 1  ----------addLast 1
		|								 |
		|								 |
		------  ChannelHandler 2  ----------addLast 2
		|								 |
		|								 |
		------  ChannelHandler n  ----------addLast n
		|							     |
		|								 |              writeAndFlush
		------TailContext(InBound)---------------------------| 
		
		HeadContext.next = 	 TailContext
		TailContext.pre  =   HeadContext
		
		
		所有与Inbound相关的操作从HeadContext找起(next所有的Inbound)
		所欲与Outbound相关的操作从TailContext找起(pre所有的Oubound)
		
		