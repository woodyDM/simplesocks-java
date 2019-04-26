package org.shadowsocks.netty.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ByteIncomingHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("client channel {}", ctx.channel().remoteAddress());
        for (int i = 0; i <1000; i++) {
            String st = "fwejkl你好！！+ "+i;
            ctx.channel().write(new StringCmdRequest(st));
        }
        ctx.channel().writeAndFlush(new StringCmdRequest("你好，hello st java!~"));
    }
}
