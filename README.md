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

## netty's recycler
* 对象的创建与回收
    * 创建
        * 是否在固定的线程中创建, Recycler依赖ThreadLocal或者FastThreadLocal(in netty), 与之关联这例如Stack，Handle等对象，在集中的线程中使用Recycler可以减少对象的创建
        * 线程是否是FastThreadLocalThread，FastThreadLocalThread在ThreadLocal实现上可以获取速度优势，Recycler又是依赖ThreadLocal的，所以尽可能在FastThreadLocalThread中使用Recycler(DefaultThreadFactory产生的线程就是FastThreadLocalThread)

    * 回收
        * 是否在创建的线程中回收, 这样在recycle时是最及时的
        * 是否在多个异步线程中回收， 异步线程中recycle会产生WeakOrderQueue, 一个线程对应一个WeakOrderQueue(所有Queue形成一个链),所以在固定或者相同的线程中回收既可以提高recycle速度，也可以减少WeakOrderQueue的创建
 
## some articles about netty 
* http://netty.io/wiki/using-as-a-generic-library.html
* http://www.antonkharenko.com/2015/08/netty-best-practices-distilled.html
* https://github.com/netty/netty/issues/1759