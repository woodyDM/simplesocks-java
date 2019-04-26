## Notice
Total a copy of
[shadowsocks-netty](https://github.com/ksfzhaohui/shadowsocks-netty)
and
[基于netty4.0的shadowsocks客户端](http://my.oschina.net/OutOfMemory/blog/744475)


## TODO  
1. client and server protocol;  
2. client to server connection pool;
3. encrypt between client and server;
4. netty handler.  


## Protocol   

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

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  4| |content-length-total |
|  1| 0x03| proxy cmd |
|  ?|     | data|
 
 
 
4.end proxy

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  4| |content-length-total |
|  1| 0x04| end proxy cmd |

5.server response(all server response is same)

|len| byte|description|
|:----:|:---:|:-------:|
|  1| 0x01| version number |
|  1| 0x01 success<br> 0x02 fail| result|
 