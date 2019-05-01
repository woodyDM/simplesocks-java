package org.simplesocks.netty.server.proxy.relay;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;
import org.simplesocks.netty.common.protocol.*;


import java.net.SocketAddress;

/**
 * local server data to target server
 */
@Slf4j
public class RelayProxyDataHandler extends SimpleChannelInboundHandler<SimpleSocksCmdRequest> {

    private ProxyRequest proxyRequest;
    private Bootstrap bootstrap;
    private Channel toTargetServerChannel;
    private boolean isProxying;
    private Channel toLocalServerChannel;
    private EventLoopGroup eventLoopGroup;
    private Encrypter encrypter = OffsetEncrypter.getInstance();


    public RelayProxyDataHandler() {
        isProxying = false;
    }



    public Channel getToLocalServerChannel() {
        return toLocalServerChannel;
    }

    public void setProxyRequest(ProxyRequest proxyRequest) {
        this.proxyRequest = proxyRequest;
        init();
    }

    public void clear(){
        if(toTargetServerChannel!=null){
            SocketAddress remoteAddress = toTargetServerChannel.remoteAddress();
            toTargetServerChannel.close().addListener(future -> {
                if(future.isSuccess())
                    log.debug("close connection to {}", remoteAddress);
                else
                    log.warn("close failed, connection is {}", remoteAddress);
            });
        }
        bootstrap = null;
        proxyRequest = null;
        isProxying = false;
    }



    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
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
                        socketChannel.pipeline().addLast(new TargetServerDataHandler(RelayProxyDataHandler.this));
                    }
                });
        bootstrap.connect(proxyRequest.getTarget(), proxyRequest.getPort())
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            toTargetServerChannel = future.channel();
                        }else{
                            log.warn("Failed to connect to target {}:{}.", proxyRequest.getTarget(), proxyRequest.getPort());
                            localChannel.writeAndFlush(new ServerResponse(DataType.PROXY_RESPONSE, ServerResponse.Code.FAIL));
                            clear();
                        }
                    }
                });
    }

    /**
     *
     */
    public void onTargetChannelActive(){
        log.info("Success! connect to target {}:{}.", proxyRequest.getTarget(), proxyRequest.getPort());
        toLocalServerChannel.writeAndFlush(new ServerResponse(DataType.PROXY_RESPONSE, ServerResponse.Code.SUCCESS));
        isProxying = true;
    }

    /**
     * data handle
     * @param channelHandlerContext
     * @param simpleSocksCmdRequest
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SimpleSocksCmdRequest simpleSocksCmdRequest) throws Exception {
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
                        byte[] encoded = request.getBytes();
                        encoded = encrypter.decode(encoded);
                        ProxyDataRequest requestT = new ProxyDataRequest(encoded);
                        toTargetServerChannel.writeAndFlush(requestT.getIncomingBuf()).addListener(future -> {
                            if(!future.isSuccess()){
                                log.debug("Failed to proxy data to target server");
                                channelHandlerContext.channel()
                                        .writeAndFlush(new ServerResponse(DataType.PROXY_DATA_RESPONSE, ServerResponse.Code.FAIL));
                            }else{
                                channelHandlerContext.channel()
                                        .writeAndFlush(new ServerResponse(DataType.PROXY_DATA_RESPONSE, ServerResponse.Code.SUCCESS));
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
            default: throw new IllegalStateException("Impossible to get here! Type is " + dataType);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        close(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("error when relay proxy data {}",cause.getMessage());
        close(ctx);
    }

    void close(ChannelHandlerContext ctx){
        clear();
        ctx.close();
    }
}
