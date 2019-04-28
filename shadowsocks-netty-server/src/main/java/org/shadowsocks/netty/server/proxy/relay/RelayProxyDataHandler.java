package org.shadowsocks.netty.server.proxy.relay;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.common.protocol.*;
import org.shadowsocks.netty.common.util.ContentUtils;

import java.net.SocketAddress;
import java.util.Objects;

@Slf4j
public class RelayProxyDataHandler extends SimpleChannelInboundHandler<SimpleSocksCmdRequest> {

    private ProxyRequest proxyRequest;
    private Bootstrap bootstrap;
    private Channel toTargetServerChannel;
    private boolean isProxying;
    private Channel toLocalServerChannel;
    private EventLoopGroup eventLoopGroup;

    public RelayProxyDataHandler() {
        isProxying = false;
    }

    public void setProxyRequest(ProxyRequest proxyRequest) {
        this.proxyRequest = proxyRequest;
        init();
    }

    public void clear(){
        SocketAddress remoteAddress = toTargetServerChannel.remoteAddress();
        toTargetServerChannel.close().addListener(future -> {
            if(future.isSuccess())
                log.debug("close connection to {}", remoteAddress);
            else
                log.warn("close failed, connection is {}", remoteAddress);
        });
        bootstrap = null;
        proxyRequest = null;
        isProxying = false;
    }

    /**
     * init
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.toLocalServerChannel = ctx.channel();
        this.eventLoopGroup = ctx.channel().eventLoop();
    }

    private void init(){
        Channel localChannel = toLocalServerChannel;
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new TargetServerDataHandler(toLocalServerChannel));
                    }
                });
        bootstrap.connect(proxyRequest.getTarget(), proxyRequest.getPort())
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            toTargetServerChannel = future.channel();
                            Objects.requireNonNull(toTargetServerChannel);
                            log.info("Success! connect to host {}:{}.", proxyRequest.getTarget(), proxyRequest.getPort());
                            localChannel.writeAndFlush(new ServerResponse(DataType.PROXY_RESPONSE, ServerResponse.Code.SUCCESS));
                            isProxying = true;
                        }else{
                            log.warn("Failed to connect to host {}:{}  , close channel.", proxyRequest.getTarget(), proxyRequest.getPort());
                            localChannel.writeAndFlush(new ServerResponse(DataType.PROXY_RESPONSE, ServerResponse.Code.FAIL));
                            clear();
                        }
                    }
                });
    }


    /**
     * data handle
     * @param channelHandlerContext
     * @param simpleSocksCmdRequest
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SimpleSocksCmdRequest simpleSocksCmdRequest) throws Exception {
        log.debug("receive data [{}] from localServer",simpleSocksCmdRequest);
        DataType dataType = simpleSocksCmdRequest.getType();
        switch (dataType){
            case PROXY:{
                if(isProxying){
                    toLocalServerChannel.writeAndFlush(new ServerResponse(DataType.PROXY_RESPONSE, ServerResponse.Code.FAIL));
                }else{
                    setProxyRequest((ProxyRequest)simpleSocksCmdRequest);
                }
                break;
            }
            case PROXY_DATA:{
                ProxyDataRequest request = (ProxyDataRequest)simpleSocksCmdRequest;
                if(!isProxying){
                    toLocalServerChannel.writeAndFlush(new ServerResponse(DataType.PROXY_DATA_RESPONSE, ServerResponse.Code.FAIL));
                }else{
                    if(!toTargetServerChannel.isActive()){
                        log.warn("target channel is not active, client is too early to send data.");
                        channelHandlerContext.writeAndFlush(new ServerResponse(DataType.PROXY_DATA_RESPONSE, ServerResponse.Code.FAIL));
                    }else{
                        log.info("receive proxy data {} from local server .", proxyRequest);
                        toTargetServerChannel.writeAndFlush(request.getIncomingBuf()).addListener(future -> {
                            if(!future.isSuccess()){
                                log.debug("Failed to proxy data to target server");
                                channelHandlerContext.channel()
                                        .writeAndFlush(new ServerResponse(DataType.PROXY_DATA_RESPONSE, ServerResponse.Code.FAIL));
                            }else{
                                log.debug("Success send proxy data to target server. ");
                            }
                        });
                    }
                }
                break;
            }
            case END_PROXY:{
                clear();
                channelHandlerContext.writeAndFlush(new ServerResponse(DataType.END_PROXY_RESPONSE, ServerResponse.Code.SUCCESS));
                break;
            }
            default: throw new IllegalStateException("impossible to get here!");
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("error when relay proxy data {}",cause.getMessage());
        ctx.close();
        if(toTargetServerChannel!=null&&toTargetServerChannel.isActive()){
            toTargetServerChannel.close();
        }
    }
}
