## Notice
Totally based on
[shadowsocks-netty](https://github.com/ksfzhaohui/shadowsocks-netty)
and
[基于netty4.0的shadowsocks客户端](http://my.oschina.net/OutOfMemory/blog/744475)  
Thanks.


## Under construction  
1. LocalServer and RemoteServer protocol:Simple Socks Protocol;  
2. the protocol netty codec. 
3. client to server connection pool(pool authenticated channel);
4. data encryption between LocalServer and RemoteServer;
 
## Map



              Socks5                SimpleSocksProtocol                 Http
    LocalAPP  ----->  LocalServer  ---------------------> RemoteServer -------> TargetServer
                                          

## Simple Socks Protocol   

client to server:  

 
1.connect  

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  4| |content-length-total |
|  1| 0x01| connect cmd |
|  1| 0x01 no auth<br> 0X02 password| authType|
|  ?|  |password (UTF8 bytes)   |


2.proxy    
Request to proxy data to target server.  
Used when connect ok.

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  4|     |content-length-total |
|  1| 0x02| proxy cmd |
|  1| 0x01 IPV4 <br> 0x03 domain <br> 0x04 IPV6| type|
|  2|     |request port |
|  1|     | byte offset |
|  ?|     |request server(UTF8) <br>  *All bytes offset |


3.proxy data   
Send proxy data to remote server, used when proxy request ok.

server send proxy data or proxy data response (failed)

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  4| |content-length-total |
|  1| 0x03| proxy cmd |
|  ?|     | data|
 
 
 
4.end proxy   
End proxy. used when no more data to send.
At this state , local server can send a new ProxyRequest to 
start new proxy data channel. 

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  4| |content-length-total |
|  1| 0x04| end proxy cmd |

5.server responses(all server response same)   

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  4| |content-length-total |
|1 | | response-type|
|  1| 0x01 success<br> 0x02 fail| result|
 
