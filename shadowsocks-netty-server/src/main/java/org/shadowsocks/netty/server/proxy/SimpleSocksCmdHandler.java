package org.shadowsocks.netty.server.proxy;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.common.protocol.*;
import org.shadowsocks.netty.server.proxy.relay.RelayProxyDataHandler;

@Slf4j
public class SimpleSocksCmdHandler extends SimpleChannelInboundHandler<SimpleSocksCmdRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleSocksCmdRequest msg) throws Exception {
        DataType type = msg.getType();
        log.info("receive {} from {}",msg,ctx.channel().remoteAddress());
        switch (type){
            case CONNECT:{
                ctx.channel().writeAndFlush(new ServerResponse(DataType.CONNECT_RESPONSE, ServerResponse.Code.SUCCESS));
                break;
            }
            case PROXY:{
                ProxyRequest request = (ProxyRequest)msg;
                RelayProxyDataHandler relayProxyDataHandler = new RelayProxyDataHandler(request);
                ctx.pipeline().addLast(relayProxyDataHandler);
                log.info("add new handler to proxy data {}.",request);
                ctx.fireChannelRead(request);
                break;
            }
            default:ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }
}
