package org.shadowsocks.netty.client.proxy.relay;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.shadowsocks.netty.common.netty.SimpleSocksProtocolDecoder;
import org.shadowsocks.netty.common.netty.SimpleSocksProtocolEncoder;

/**
 * 远程连接目标服务器handler
 * 当连接成功后，触发数据转发
 */
public final class RelayHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private DirectRelayClient client;

    public RelayHandlerInitializer( DirectRelayClient client) {
        this.client = client;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new RelayProxyDataHandler(client));
    }

}
