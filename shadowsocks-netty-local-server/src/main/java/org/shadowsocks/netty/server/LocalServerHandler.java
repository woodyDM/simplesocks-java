package org.shadowsocks.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.common.protocol.*;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
@Slf4j
public class LocalServerHandler extends SimpleChannelInboundHandler<SimpleSocksCmdRequest> {


    int port = 8086;
    int port2 = 8087;
    boolean one=true;
    String host = "localhost";

    int counter = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("connect to {}, send auth.",ctx.channel().remoteAddress());
        ctx.channel().writeAndFlush(new AuthConnectionRequest("123456"));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleSocksCmdRequest msg) throws Exception {
        log.info("receive {}",msg);
        Channel channel = ctx.channel();
        DataType type = msg.getType();
        switch (type){
            case CONNECT_RESPONSE:{
                ProxyRequest proxyRequest = new ProxyRequest(ProxyRequest.Type.DOMAIN, port, host);
                channel.writeAndFlush(proxyRequest);
                break;
            }
            case PROXY_RESPONSE: {
                write(channel);
                break;
            }

            case PROXY_DATA:{
                ProxyDataRequest request = (ProxyDataRequest)msg;
                String ms = new String(request.getBytes(), StandardCharsets.UTF_8);
                log.info("receive msg: {}",ms);
                write(channel);
                break;
            }

            case END_PROXY_RESPONSE:{
                if(one){
                    ProxyRequest proxyRequest = new ProxyRequest(ProxyRequest.Type.DOMAIN, port2, host);
                    counter=5;
                    one = false;
                    channel.writeAndFlush(proxyRequest);
                    log.info("ReProxy to {}",proxyRequest);
                }
                break;
            }

        }
    }


    void write(Channel channel){
        if(counter<10){
            counter++;
            String msgStr = "你好！！netty😊 " + counter;
            byte[] data = msgStr.getBytes(StandardCharsets.UTF_8);
            ProxyDataRequest request = new ProxyDataRequest(data);
            channel.writeAndFlush(request);
        }else{
            channel.writeAndFlush(new EndProxyRequest());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exception in handler",cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }
}
