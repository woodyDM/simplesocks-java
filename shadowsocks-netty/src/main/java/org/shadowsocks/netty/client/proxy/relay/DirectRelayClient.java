package org.shadowsocks.netty.client.proxy.relay;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksCmdRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DirectRelayClient {

    private Bootstrap b;
    private String host;
    private int port;
    private EventLoopGroup group;
    private Channel remoteChannel;
    private SocksCmdRequest request;

    public SocksCmdRequest getRequest() {
        return request;
    }

    public void setRequest(SocksCmdRequest request) {
        this.request = request;

    }

    public Channel getRemoteChannel() {
        return remoteChannel;
    }

    public void setRemoteChannel(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    public DirectRelayClient(String host, int port, EventLoopGroup group ) {
        this.host = host;
        this.port = port;
        this.group = group;
    }

    public boolean isValid(){
        return remoteChannel!=null&&remoteChannel.isActive()&&remoteChannel.isOpen();
    }

    public void init( ) {
        b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new RelayHandlerInitializer(this));
        b.connect(host, port)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            log.warn("Failed to connect to host {} port {}  , close channel.", host, port);
                            future.channel().close();
                        }else{
                            remoteChannel = future.channel();
                            log.debug("success to connect to {}:{}",host,port);
                        }
                    }
                });
    }

}
