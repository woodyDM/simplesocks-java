package org.shadowsocks.netty.common.netty;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ByteReceiveHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int len = msg.readableBytes();
        byte[] bytes = new byte[len];
        msg.readBytes(bytes);

        String string = new String(bytes, StandardCharsets.UTF_8);
        log.info(string);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("active channel {}",ctx.channel().remoteAddress());
    }
}
