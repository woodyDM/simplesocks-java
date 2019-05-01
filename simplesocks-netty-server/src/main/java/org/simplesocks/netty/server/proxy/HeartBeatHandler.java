package org.simplesocks.netty.server.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.protocol.SimpleSocksCmdRequest;

@Slf4j
public class HeartBeatHandler extends SimpleChannelInboundHandler<SimpleSocksCmdRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SimpleSocksCmdRequest simpleSocksCmdRequest) throws Exception {
        channelHandlerContext.fireChannelRead(simpleSocksCmdRequest);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            ctx.channel().close().addListener(future -> {
                log.warn("Too long to interactive with remote channel. event={},close={}", event.state(),future.isSuccess());
            });
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }


}
