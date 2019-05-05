package org.simplesocks.netty.app.proxy.relay.direct;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.protocol.ConnectionMessage;
import org.simplesocks.netty.common.util.ServerUtils;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class DirectRelayClient implements RelayClient {

    private Bootstrap b;
    private EventLoopGroup group;
    private Channel remoteChannel;
    private Consumer<byte[]> onDataAction;
    private Runnable onClose;

    public DirectRelayClient(EventLoopGroup group ) {
        this.group = group;
        init();
    }

    @Override
    public boolean isConnect() {
        return remoteChannel!=null&&remoteChannel.isActive();
    }


    @Override
    public Promise<Channel> sendProxyRequest(String host, int port, ConnectionMessage.Type proxyType, EventExecutor eventExecutor) {
        Promise<Channel> promise = eventExecutor.newPromise();
        b.connect(host, port)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            log.warn("Failed to connect to host {} port {}.", host, port);
                            promise.setFailure(new ProxyConnectException("failed to get proxy client"));
                        }else{
                            remoteChannel = future.channel();
                            promise.setSuccess(remoteChannel);
                            log.debug("Success to connect to {}:{}",host,port);
                        }
                    }
                });
        return promise;
    }




    @Override
    public void sendProxyData(byte[] data) {
        if(remoteChannel==null){
            throw new IllegalStateException("call sendProxyData after proxy request success!");
        }
        if(onDataAction==null){
            log.warn("no data action set , the response may not be received!");
        }
        if(data.length==0)return;
        ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
        this.remoteChannel.writeAndFlush(byteBuf).addListener(future -> {
            if(!future.isSuccess()){
                ServerUtils.logException(log, future.cause());
            }
        });
    }

    @Override
    public void onReceiveProxyData(Consumer<byte[]> action) {
        Objects.requireNonNull(action);
        this.onDataAction = action;
    }

    public void onReceiveProxyData(byte[] bytes){
        log.debug("client receive data len {}", bytes.length);
        this.onDataAction.accept(bytes);
    }

    @Override
    public void onClose(Runnable action) {
        this.onClose = action;
    }

    public void close(){
        if(this.remoteChannel!=null){
            log.debug("Closing client {}.",remoteChannel.remoteAddress());
            remoteChannel.close();
        }
        if(onClose!=null){
            onClose.run();
        }
    }


    private void init( ) {
        b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new RelayHandlerInitializer(this));

    }


}
