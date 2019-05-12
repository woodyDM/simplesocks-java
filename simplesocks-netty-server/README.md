A proxy server based on Netty4 
====

How to use
---
1.mvn package  
2.Unzip the package and write your own configuration in `conf/config.xml`   
3.Run `start.sh` if on Linux or `start.bat` on Windows    
4.If you see 
` java.lang.UnsatisfiedLinkError: failed to load the required native library`
when trying to start on Linux, try to set `-Dorg.simplesocks.enable.epoll=false` in the start.sh file.

 