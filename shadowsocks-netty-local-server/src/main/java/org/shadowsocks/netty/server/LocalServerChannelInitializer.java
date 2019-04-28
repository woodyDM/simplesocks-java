package org.shadowsocks.netty.server;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.shadowsocks.netty.common.netty.SimpleSocksProtocolDecoder;
import org.shadowsocks.netty.common.netty.SimpleSocksProtocolEncoder;

public class LocalServerChannelInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                1,4,-5,5);
        ch.pipeline()
                .addLast(decoder)
                .addLast(new SimpleSocksProtocolDecoder())
                .addLast(new LocalServerHandler())
                .addFirst(new SimpleSocksProtocolEncoder());

    }
}
