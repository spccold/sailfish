# sailfish
* A reliable, scalable, fast remoting module for rapid development of high performance network program
* with so many advanced future,  I guarantee you'll like it!


## design concept
* 一个通用的remoting模块应该致力于tcp连接的管理（新建, 关闭, 流控, 复用, 异常恢复等）和简单二进制数据的传输，除此之外不应该再承担任何责任

## sailfish's advantage
* 精简的二进制协议(后面会考虑协议的动态替换，其实也简单的)
* 支持高性能写, 单连接的写操作越多，收益越明显(自动合并多个写操作, 减少不必要的flush，每次flush都是一次系统调用，具有较大的开销)
* 多种channle模型(当连接数大于1时，还可以设置channel反转)
    * 单连接
        * egaer channel, 在初始化客户端的时候就初始化channl
        * lazy channel, 在第一次真正发起调用的时候完成初始化
    * 多连接
        * 所有请求会以round-robin的方式分配channel
    * 多连接，但支持读写分离(一部分连接用于读， 另一份连接用于写)
* 对于需要大量重复创建并且生命周期短的对象用netty recycler进行回收利用，减少对象创建的开销和gc的压力
* 序列化方式，压缩方式无关的设计，因为sailfish只进行二进制的传输，至于你使用何种序列化，压缩方式，是不关心的(`但是会把这些信息传输到对端`)，这意味着请求和响应可以采用不同的序列化和压缩方式，并且每一次请求都可以使用不同的序列化和压缩方式(`你可能根据数据量，对象类型采用不同的序列化方式`)，只要你把此次请求的序列化方式, 序列化方式通过协议传输给对方就好了

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