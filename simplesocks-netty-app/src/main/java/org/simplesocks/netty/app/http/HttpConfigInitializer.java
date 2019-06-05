package org.simplesocks.netty.app.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.simplesocks.netty.app.config.AppConfiguration;

public class HttpConfigInitializer extends ChannelInitializer<SocketChannel> {


    private AppConfiguration configuration;
    private final int MAX_SIZE = 512 * 1024;

    public HttpConfigInitializer(AppConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("codec",new HttpServerCodec())
                .addLast("aggregator", new HttpObjectAggregator(MAX_SIZE))
                .addLast("configHandler",new ConfigDispatchHandler());
    }
}
