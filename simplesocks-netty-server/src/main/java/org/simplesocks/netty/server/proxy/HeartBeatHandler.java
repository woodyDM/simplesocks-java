package org.simplesocks.netty.server.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.protocol.SimpleSocksMessage;

@Slf4j
public class HeartBeatHandler extends SimpleChannelInboundHandler<SimpleSocksMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SimpleSocksMessage simpleSocksMessage) throws Exception {
        channelHandlerContext.fireChannelRead(simpleSocksMessage);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            /**
             * close channel idle too long.
             */
            IdleStateEvent event = (IdleStateEvent)evt;
            ctx.close()
                    .addListener(future -> {
                        ctx.close();
                        log.warn("Too long to interactive with remote channel. event={},close={}", event.state(),future.isSuccess());

                    });

        }else {
            super.userEventTriggered(ctx,evt);
        }
    }


}
