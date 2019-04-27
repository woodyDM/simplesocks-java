package org.shadowsocks.netty.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.common.protocol.*;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ByteIncomingHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("client channel {}", ctx.channel().remoteAddress());
        Channel channel = ctx.channel();
        channel.writeAndFlush(new NoAuthConnectionRequest());
    }
}
