package org.shadowsocks.netty.client.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.shadowsocks.netty.common.netty.ByteIncomingHandler;
import org.shadowsocks.netty.common.netty.SimpleSocksProtocolEncoder;

/**
 * 远程连接目标服务器handler
 * 当连接成功后，触发数据转发
 */
public final class RelayHandlerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                1,4,-5,5);
        ch.pipeline()
                .addFirst(decoder)
                .addFirst(new SimpleSocksProtocolEncoder())

                .addFirst(new ByteIncomingHandler());
    }

}
