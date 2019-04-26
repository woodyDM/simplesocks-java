package org.shadowsocks.netty.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.common.protocol.*;

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

        channel.write(new NoAuthConnectionRequest());
        channel.write(new AuthConnectionRequest("1234呵呵哒"));
        channel.write(new ProxyRequest(ProxyRequest.Type.DOMAIN,9090,"google.com~!@#哈😊"));
        channel.write(new EndProxyRequest());
        channel.write(new ServerResponse(DataType.CONNECT_RESPONSE, ServerResponse.Code.SUCCESS));
        channel.write(new ServerResponse(DataType.PROXY_RESPONSE, ServerResponse.Code.FAIL));
        channel.writeAndFlush(new ServerResponse(DataType.END_PROXY_RESPONSE, ServerResponse.Code.SUCCESS));

    }
}
