package org.shadowsocks.netty.server.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.server.auth.AuthProvider;

/**
 * when exception happens, close channel and clear auth information.
 */
@Slf4j
public class ExceptionHandler extends ChannelInboundHandlerAdapter {


    private AuthProvider authProvider;

    public ExceptionHandler(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exception happens, closing ctx and remove auth. MSG:{}",cause.getMessage());
        authProvider.remove(ctx.channel().remoteAddress().toString());
        ctx.channel().close();
    }
}
