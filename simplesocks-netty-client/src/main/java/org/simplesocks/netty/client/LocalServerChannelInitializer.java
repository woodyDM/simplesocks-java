package org.simplesocks.netty.client;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.simplesocks.netty.common.netty.SimpleSocksDecoder;
import org.simplesocks.netty.common.netty.SimpleSocksProtocolDecoder;
import org.simplesocks.netty.common.netty.SimpleSocksProtocolEncoder;

public class LocalServerChannelInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {

    private SimpleSocksProtocolClient client;

    public LocalServerChannelInitializer(SimpleSocksProtocolClient client) {
        this.client = client;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        LengthFieldBasedFrameDecoder decoder = SimpleSocksDecoder.newLengthDecoder();
        ch.pipeline()
                .addLast(decoder)
                .addLast(new SimpleSocksProtocolDecoder())
                .addLast(new LocalServerHandler(client))
                .addFirst(new SimpleSocksProtocolEncoder());
    }
}
