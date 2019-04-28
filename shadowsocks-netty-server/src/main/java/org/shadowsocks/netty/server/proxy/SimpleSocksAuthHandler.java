package org.shadowsocks.netty.server.proxy;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.common.protocol.*;
import org.shadowsocks.netty.server.proxy.relay.RelayProxyDataHandler;

@Slf4j
public class SimpleSocksAuthHandler extends SimpleChannelInboundHandler<SimpleSocksCmdRequest> {

    private boolean isProxying = false;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleSocksCmdRequest msg) throws Exception {
        DataType type = msg.getType();
        log.debug("receive {} from {}",msg,ctx.channel().remoteAddress());
        switch (type){
            case CONNECT:{
                ctx.channel().writeAndFlush(new ServerResponse(DataType.CONNECT_RESPONSE, ServerResponse.Code.SUCCESS));
                break;
            }
            default:ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exception found. {}",cause.getMessage());
        ctx.close();
    }
}
