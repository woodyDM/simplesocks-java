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

        channel.write(new NoAuthConnectionRequest());
        channel.write(new AuthConnectionRequest("1234呵呵哒"));
        channel.write(new ProxyRequest(ProxyRequest.Type.DOMAIN,10090,"google.com~!@#哈😊"));
        channel.write(new EndProxyRequest());
        channel.write(new ServerResponse(DataType.CONNECT_RESPONSE, ServerResponse.Code.SUCCESS));
        channel.write(new ServerResponse(DataType.PROXY_RESPONSE, ServerResponse.Code.FAIL));
        channel.writeAndFlush(new ServerResponse(DataType.END_PROXY_RESPONSE, ServerResponse.Code.SUCCESS));

        String msg = "你好，这里是来自proxy data的数据!";
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        ProxyDataRequest proxyDataRequest = new ProxyDataRequest(byteBuf);
        channel.writeAndFlush(proxyDataRequest);

    }
}
