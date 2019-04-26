package org.shadowsocks.netty.client.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RelayClientServer {

    private Bootstrap b;
    private String host;
    private int port;
    private EventLoopGroup group;
    Promise<Channel> promise ;

    public RelayClientServer(String host, int port, EventLoopGroup group, Promise<Channel> promise) {
        this.host = host;
        this.port = port;
        this.group = group;
        this.promise = promise;
        init();
    }

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup(4);
        RelayClientServer s = new RelayClientServer("localhost",10801, group,null);
    }

    private void init( ) {
        b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new RelayHandlerInitializer());
        b.connect(host, port)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            log.warn("Failed to connect to host {} port {}  , close channel.", host, port);
                        } else {
                            //only connect success
                            //the success response to local socks5 connection is send by
                            //promise above
                            log.info("Success Connect to host {} port {}  ", host, port);
                        }
                    }
                });
    }

}
