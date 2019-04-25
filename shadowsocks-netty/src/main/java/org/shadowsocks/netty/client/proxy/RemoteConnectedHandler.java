package org.shadowsocks.netty.client.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;

/**
 * 远程连接目标服务器handler
 * 当连接成功后，触发数据转发
 */
public final class RemoteConnectedHandler extends ChannelInboundHandlerAdapter {

    private final Promise<Channel> promise;

    public RemoteConnectedHandler(Promise<Channel> promise) {
        this.promise = promise;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);
        promise.setSuccess(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        promise.setFailure(throwable);
    }
}
