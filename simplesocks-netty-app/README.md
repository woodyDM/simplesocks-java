A proxy client based on Socks5 and SimpleSocksProtocol
====

使用
---
1.mvn package  
2.unzip target/simplesocks-netty-app-0.0.1.jar. Edit conf/config.xml,add your own host information.  
3.Run `start.sh` if on Linux or `start.bat` on Windows  

chrome代理设置
---
需要安装Proxy SwitchySharp插件，然后设置socks代理，指定本地代理服务器就行了

firefox代理设置
---
firefox自带设置socks代理功能，直接设置就行了

更多详细介绍
---
[基于netty4.0的shadowsocks客户端](http://my.oschina.net/OutOfMemory/blog/744475)