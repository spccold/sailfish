# sailfish(coding......, stay tuned!)
* A reliable, scalable, fast remoting module for rapid development of high performance network program
* with so many advanced future,  I guarantee you'll like it!



##设计理念
* 一个通用的remoting模块应该致力于tcp连接的管理（新建, 关闭, 流控, 复用, 异常恢复等）和简单二进制数据的传输，除此之外不应该再承担任何责任

## The current status of sailfish
* Basic Implementation So Far
	* SimpleExchangeChannel with eager or lazy netty channel
	* MultiConnctionsChannel with multiple SimpleExchangeChannel
	* ReadWriteSplittingChannel with multiple SimpleExchangeChannel but support for read write splitting, this means some connections used for write only and other connections used for read only
* Something Else (You Should Pay Attention ?)
	* initial implementation without any refactor and check
	* i will spent a lot of time on this exciting project, i require your constant focus!

## some articles about netty 
* http://netty.io/wiki/using-as-a-generic-library.html
* http://www.antonkharenko.com/2015/08/netty-best-practices-distilled.html
* https://github.com/netty/netty/issues/1759