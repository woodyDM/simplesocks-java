package org.simplesocks.netty.server.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.util.ServerUtils;
import org.simplesocks.netty.server.auth.AuthProvider;

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
        ServerUtils.logException(log, cause);
        authProvider.remove(ctx.channel().remoteAddress().toString());
        ctx.channel().close();
    }
}
