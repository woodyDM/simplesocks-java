## Description  
Simplesocks is a tcp proxy software based on Socks5 and SimpleSocksProtocol for personal proxy
usage.
 

## Notice
Thanks to :   
1. [shadowsocks](https://github.com/shadowsocks/shadowsocks)  
2. [shadowsocks-netty](https://github.com/ksfzhaohui/shadowsocks-netty)
3. [基于netty4.0的shadowsocks客户端](http://my.oschina.net/OutOfMemory/blog/744475)    
4. [Special list](https://github.com/gfwlist/gfwlist)  
  
 


## Features  
1.  [x]Local socks5 proxy server;  
2.  [x]Self-defined SimpleSocksProtocol;   
3.  ~~SimpleSocksProtocol connection pool in client.(pool authenticated channel);~~
(It seems hard to create a pool with non-blocking synchronizations)     
4.  [x]Data encryption between LocalServer and RemoteServer;     
5.  [x]special pac list;  
6.  []Setting pages on browsers;
7.  []Traffic monitor;  

## DataFlow



              Socks5                SimpleSocksProtocol                 (Http)
    LocalAPP  ----->  LocalServer  ---------------------> RemoteServer -------> TargetServer
                                          encryt   

## SimpleSocksProtocol   

Simply:client to server  

1.connect request

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  4| |content length total |
|  1| 0x01|  connect cmd |
|1| ? | auth password length|
|1| ? | encrypt type string length|
|?|  |auth password content (UTF8 bytes),auth is used for encryption secret key|
|?|  | encrypt type (UTF8 bytes) (supports:aes-cfb ,aes-cbc, caesar)|
|1|0x01 IPV4 <br> 0x03 domain <br> 0x04 IPV6|host type|
|  2|     |request port |
|  1|     | byte offset |
|  ?|     |request server content(UTF8) <br>  *All bytes offset |

1.1 server response

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  4| |content length total |
|  1|0x11 | connect cmd response|
|  1| 0x01 success<br> 0x02 fail| result|
| 1 |? | encrypt type string length|
| 1 |? | encrypt iv length|
|  ?| | encrypt type|
|  ?| | encrypt iv bytes|

2.proxy data   
Send proxy data to remote server, used when connect request ok.

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  4| |content length total |
|  1| 0x02| proxy cmd |
|  1| | id length|
|  ?| | id bytes |
|  ?| | data|

2.1 the server should proxy the data and response :

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  4| |content length total |
|  1| 0x12| proxy cmd response |
|  1| 0x01 success<br> 0x02 fail| result|
|1 | | id length|
|? | | id bytes |

3.server send target data to client using the proxy data structure.  

 
4.if client all data are sent, no need to 
send end request , client directly close channel.

5.If the channel is idle for long time (defined by server) ,server need 
close channel.


 
