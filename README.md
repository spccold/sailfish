# sailfish(coding......, stay tuned!)
A reliable, scalable, fast remoting module for rapid development of high performance network program

##设计理念
* 一个通用的remoting模块应该致力于tcp连接的管理（新建，复用，异常恢复等）和二进制的传输，除此之外不应该再承担任何责任
* 序列化，压缩／解压缩等均应该是可插拔的， 并且独立于remoting模块