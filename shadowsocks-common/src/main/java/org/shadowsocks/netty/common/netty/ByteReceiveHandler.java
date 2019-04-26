package org.shadowsocks.netty.common.netty;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.common.protocol.CmdRequestFactory;
import org.shadowsocks.netty.common.protocol.SimpleSocksCmdRequest;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ByteReceiveHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int len = msg.readableBytes();
        log.info("readable = {}",len);
        SimpleSocksCmdRequest simpleSocksCmdRequest = CmdRequestFactory.newInstance(msg);
        log.info("{");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("active channel {}",ctx.channel().remoteAddress());
    }
}
