package org.shadowsocks.netty.client.proxy.relay;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.Promise;
import org.shadowsocks.netty.common.netty.SimpleSocksProtocolDecoder;
import org.shadowsocks.netty.common.netty.SimpleSocksProtocolEncoder;

/**
 * 远程连接目标服务器handler
 * 当连接成功后，触发数据转发
 */
public final class RelayHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private RelayClient client;

    public RelayHandlerInitializer( RelayClient client) {
        this.client = client;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                1,4,-5,5);
        ch.pipeline()
                .addLast(decoder)
                .addLast(new SimpleSocksProtocolDecoder())
                .addLast(new RelayHandshakeHandler(client))
                .addFirst(new SimpleSocksProtocolEncoder());

    }

}
